package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    private final ManhuntPlugin plugin;

    public InventoryListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getCompassManager().isTrackerCompass(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop your Manhunt tracking compass!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean isClickedCompass = plugin.getCompassManager().isTrackerCompass(clicked);
        boolean isCursorCompass = plugin.getCompassManager().isTrackerCompass(cursor);

        if (!isClickedCompass && !isCursorCompass) {
            return;
        }

        // Prevent moving compasses out of player inventory into chests or containers
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage("§cYou cannot store tracking compasses in containers!");
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage("§cYou cannot store tracking compasses in containers!");
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING && event.getInventory().getType() != InventoryType.PLAYER) {
            ItemStack oldCursor = event.getOldCursor();
            if (plugin.getCompassManager().isTrackerCompass(oldCursor)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player p) {
                    p.sendMessage("§cYou cannot store tracking compasses in containers!");
                }
            }
        }
    }

    @EventHandler
    public void onEntityInteract(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (plugin.getCompassManager().isTrackerCompass(hand) || plugin.getCompassManager().isTrackerCompass(offhand)) {
            if (event.getRightClicked() instanceof org.bukkit.entity.ItemFrame || event.getRightClicked() instanceof org.bukkit.entity.ArmorStand) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot place tracking compasses on entities!");
            }
        }
    }
}
