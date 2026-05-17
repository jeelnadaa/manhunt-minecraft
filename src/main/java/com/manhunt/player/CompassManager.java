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
        int[] allSlots = new int[37];
        for (int i = 0; i < 36; i++) allSlots[i] = i;
        allSlots[36] = 40;

        for (Player hunter : plugin.getPlayerManager().getOnlineHunters()) {
            org.bukkit.inventory.PlayerInventory inv = hunter.getInventory();
            List<UUID> trackedInInv = new ArrayList<>();

            // 1. Remove compasses for invalid or dead runners
            for (int slotIndex : allSlots) {
                ItemStack item = slotIndex == 40 ? inv.getItemInOffHand() : inv.getItem(slotIndex);
                if (isTrackerCompass(item)) {
                    UUID tracked = getTrackedRunner(item);
                    Player trackedPlayer = tracked != null ? Bukkit.getPlayer(tracked) : null;
                    boolean isDeadOrSpec = trackedPlayer != null && trackedPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR;
                    if (tracked == null || !runnerUuids.contains(tracked) || isDeadOrSpec) {
                        if (slotIndex == 40) {
                            inv.setItemInOffHand(null);
                        } else {
                            inv.setItem(slotIndex, null);
                        }
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

                        int emptySlot = -1;
                        for (int i = 0; i < 36; i++) {
                            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                                emptySlot = i;
                                break;
                            }
                        }

                        if (emptySlot != -1) {
                            inv.setItem(emptySlot, newCompass);
                        } else {
                            ItemStack displaced = inv.getItem(8);
                            inv.setItem(8, newCompass);
                            if (displaced != null && displaced.getType() != Material.AIR) {
                                hunter.getWorld().dropItemNaturally(hunter.getLocation(), displaced);
                                hunter.sendMessage("§eYour inventory was full! An item was dropped to make room for your tracking compass.");
                            }
                        }
                    }
                }
            }
        }
    }

    public void startCompassUpdaterTask() {
        int[] allSlots = new int[37];
        for (int i = 0; i < 36; i++) allSlots[i] = i;
        allSlots[36] = 40;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player hunter : plugin.getPlayerManager().getOnlineHunters()) {
                org.bukkit.inventory.PlayerInventory inv = hunter.getInventory();
                for (int slotIndex : allSlots) {
                    ItemStack item = slotIndex == 40 ? inv.getItemInOffHand() : inv.getItem(slotIndex);
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
                                    if (slotIndex == 40) {
                                        inv.setItemInOffHand(item);
                                    } else {
                                        inv.setItem(slotIndex, item);
                                    }
                                }
                            } else {
                                CompassMeta meta = (CompassMeta) item.getItemMeta();
                                if (meta != null) {
                                    String rName = runner != null ? runner.getName() : "Runner";
                                    meta.setDisplayName("§aTracking: §e" + rName);
                                    meta.setLore(List.of("§cRunner is OFFLINE (§7Last Known Location)"));
                                    item.setItemMeta(meta);
                                    if (slotIndex == 40) {
                                        inv.setItemInOffHand(item);
                                    } else {
                                        inv.setItem(slotIndex, item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 10L); // Run every 10 ticks (0.5s)
    }
}
