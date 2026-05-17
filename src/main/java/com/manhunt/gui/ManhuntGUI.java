package com.manhunt.gui;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.Objects;

public class ManhuntGUI implements Listener {
    private final ManhuntPlugin plugin;

    public ManhuntGUI(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lManhunt Control Panel");

        inv.setItem(10, createGuiItem(Material.EMERALD_BLOCK, "§a§lStart Match", "§7Click to start the manhunt game."));
        inv.setItem(11, createGuiItem(Material.GOLD_BLOCK, "§e§lPause Match", "§7Click to freeze all players."));
        inv.setItem(12, createGuiItem(Material.REDSTONE_BLOCK, "§c§lEnd Match", "§7Click to end match and return to base world."));

        inv.setItem(14, createGuiItem(Material.GRASS_BLOCK, "§b§lWorld Management", "§7Click to generate or teleport to manhunt worlds."));
        inv.setItem(15, createGuiItem(Material.DIAMOND_BOOTS, "§d§lManage Runners", "§7Click to add or remove runners."));

        long limit = plugin.getConfigManager().getSettings().getTimeLimitSeconds();
        String limitStr = limit == 0 ? "Indefinite" : (limit / 60) + " minutes";
        inv.setItem(16, createGuiItem(Material.CLOCK, "§6§lTime Limit: §e" + limitStr, "§7Click to cycle: Indefinite / 30m / 1h / 2h"));

        player.openInventory(inv);
    }

    public void openWorldGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lWorld Manager");

        inv.setItem(11, createGuiItem(Material.DIAMOND_PICKAXE, "§a§lGenerate Worlds", "§7Generate overworld, nether, and end for manhunt."));
        inv.setItem(15, createGuiItem(Material.ENDER_PEARL, "§b§lTeleport All to Manhunt", "§7Teleport all players to manhunt spawn."));

        player.openInventory(inv);
    }

    public void openRunnersGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lManhunt Runners");

        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean isRunner = plugin.getPlayerManager().isRunner(p);
            Material mat = isRunner ? Material.DIAMOND_HELMET : Material.IRON_HELMET;
            String name = (isRunner ? "§a§l" : "§7") + p.getName() + (isRunner ? " (Runner)" : " (Hunter)");
            String lore = isRunner ? "§cClick to remove runner" : "§aClick to add runner";
            inv.setItem(slot++, createGuiItem(mat, name, lore));
        }

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals("§8§lManhunt Control Panel")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getRawSlot();
            if (slot == 10) {
                player.closeInventory();
                plugin.getGameManager().start();
            } else if (slot == 11) {
                player.closeInventory();
                plugin.getGameManager().pause();
            } else if (slot == 12) {
                player.closeInventory();
                plugin.getGameManager().end();
            } else if (slot == 14) {
                openWorldGUI(player);
            } else if (slot == 15) {
                openRunnersGUI(player);
            } else if (slot == 16) {
                long current = plugin.getConfigManager().getSettings().getTimeLimitSeconds();
                long next = current == 0 ? 1800 : (current == 1800 ? 3600 : (current == 3600 ? 7200 : 0));
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(next);
                player.sendMessage("§aTime limit instantly updated to " + (next == 0 ? "Indefinite" : (next / 60) + " minutes"));
                openMainGUI(player); // Reopen to instantly update UI
            }
        } else if (title.equals("§8§lWorld Manager")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                player.closeInventory();
                player.sendMessage("§eStarting world generation...");
                plugin.getWorldManager().generateWorlds(() -> player.sendMessage("§aWorlds generated successfully!"));
            } else if (slot == 15) {
                player.closeInventory();
                plugin.getWorldManager().teleportAllToManhunt();
            }
        } else if (title.equals("§8§lManhunt Runners")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

            String name = Objects.requireNonNull(clicked.getItemMeta()).getDisplayName();
            String rawName = name.replace("§a§l", "").replace("§7", "").replace(" (Runner)", "").replace(" (Hunter)", "");
            Player target = Bukkit.getPlayerExact(rawName);

            if (target != null) {
                if (plugin.getPlayerManager().isRunner(target)) {
                    plugin.getPlayerManager().removeRunner(target);
                } else {
                    plugin.getPlayerManager().addRunner(target);
                }
                openRunnersGUI(player); // Refresh GUI instantly
            }
        }
    }
}
