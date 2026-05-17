package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import com.manhunt.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;
import java.util.Objects;

public class PlayerListener implements Listener {
    private final ManhuntPlugin plugin;

    public PlayerListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getTimerManager().showToPlayer(player);

        if (plugin.getGameManager().getState() == GameState.PLAYING) {
            if (!plugin.getPlayerManager().isRunner(player)) {
                plugin.getCompassManager().updateAllHuntersInventory();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTimerManager().hideFromPlayer(player);

        if (plugin.getGameManager().getState() == GameState.PLAYING) {
            if (plugin.getPlayerManager().isRunner(player)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (plugin.getPlayerManager().areAllRunnersDead()) {
                        plugin.getGameManager().handleHuntersWin();
                    }
                }, 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getGameManager().getState() == GameState.PLAYING) {
            if (plugin.getPlayerManager().isRunner(player)) {
                plugin.getPlayerManager().handleRunnerDeath(player);
                if (plugin.getPlayerManager().areAllRunnersDead()) {
                    plugin.getGameManager().handleHuntersWin();
                }
            } else {
                // Hunter died: filter out tracking compasses from drops
                Iterator<ItemStack> iterator = event.getDrops().iterator();
                while (iterator.hasNext()) {
                    ItemStack item = iterator.next();
                    if (plugin.getCompassManager().isTrackerCompass(item)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getState() == GameState.PLAYING) {
            World overworld = plugin.getWorldManager().getManhuntOverworld();
            if (overworld != null && Objects.requireNonNull(player.getLocation().getWorld()).getName().startsWith(overworld.getName())) {
                event.setRespawnLocation(overworld.getSpawnLocation());
            }

            if (!plugin.getPlayerManager().isRunner(player)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getCompassManager().updateAllHuntersInventory();
                }, 10L);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            World world = event.getEntity().getWorld();
            String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
            if (world.getName().startsWith(prefix)) {
                plugin.getGameManager().handleRunnersWin();
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        String prefix = plugin.getConfigManager().getSettings().getManhuntWorldPrefix();
        World currentWorld = from.getWorld();
        if (currentWorld == null || !currentWorld.getName().startsWith(prefix)) {
            return;
        }

        World overworld = plugin.getWorldManager().getManhuntOverworld();
        World nether = plugin.getWorldManager().getManhuntNether();
        World end = plugin.getWorldManager().getManhuntEnd();

        if (event.getCause() == PlayerPortalEvent.TeleportCause.NETHER_PORTAL) {
            if (currentWorld.getEnvironment() == World.Environment.NORMAL && nether != null) {
                Location target = new Location(nether, from.getX() / 8.0, from.getY(), from.getZ() / 8.0);
                event.setTo(target);
            } else if (currentWorld.getEnvironment() == World.Environment.NETHER && overworld != null) {
                Location target = new Location(overworld, from.getX() * 8.0, from.getY(), from.getZ() * 8.0);
                event.setTo(target);
            }
        } else if (event.getCause() == PlayerPortalEvent.TeleportCause.END_PORTAL) {
            if (currentWorld.getEnvironment() == World.Environment.NORMAL && end != null) {
                event.setTo(end.getSpawnLocation());
            } else if (currentWorld.getEnvironment() == World.Environment.THE_END && overworld != null) {
                event.setTo(overworld.getSpawnLocation());
            }
        }
    }
}
