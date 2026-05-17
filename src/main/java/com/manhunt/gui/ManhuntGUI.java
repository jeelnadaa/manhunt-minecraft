package com.manhunt.gui;

import com.manhunt.ManhuntPlugin;
import com.manhunt.game.GameState;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManhuntGUI implements Listener {
    private final ManhuntPlugin plugin;
    private final Map<UUID, Boolean> pendingTimerInput = new ConcurrentHashMap<>();

    public ManhuntGUI(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPendingTimerInput(Player player) {
        return pendingTimerInput.containsKey(player.getUniqueId());
    }

    public void handleCustomTimerInput(Player p, String msg) {
        pendingTimerInput.remove(p.getUniqueId());
        if (msg.equalsIgnoreCase("cancel")) {
            p.sendMessage("§cCustom timer input cancelled.");
            Bukkit.getScheduler().runTask(plugin, () -> openTimerGUI(p));
            return;
        }
        try {
            long mins = Long.parseLong(msg.trim());
            if (mins < 0) mins = 0;
            long secs = mins * 60;
            plugin.getConfigManager().getSettings().setTimeLimitSeconds(secs);
            p.sendMessage("§aTime limit instantly updated to " + (mins == 0 ? "Indefinite" : mins + " minutes."));
            Bukkit.getScheduler().runTask(plugin, () -> openTimerGUI(p));
        } catch (NumberFormatException e) {
            p.sendMessage("§cInvalid number! Input cancelled.");
            Bukkit.getScheduler().runTask(plugin, () -> openTimerGUI(p));
        }
    }

    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lManhunt Control Panel");

        inv.setItem(10, createGuiItem(Material.EMERALD_BLOCK, "§a§lStart Match", "§7Click to start the manhunt game."));

        GameState state = plugin.getGameManager().getState();
        if (state == GameState.PAUSED) {
            inv.setItem(11, createGuiItem(Material.EMERALD, "§a§lResume Match", "§7Click to unpause and resume match."));
        } else {
            inv.setItem(11, createGuiItem(Material.GOLD_BLOCK, "§e§lPause Match", "§7Click to freeze all players."));
        }

        inv.setItem(12, createGuiItem(Material.REDSTONE_BLOCK, "§c§lEnd Match", "§7Click to end match and return to base world."));

        inv.setItem(14, createGuiItem(Material.GRASS_BLOCK, "§b§lWorld Management", "§7Click to generate or teleport to worlds."));
        inv.setItem(15, createGuiItem(Material.DIAMOND_BOOTS, "§d§lManage Runners", "§7Click to add or remove runners."));

        long limit = plugin.getConfigManager().getSettings().getTimeLimitSeconds();
        String limitStr = limit == 0 ? "Indefinite" : (limit / 60) + " minutes";
        inv.setItem(16, createGuiItem(Material.CLOCK, "§6§lTime Limit: §e" + limitStr, "§7Click to configure time limit options."));

        player.openInventory(inv);
    }

    public void openTimerGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lTimer Manager");

        long current = plugin.getConfigManager().getSettings().getTimeLimitSeconds();
        inv.setItem(10, createGuiItem(Material.CLOCK, "§e§lIndefinite", "§7Set time limit to 0 (No limit)" + (current == 0 ? " §a(Active)" : "")));
        inv.setItem(11, createGuiItem(Material.GOLD_NUGGET, "§6§l15 Minutes", "§7Set time limit to 900 seconds" + (current == 900 ? " §a(Active)" : "")));
        inv.setItem(12, createGuiItem(Material.GOLD_INGOT, "§6§l30 Minutes", "§7Set time limit to 1800 seconds" + (current == 1800 ? " §a(Active)" : "")));
        inv.setItem(13, createGuiItem(Material.GOLD_BLOCK, "§6§l1 Hour", "§7Set time limit to 3600 seconds" + (current == 3600 ? " §a(Active)" : "")));
        inv.setItem(14, createGuiItem(Material.DIAMOND, "§b§l2 Hours", "§7Set time limit to 7200 seconds" + (current == 7200 ? " §a(Active)" : "")));

        inv.setItem(16, createGuiItem(Material.NAME_TAG, "§d§lCustom Time", "§7Click to type custom minutes in chat."));

        inv.setItem(26, createGuiItem(Material.ARROW, "§c§l⬅ Back to Main Menu", null));

        player.openInventory(inv);
    }

    public void openWorldGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lWorld Manager");

        inv.setItem(10, createGuiItem(Material.DIAMOND_PICKAXE, "§a§lGenerate Worlds", "§7Generate overworld, nether, and end."));
        inv.setItem(12, createGuiItem(Material.ENDER_PEARL, "§b§lTP All to Manhunt", "§7Teleport all players to manhunt spawn."));
        inv.setItem(13, createGuiItem(Material.COMPASS, "§b§lTP Specific to Manhunt", "§7Select a player to TP to Manhunt."));
        inv.setItem(14, createGuiItem(Material.CHORUS_FRUIT, "§e§lTP All to Base World", "§7Teleport all players to base world."));
        inv.setItem(15, createGuiItem(Material.RECOVERY_COMPASS, "§e§lTP Specific to Base", "§7Select a player to TP to Base World."));

        inv.setItem(26, createGuiItem(Material.ARROW, "§c§l⬅ Back to Main Menu", null));

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

        inv.setItem(53, createGuiItem(Material.ARROW, "§c§l⬅ Back to Main Menu", null));

        player.openInventory(inv);
    }

    public void openTpSelectionGUI(Player player, boolean toManhunt) {
        String title = "§8§lTP " + (toManhunt ? "Manhunt" : "Base");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            inv.setItem(slot++, createGuiItem(Material.PLAYER_HEAD, "§e" + p.getName(), "§7Click to teleport to " + (toManhunt ? "Manhunt World" : "Base World")));
        }

        inv.setItem(53, createGuiItem(Material.ARROW, "§c§l⬅ Back to World Menu", null));

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
                if (plugin.getGameManager().getState() == GameState.PAUSED) {
                    player.closeInventory();
                    plugin.getGameManager().resume();
                } else if (plugin.getGameManager().pause()) {
                    player.closeInventory();
                } else {
                    player.sendMessage("§cCannot pause: No active match is playing!");
                    player.closeInventory();
                }
            } else if (slot == 12) {
                if (plugin.getGameManager().end()) {
                    player.closeInventory();
                } else {
                    player.sendMessage("§cCannot end: No active match is running!");
                    player.closeInventory();
                }
            } else if (slot == 14) {
                openWorldGUI(player);
            } else if (slot == 15) {
                openRunnersGUI(player);
            } else if (slot == 16) {
                openTimerGUI(player);
            }
        } else if (title.equals("§8§lTimer Manager")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 10) {
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(0);
                player.sendMessage("§aTime limit instantly updated to Indefinite.");
                openTimerGUI(player);
            } else if (slot == 11) {
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(900);
                player.sendMessage("§aTime limit instantly updated to 15 minutes.");
                openTimerGUI(player);
            } else if (slot == 12) {
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(1800);
                player.sendMessage("§aTime limit instantly updated to 30 minutes.");
                openTimerGUI(player);
            } else if (slot == 13) {
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(3600);
                player.sendMessage("§aTime limit instantly updated to 1 hour.");
                openTimerGUI(player);
            } else if (slot == 14) {
                plugin.getConfigManager().getSettings().setTimeLimitSeconds(7200);
                player.sendMessage("§aTime limit instantly updated to 2 hours.");
                openTimerGUI(player);
            } else if (slot == 16) {
                player.closeInventory();
                pendingTimerInput.put(player.getUniqueId(), true);
                player.sendMessage("§eType your custom time limit in minutes in chat (or type 'cancel').");
            } else if (slot == 26) {
                openMainGUI(player);
            }
        } else if (title.equals("§8§lWorld Manager")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 10) {
                player.closeInventory();
                player.sendMessage("§eStarting world generation...");
                plugin.getWorldManager().generateWorlds(() -> player.sendMessage("§aWorlds generated successfully!"));
            } else if (slot == 12) {
                player.closeInventory();
                plugin.getWorldManager().teleportAllToManhunt();
            } else if (slot == 13) {
                openTpSelectionGUI(player, true);
            } else if (slot == 14) {
                player.closeInventory();
                plugin.getWorldManager().teleportAllToBaseWorld();
            } else if (slot == 15) {
                openTpSelectionGUI(player, false);
            } else if (slot == 26) {
                openMainGUI(player);
            }
        } else if (title.equals("§8§lManhunt Runners")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 53) {
                openMainGUI(player);
                return;
            }
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
                openRunnersGUI(player);
            }
        } else if (title.startsWith("§8§lTP ")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 53) {
                openWorldGUI(player);
                return;
            }
            boolean toManhunt = title.equals("§8§lTP Manhunt");
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

            String rawName = Objects.requireNonNull(clicked.getItemMeta()).getDisplayName().replace("§e", "");
            Player target = Bukkit.getPlayerExact(rawName);
            if (target != null) {
                if (toManhunt) {
                    plugin.getWorldManager().teleportToManhunt(target);
                } else {
                    plugin.getWorldManager().teleportToBase(target);
                }
                player.sendMessage("§aTeleported " + target.getName() + " successfully.");
            }
        }
    }
}
