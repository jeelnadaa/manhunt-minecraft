package com.manhunt.player;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
    private final ManhuntPlugin plugin;

    public PlayerManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRunner(UUID uuid) {
        return plugin.getConfigManager().getSettings().getRunnerUuids().contains(uuid);
    }

    public boolean isRunner(Player player) {
        return isRunner(player.getUniqueId());
    }

    public void addRunner(Player player) {
        plugin.getConfigManager().getSettings().addRunnerUuid(player.getUniqueId());
        player.sendMessage("§aYou have been assigned as a Manhunt Runner!");
        // Update compasses for all hunters
        plugin.getCompassManager().updateAllHuntersInventory();
    }

    public void removeRunner(Player player) {
        plugin.getConfigManager().getSettings().removeRunnerUuid(player.getUniqueId());
        player.sendMessage("§cYou are no longer a Manhunt Runner.");
        // Update compasses for all hunters
        plugin.getCompassManager().updateAllHuntersInventory();
    }

    public List<Player> getOnlineRunners() {
        List<Player> runners = new ArrayList<>();
        List<UUID> runnerUuids = plugin.getConfigManager().getSettings().getRunnerUuids();
        for (UUID uuid : runnerUuids) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                runners.add(p);
            }
        }
        return runners;
    }

    public List<Player> getOnlineHunters() {
        List<Player> hunters = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isRunner(p)) {
                hunters.add(p);
            }
        }
        return hunters;
    }

    public void handleRunnerDeath(Player runner) {
        if (isRunner(runner)) {
            runner.setGameMode(GameMode.SPECTATOR);
            runner.sendMessage("§cYou died and are now a spectator!");
        }
    }

    public boolean areAllRunnersDead() {
        List<UUID> runnerUuids = plugin.getConfigManager().getSettings().getRunnerUuids();
        if (runnerUuids.isEmpty()) {
            return false;
        }
        for (UUID uuid : runnerUuids) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && p.getGameMode() != GameMode.SPECTATOR) {
                return false; // Found an active runner
            }
        }
        return true;
    }
}
