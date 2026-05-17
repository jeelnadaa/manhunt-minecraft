package com.manhunt.game;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class GameManager {
    private final ManhuntPlugin plugin;
    private GameState state = GameState.WAITING;

    public GameManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public GameState getState() {
        return state;
    }

    public void start() {
        if (state == GameState.PLAYING) {
            return;
        }

        if (!plugin.getWorldManager().areWorldsGenerated()) {
            Bukkit.broadcastMessage("§eGenerating Manhunt worlds first... Please wait.");
            plugin.getWorldManager().generateWorlds(() -> {
                executeStart();
            });
        } else {
            executeStart();
        }
    }

    private void executeStart() {
        state = GameState.PLAYING;
        plugin.getWorldManager().teleportAllToManhunt();

        // Heal and reset players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setSaturation(20.0f);
            p.sendTitle("§a§lMANHUNT STARTED", "§eRunners vs Hunters!", 10, 70, 20);
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
        }

        plugin.getCompassManager().updateAllHuntersInventory();
        plugin.getTimerManager().start();
    }

    public boolean pause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            plugin.getTimerManager().pause();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§c§lGAME PAUSED", "§7Nobody can move or take damage", 10, 70, 20);
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.8f);
            }
            return true;
        }
        return false;
    }

    public boolean resume() {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            plugin.getTimerManager().start();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lGAME RESUMED", "§eGo go go!", 10, 70, 20);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f);
            }
            return true;
        }
        return false;
    }

    public boolean end() {
        if (state != GameState.PLAYING && state != GameState.PAUSED) {
            return false;
        }
        state = GameState.ENDED;
        plugin.getTimerManager().reset();

        // Clear compasses
        for (Player p : Bukkit.getOnlinePlayers()) {
            Inventory inv = p.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (plugin.getCompassManager().isTrackerCompass(item)) {
                    inv.setItem(i, null);
                }
            }
        }

        plugin.getWorldManager().teleportAllToBaseWorld();
        Bukkit.broadcastMessage("§c§lManhunt match has ended.");
        state = GameState.WAITING;
        return true;
    }

    public void handleRunnersWin() {
        if (state != GameState.PLAYING) return;
        state = GameState.ENDED;
        plugin.getTimerManager().pause();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§b§lRUNNERS WIN!", "§eThe Ender Dragon has been defeated!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            spawnVictoryFireworks(p);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 140L); // End after 7 seconds
    }

    public void handleHuntersWin() {
        if (state != GameState.PLAYING) return;
        state = GameState.ENDED;
        plugin.getTimerManager().pause();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§c§lHUNTERS WIN!", "§eAll runners have been eliminated!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
            spawnVictoryFireworks(p);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 140L);
    }

    public void handleTimeUp() {
        if (state != GameState.PLAYING) return;
        state = GameState.ENDED;
        plugin.getTimerManager().pause();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§c§lTIME UP!", "§eRunners failed to beat the time limit. Hunters Win!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 140L);
    }

    private void spawnVictoryFireworks(Player player) {
        Firework fw = player.getWorld().spawn(player.getLocation().add(0, 2, 0), Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.YELLOW, Color.AQUA, Color.GREEN)
                .withFade(Color.RED, Color.PURPLE)
                .build());
        fm.setPower(1);
        fw.setFireworkMeta(fm);
    }
}
