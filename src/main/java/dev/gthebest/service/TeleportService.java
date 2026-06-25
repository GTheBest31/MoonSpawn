package dev.gthebest.service;

import dev.gthebest.config.PluginSettings;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportService {

    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private final MessageService messages;
    private final SpawnService spawnService;
    private final Map<UUID, BukkitTask> activeTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TeleportService(JavaPlugin plugin, PluginSettings settings, MessageService messages, SpawnService spawnService) {
        this.plugin = plugin;
        this.settings = settings;
        this.messages = messages;
        this.spawnService = spawnService;
    }

    public void requestTeleport(Player player) {
        if (!spawnService.isConfigured()) {
            messages.send(player, "spawn-not-set");
            return;
        }

        Optional<Location> spawn = spawnService.getSpawn();
        if (!spawn.isPresent()) {
            messages.send(player, "spawn-world-missing", messages.placeholders("world", spawnService.configuredWorldName()));
            return;
        }

        UUID uuid = player.getUniqueId();
        if (activeTeleports.containsKey(uuid)) {
            messages.send(player, "already-teleporting");
            return;
        }

        if (!player.hasPermission("moonspawn.bypass.cooldown")) {
            long remaining = getCooldownRemainingSeconds(uuid);
            if (remaining > 0L) {
                messages.send(player, "cooldown", messages.placeholders("seconds", remaining));
                return;
            }
        }

        int delay = settings.delaySeconds();
        if (delay <= 0) {
            completeTeleport(player, spawn.get());
            setCooldown(uuid);
            return;
        }

        messages.send(player, "teleport-start", messages.placeholders("seconds", delay));
        BukkitTask task = new BukkitRunnable() {
            private int remaining = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cleanup(uuid);
                    cancel();
                    return;
                }

                if (remaining <= 0) {
                    Optional<Location> currentSpawn = spawnService.getSpawn();
                    if (!currentSpawn.isPresent()) {
                        messages.send(player, "spawn-not-set");
                        cleanup(uuid);
                        cancel();
                        return;
                    }
                    completeTeleport(player, currentSpawn.get());
                    setCooldown(uuid);
                    cleanup(uuid);
                    cancel();
                    return;
                }

                Map<String, String> placeholders = messages.placeholders("seconds", remaining);
                messages.actionbar(player, "actionbar.countdown", placeholders);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, settings.actionbarUpdateTicks());

        activeTeleports.put(uuid, task);
    }

    public void cancelForMove(Player player, Location from, Location to) {
        if (!settings.cancelOnMove() || !sameBlockMove(from, to)) {
            return;
        }
        cancel(player, "teleport-cancel-move");
    }

    public void cancelForDamage(Player player) {
        if (!settings.cancelOnDamage()) {
            return;
        }
        cancel(player, "teleport-cancel-damage");
    }

    public void cancel(Player player, String messagePath) {
        BukkitTask task = activeTeleports.remove(player.getUniqueId());
        if (task == null) {
            return;
        }
        task.cancel();
        messages.send(player, messagePath);
    }

    public void cancelAll() {
        for (BukkitTask task : activeTeleports.values()) {
            task.cancel();
        }
        activeTeleports.clear();
    }

    public boolean isTeleporting(Player player) {
        return activeTeleports.containsKey(player.getUniqueId());
    }

    private void completeTeleport(Player player, Location spawn) {
        player.teleport(spawn);
        playEffects(player);
        messages.send(player, "teleport-success");
        messages.actionbar(player, "actionbar.success", messages.placeholders());
        messages.title(player, "title.teleport-success", "subtitle.teleport-success", messages.placeholders());
    }

    private void playEffects(Player player) {
        if (settings.soundEnabled()) {
            try {
                Sound sound = Sound.valueOf(settings.soundName());
                player.playSound(player.getLocation(), sound, settings.soundVolume(), settings.soundPitch());
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid sound in config: " + settings.soundName());
            }
        }

        if (settings.particleEnabled()) {
            try {
                Particle particle = Particle.valueOf(settings.particleName());
                double offset = settings.particleOffset();
                player.getWorld().spawnParticle(particle, player.getLocation().add(0.0D, 1.0D, 0.0D),
                        settings.particleCount(), offset, offset, offset, 0.0D);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid particle in config: " + settings.particleName());
            }
        }
    }

    private void setCooldown(UUID uuid) {
        int seconds = settings.cooldownSeconds();
        if (seconds > 0) {
            cooldowns.put(uuid, System.currentTimeMillis() + (seconds * 1000L));
        }
    }

    private long getCooldownRemainingSeconds(UUID uuid) {
        Long until = cooldowns.get(uuid);
        if (until == null) {
            return 0L;
        }

        long remainingMillis = until - System.currentTimeMillis();
        if (remainingMillis <= 0L) {
            cooldowns.remove(uuid);
            return 0L;
        }
        return (remainingMillis + 999L) / 1000L;
    }

    private void cleanup(UUID uuid) {
        activeTeleports.remove(uuid);
    }

    private boolean sameBlockMove(Location from, Location to) {
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()
                || !from.getWorld().equals(to.getWorld());
    }
}
