package com.manhunt.world;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class WorldManager {
    private final ManhuntPlugin plugin;

    public WorldManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean areWorldsGenerated() {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        return Bukkit.getWorld(prefix) != null;
    }

    public void generateWorlds(Runnable onComplete) {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        plugin.getLogger().info("Generating Manhunt Overworld...");
        World overworld = new WorldCreator(prefix).environment(World.Environment.NORMAL).createWorld();

        plugin.getLogger().info("Generating Manhunt Nether...");
        World nether = new WorldCreator(prefix + "_nether").environment(World.Environment.NETHER).createWorld();

        plugin.getLogger().info("Generating Manhunt End...");
        World end = new WorldCreator(prefix + "_the_end").environment(World.Environment.THE_END).createWorld();

        if (onComplete != null) {
            onComplete.run();
        }
    }

    public World getManhuntOverworld() {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        return Bukkit.getWorld(prefix);
    }

    public World getManhuntNether() {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        return Bukkit.getWorld(prefix + "_nether");
    }

    public World getManhuntEnd() {
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        return Bukkit.getWorld(prefix + "_the_end");
    }

    public void teleportToManhunt(Player player) {
        World world = getManhuntOverworld();
        if (world != null) {
            player.teleportAsync(world.getSpawnLocation());
            player.sendMessage("§aTeleported to Manhunt World.");
        } else {
            player.sendMessage("§cManhunt worlds have not been generated yet! Run /mh generate");
        }
    }

    public void teleportAllToManhunt() {
        World world = getManhuntOverworld();
        if (world != null) {
            Location spawn = world.getSpawnLocation();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleportAsync(spawn);
                player.sendMessage("§aTeleported to Manhunt World.");
            }
        } else {
            plugin.getLogger().warning("Cannot teleport all: Manhunt world not generated.");
        }
    }

    public void teleportAllToBaseWorld() {
        String baseWorldName = plugin.getConfigManager().getSettings().getBaseWorld();
        World base = Bukkit.getWorld(baseWorldName);
        if (base == null && !Bukkit.getWorlds().isEmpty()) {
            base = Bukkit.getWorlds().get(0);
        }
        if (base != null) {
            Location spawn = base.getSpawnLocation();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleportAsync(spawn);
                player.sendMessage("§eReturned to Base World.");
            }
        }
    }
}
