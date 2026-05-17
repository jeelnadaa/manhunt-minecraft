package com.manhunt.game;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TimerManager {
    private final ManhuntPlugin plugin;
    private BossBar bossBar;
    private long elapsedSeconds = 0;
    private BukkitTask timerTask;

    public TimerManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        bossBar = Bukkit.createBossBar("§6Manhunt §8| §eWaiting for Game...", BarColor.YELLOW, BarStyle.SOLID);
    }

    public void start() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        bossBar.setColor(BarColor.GREEN);
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getGameManager().getState() == GameState.PLAYING) {
                elapsedSeconds++;
                long limit = plugin.getConfigManager().getSettings().getTimeLimitSeconds();

                if (limit > 0) {
                    long remaining = limit - elapsedSeconds;
                    if (remaining <= 0) {
                        plugin.getGameManager().handleTimeUp();
                        return;
                    }
                    double progress = remaining / (double) limit;
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                    bossBar.setTitle("§6Manhunt §8| §eRemaining: " + formatTime(remaining) + " §8| §cRunners: " + plugin.getPlayerManager().getOnlineRunners().size());
                } else {
                    bossBar.setProgress(1.0);
                    bossBar.setTitle("§6Manhunt §8| §eElapsed: " + formatTime(elapsedSeconds) + " §8| §cRunners: " + plugin.getPlayerManager().getOnlineRunners().size());
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!bossBar.getPlayers().contains(p)) {
                        bossBar.addPlayer(p);
                    }
                }
            } else if (plugin.getGameManager().getState() == GameState.PAUSED) {
                bossBar.setColor(BarColor.RED);
                bossBar.setTitle("§6Manhunt §8| §c§lGAME PAUSED");
            }
        }, 0L, 20L); // 1 second loop
    }

    public void pause() {
        if (bossBar != null) {
            bossBar.setColor(BarColor.RED);
            bossBar.setTitle("§6Manhunt §8| §c§lGAME PAUSED");
        }
    }

    public void reset() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        elapsedSeconds = 0;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setTitle("§6Manhunt §8| §eWaiting for Game...");
            bossBar.setColor(BarColor.YELLOW);
            bossBar.setProgress(1.0);
        }
    }

    public void showToPlayer(Player player) {
        if (bossBar != null && !bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    public void hideFromPlayer(Player player) {
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    private String formatTime(long totalSecs) {
        long hours = totalSecs / 3600;
        long mins = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, mins, secs);
        }
        return String.format("%02d:%02d", mins, secs);
    }
}
