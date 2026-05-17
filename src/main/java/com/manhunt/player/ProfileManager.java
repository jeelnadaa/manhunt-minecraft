package com.manhunt.player;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProfileManager {
    private final ManhuntPlugin plugin;
    private final File profilesDir;

    public ProfileManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.profilesDir = new File(plugin.getDataFolder(), "profiles");
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
    }

    private File getProfileFile(UUID uuid) {
        return new File(profilesDir, uuid.toString() + ".yml");
    }

    private FileConfiguration getProfileConfig(UUID uuid) {
        File file = getProfileFile(uuid);
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveProfileConfig(UUID uuid, FileConfiguration config) {
        try {
            config.save(getProfileFile(uuid));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save profile for " + uuid + ": " + e.getMessage());
        }
    }

    public void savePlayerState(Player player, String category) {
        FileConfiguration config = getProfileConfig(player.getUniqueId());

        config.set("uuid", player.getUniqueId().toString());
        config.set("last-category", category);
        if (category.equals("manhunt")) {
            config.set("manhunt-generation-id", plugin.getConfigManager().getSettings().getCurrentGenerationId());
        }

        String path = category + ".";
        config.set(path + "location", player.getLocation());
        config.set(path + "inventory", player.getInventory().getContents());
        config.set(path + "armor", player.getInventory().getArmorContents());
        config.set(path + "enderchest", player.getEnderChest().getContents());
        config.set(path + "health", player.getHealth());
        config.set(path + "food", player.getFoodLevel());
        config.set(path + "saturation", (double) player.getSaturation());
        config.set(path + "level", player.getLevel());
        config.set(path + "exp", (double) player.getExp());
        config.set(path + "gamemode", player.getGameMode().name());
        config.set(path + "effects", player.getActivePotionEffects());

        saveProfileConfig(player.getUniqueId(), config);
    }

    @SuppressWarnings("unchecked")
    public void loadPlayerState(Player player, String category) {
        FileConfiguration config = getProfileConfig(player.getUniqueId());
        String path = category + ".";

        if (!config.contains(path + "gamemode")) {
            // First time entering this realm: reset state to clean defaults
            player.getInventory().clear();
            player.getEnderChest().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(5.0f);
            player.setLevel(0);
            player.setExp(0.0f);
            player.setGameMode(GameMode.SURVIVAL);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            config.set("last-category", category);
            if (category.equals("manhunt")) {
                config.set("manhunt-generation-id", plugin.getConfigManager().getSettings().getCurrentGenerationId());
            }
            saveProfileConfig(player.getUniqueId(), config);
            return;
        }

        config.set("last-category", category);
        if (category.equals("manhunt")) {
            config.set("manhunt-generation-id", plugin.getConfigManager().getSettings().getCurrentGenerationId());
        }
        saveProfileConfig(player.getUniqueId(), config);

        List<?> invList = config.getList(path + "inventory");
        if (invList != null) {
            player.getInventory().setContents(invList.toArray(new ItemStack[0]));
        } else {
            player.getInventory().clear();
        }

        List<?> armorList = config.getList(path + "armor");
        if (armorList != null) {
            player.getInventory().setArmorContents(armorList.toArray(new ItemStack[0]));
        }

        List<?> ecList = config.getList(path + "enderchest");
        if (ecList != null) {
            player.getEnderChest().setContents(ecList.toArray(new ItemStack[0]));
        } else {
            player.getEnderChest().clear();
        }

        player.setHealth(config.getDouble(path + "health", 20.0));
        player.setFoodLevel(config.getInt(path + "food", 20));
        player.setSaturation((float) config.getDouble(path + "saturation", 5.0));
        player.setLevel(config.getInt(path + "level", 0));
        player.setExp((float) config.getDouble(path + "exp", 0.0));

        String gmStr = config.getString(path + "gamemode", "SURVIVAL");
        try {
            player.setGameMode(GameMode.valueOf(gmStr));
        } catch (IllegalArgumentException ignored) {}

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        List<?> effectsList = config.getList(path + "effects");
        if (effectsList != null) {
            for (Object obj : effectsList) {
                if (obj instanceof PotionEffect pe) {
                    player.addPotionEffect(pe);
                }
            }
        }
    }

    public void wipeAllManhuntProfiles() {
        File[] files = profilesDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("manhunt", null);
            config.set("last-category", "base");
            try {
                config.save(file);
            } catch (IOException ignored) {}
        }
    }

    public void handleJoin(Player player) {
        FileConfiguration config = getProfileConfig(player.getUniqueId());
        String lastCat = config.getString("last-category", "base");
        long genId = config.getLong("manhunt-generation-id", 0);

        if (lastCat.equals("manhunt")) {
            long currentGenId = plugin.getConfigManager().getSettings().getCurrentGenerationId();
            if (genId == currentGenId && plugin.getWorldManager().areWorldsGenerated()) {
                Location lastLoc = config.getLocation("manhunt.location");
                if (lastLoc != null && lastLoc.getWorld() != null) {
                    player.teleportAsync(lastLoc);
                } else {
                    plugin.getWorldManager().teleportToManhuntSpawn(player);
                }
                loadPlayerState(player, "manhunt");
            } else {
                config.set("manhunt", null);
                config.set("last-category", "base");
                saveProfileConfig(player.getUniqueId(), config);
                plugin.getWorldManager().teleportToBaseSpawn(player);
                loadPlayerState(player, "base");
            }
        } else {
            Location lastLoc = config.getLocation("base.location");
            if (lastLoc != null && lastLoc.getWorld() != null) {
                player.teleportAsync(lastLoc);
            } else {
                plugin.getWorldManager().teleportToBaseSpawn(player);
            }
            loadPlayerState(player, "base");
        }
    }

    public void handleQuit(Player player) {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        if (player.getLocation().getWorld() == null) return;
        String currentWorldName = player.getLocation().getWorld().getName();
        String cat = currentWorldName.startsWith(prefix) ? "manhunt" : "base";
        savePlayerState(player, cat);
    }

    public void switchRealm(Player player, boolean toManhunt) {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        if (player.getLocation().getWorld() == null) return;
        String currentWorldName = player.getLocation().getWorld().getName();
        String currentCat = currentWorldName.startsWith(prefix) ? "manhunt" : "base";
        String targetCat = toManhunt ? "manhunt" : "base";

        if (toManhunt && !plugin.getWorldManager().areWorldsGenerated()) {
            player.sendMessage("§cManhunt worlds have not been generated yet! Run /mh generate");
            return;
        }

        savePlayerState(player, currentCat);

        FileConfiguration config = getProfileConfig(player.getUniqueId());
        Location targetLoc = config.getLocation(targetCat + ".location");
        if (targetLoc != null && targetLoc.getWorld() != null) {
            player.teleportAsync(targetLoc).thenAccept(success -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    loadPlayerState(player, targetCat);
                    player.sendMessage("§aTeleported to your last position in " + (toManhunt ? "Manhunt World." : "Base World."));
                });
            });
        } else {
            if (toManhunt) {
                plugin.getWorldManager().teleportToManhuntSpawn(player).thenAccept(success -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        loadPlayerState(player, "manhunt");
                        player.sendMessage("§aTeleported to Manhunt World spawn.");
                    });
                });
            } else {
                plugin.getWorldManager().teleportToBaseSpawn(player).thenAccept(success -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        loadPlayerState(player, "base");
                        player.sendMessage("§eTeleported to Base World spawn.");
                    });
                });
            }
        }
    }

    public void switchAllToRealm(boolean toManhunt) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            switchRealm(player, toManhunt);
        }
    }
}
