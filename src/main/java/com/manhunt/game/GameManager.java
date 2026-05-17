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
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final ManhuntPlugin plugin;
    private GameState state = GameState.WAITING;
    private BukkitTask graceTask;
    private long remainingGraceSeconds = 0;

    public GameManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public GameState getState() {
        return state;
    }

    public void start() {
        if (state == GameState.PLAYING || state == GameState.STARTING) {
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
        plugin.getWorldManager().teleportAllToManhunt();

        // Heal and reset players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setSaturation(20.0f);
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
        }

        plugin.getCompassManager().updateAllHuntersInventory();

        long grace = plugin.getConfigManager().getSettings().getHeadStartSeconds();
        if (grace > 0) {
            state = GameState.STARTING;
            remainingGraceSeconds = grace;
            startGraceCountdown(grace);
        } else {
            state = GameState.PLAYING;
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lMANHUNT STARTED", "§eRunners vs Hunters!", 10, 70, 20);
            }
            plugin.getTimerManager().start();
        }
    }

    private void startGraceCountdown(long totalSeconds) {
        if (graceTask != null) {
            graceTask.cancel();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getPlayerManager().isRunner(p)) {
                p.sendTitle("§a§lHEAD START!", "§eRun while hunters are frozen! (" + totalSeconds + "s)", 10, 70, 20);
            } else {
                p.sendTitle("§6§lGRACE PERIOD", totalSeconds + " seconds remaining!", 10, 70, 20);
            }
        }

        graceTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (state != GameState.STARTING) {
                    if (graceTask != null) graceTask.cancel();
                    return;
                }

                if (remainingGraceSeconds <= 0) {
                    state = GameState.PLAYING;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§c§lTHE HUNT HAS BEGUN!", "§eHunters are un-frozen! Go!", 10, 70, 20);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                    }
                    plugin.getTimerManager().start();
                    if (graceTask != null) graceTask.cancel();
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!plugin.getPlayerManager().isRunner(p)) {
                            p.sendActionBar("§6§lHunters Frozen: §e" + remainingGraceSeconds + "s");
                        } else {
                            p.sendActionBar("§a§lHead Start: §e" + remainingGraceSeconds + "s");
                        }
                    }
                    remainingGraceSeconds--;
                }
            }
        }, 0L, 20L);
    }

    public boolean pause() {
        if (state == GameState.PLAYING || state == GameState.STARTING) {
            GameState prev = state;
            state = GameState.PAUSED;
            if (prev == GameState.PLAYING) {
                plugin.getTimerManager().pause();
            } else if (prev == GameState.STARTING) {
                if (graceTask != null) graceTask.cancel();
            }
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
            if (remainingGraceSeconds > 0) {
                state = GameState.STARTING;
                startGraceCountdown(remainingGraceSeconds);
            } else {
                state = GameState.PLAYING;
                plugin.getTimerManager().start();
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lGAME RESUMED", "§eGo go go!", 10, 70, 20);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f);
            }
            return true;
        }
        return false;
    }

    public boolean end() {
        if (state != GameState.PLAYING && state != GameState.PAUSED && state != GameState.STARTING) {
            return false;
        }
        if (graceTask != null) {
            graceTask.cancel();
        }
        remainingGraceSeconds = 0;
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
            ItemStack offhand = p.getInventory().getItemInOffHand();
            if (plugin.getCompassManager().isTrackerCompass(offhand)) {
                p.getInventory().setItemInOffHand(null);
            }
        }

        plugin.getWorldManager().teleportAllToBaseWorld();
        Bukkit.broadcastMessage("§c§lManhunt match has ended.");
        state = GameState.WAITING;
        return true;
    }

    public void handleRunnersWin() {
        if (state != GameState.PLAYING && state != GameState.STARTING) return;
        if (graceTask != null) graceTask.cancel();
        remainingGraceSeconds = 0;
        state = GameState.ENDED;
        plugin.getTimerManager().pause();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§b§lRUNNERS WIN!", "§eThe Ender Dragon has been defeated!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            spawnVictoryFireworks(p);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 300L); // End after 15 seconds
    }

    public void handleHuntersWin() {
        if (state != GameState.PLAYING && state != GameState.STARTING) return;
        if (graceTask != null) graceTask.cancel();
        remainingGraceSeconds = 0;
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
        if (state != GameState.PLAYING && state != GameState.STARTING) return;
        if (graceTask != null) graceTask.cancel();
        remainingGraceSeconds = 0;
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
