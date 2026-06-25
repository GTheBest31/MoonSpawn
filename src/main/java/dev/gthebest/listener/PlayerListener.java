package dev.gthebest.listener;

import dev.gthebest.MoonSpawn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {

    private final MoonSpawn plugin;

    public PlayerListener(MoonSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() != null) {
            plugin.teleports().cancelForMove(event.getPlayer(), event.getFrom(), event.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            plugin.teleports().cancelForDamage((Player) event.getEntity());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.teleports().cancel(event.getPlayer(), "teleport-cancel-other");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.settings().joinTeleport()) {
            return;
        }
        if (plugin.settings().firstJoinOnly() && event.getPlayer().hasPlayedBefore()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.spawns().teleportNow(event.getPlayer()), 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.settings().respawnTeleport()) {
            return;
        }
        plugin.spawns().getSpawn().ifPresent(event::setRespawnLocation);
    }
}
