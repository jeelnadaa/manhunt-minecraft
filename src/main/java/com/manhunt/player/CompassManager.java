package com.manhunt.player;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CompassManager {
    private final ManhuntPlugin plugin;
    private final NamespacedKey compassKey;
    private final NamespacedKey runnerUuidKey;

    public CompassManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.compassKey = new NamespacedKey(plugin, "manhunt_compass");
        this.runnerUuidKey = new NamespacedKey(plugin, "tracking_uuid");
    }

    public boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().has(compassKey, PersistentDataType.BYTE);
    }

    public UUID getTrackedRunner(ItemStack item) {
        if (!isTrackerCompass(item)) return null;
        String uuidStr = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().get(runnerUuidKey, PersistentDataType.STRING);
        if (uuidStr == null) return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ItemStack createTrackerCompass(Player runner) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aTracking: §e" + runner.getName());
            meta.getPersistentDataContainer().set(compassKey, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(runnerUuidKey, PersistentDataType.STRING, runner.getUniqueId().toString());
            compass.setItemMeta(meta);
        }
        return compass;
    }

    public void updateAllHuntersInventory() {
        List<UUID> runnerUuids = plugin.getConfigManager().getSettings().getRunnerUuids();
        List<Integer> slots = plugin.getConfigManager().getSettings().getCompassSlots();

        for (Player hunter : plugin.getPlayerManager().getOnlineHunters()) {
            Inventory inv = hunter.getInventory();
            List<UUID> trackedInInv = new ArrayList<>();

            // 1. Remove compasses for invalid or dead runners
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (isTrackerCompass(item)) {
                    UUID tracked = getTrackedRunner(item);
                    Player trackedPlayer = tracked != null ? Bukkit.getPlayer(tracked) : null;
                    boolean isDeadOrSpec = trackedPlayer != null && trackedPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR;
                    if (tracked == null || !runnerUuids.contains(tracked) || isDeadOrSpec) {
                        inv.setItem(i, null);
                    } else {
                        trackedInInv.add(tracked);
                    }
                }
            }

            // 2. Add compasses for active runners not yet in inventory
            for (UUID runnerUuid : runnerUuids) {
                if (!trackedInInv.contains(runnerUuid)) {
                    Player runner = Bukkit.getPlayer(runnerUuid);
                    if (runner != null && runner.isOnline() && runner.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                        ItemStack newCompass = createTrackerCompass(runner);
                        boolean placed = false;
                        for (int slot : slots) {
                            if (slot >= 0 && slot < inv.getSize()) {
                                if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR) {
                                    inv.setItem(slot, newCompass);
                                    placed = true;
                                    break;
                                }
                            }
                        }
                        if (!placed) {
                            inv.addItem(newCompass);
                        }
                    }
                }
            }
        }
    }

    public void startCompassUpdaterTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player hunter : plugin.getPlayerManager().getOnlineHunters()) {
                Inventory inv = hunter.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (isTrackerCompass(item)) {
                        UUID runnerUuid = getTrackedRunner(item);
                        if (runnerUuid != null) {
                            Player runner = Bukkit.getPlayer(runnerUuid);
                            if (runner != null && runner.isOnline()) {
                                CompassMeta meta = (CompassMeta) item.getItemMeta();
                                if (meta != null) {
                                    if (hunter.getWorld().equals(runner.getWorld())) {
                                        meta.setLodestone(runner.getLocation());
                                        meta.setLodestoneTracked(false);
                                        meta.setDisplayName("§aTracking: §e" + runner.getName());
                                        long dist = Math.round(hunter.getLocation().distance(runner.getLocation()));
                                        meta.setLore(List.of("§7Distance: §b" + dist + "m"));
                                    } else {
                                        Location northUp = hunter.getLocation().add(0, 0, -10000);
                                        meta.setLodestone(northUp);
                                        meta.setLodestoneTracked(false);
                                        meta.setDisplayName("§aTracking: §e" + runner.getName());
                                        meta.setLore(List.of("§cTarget in different dim: §6" + runner.getWorld().getEnvironment().name()));
                                    }
                                    item.setItemMeta(meta);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 10L); // Run every 10 ticks (0.5s)
    }
}
