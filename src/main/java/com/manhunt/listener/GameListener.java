package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import com.manhunt.game.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameListener implements Listener {
    private final ManhuntPlugin plugin;

    public GameListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isPaused() {
        return plugin.getGameManager().getState() == GameState.PAUSED;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isPaused()) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                event.setTo(from);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPaused() && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isPaused()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isPaused()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isPaused()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (isPaused() && event.getTarget() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        if (event.getCause() == org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if (isPaused()) {
                event.setCancelled(true);
            }
        }
    }
}
