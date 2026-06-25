package dev.gthebest.service;

import dev.gthebest.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class SpawnService {

    private final JavaPlugin plugin;
    private final PluginSettings settings;

    public SpawnService(JavaPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void saveSpawn(Location location) {
        FileConfiguration config = plugin.getConfig();
        config.set("spawn.enabled", true);
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Optional<Location> getSpawn() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("spawn.enabled", false)) {
            return Optional.empty();
        }

        String worldName = config.getString("spawn.world", "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Optional.empty();
        }

        Location location = new Location(
                world,
                config.getDouble("spawn.x"),
                config.getDouble("spawn.y"),
                config.getDouble("spawn.z"),
                (float) config.getDouble("spawn.yaw"),
                (float) config.getDouble("spawn.pitch")
        );

        if (settings.centerBlock()) {
            location.setX(location.getBlockX() + 0.5D);
            location.setZ(location.getBlockZ() + 0.5D);
        }
        return Optional.of(settings.safeTeleport() ? makeSafe(location) : location);
    }

    public String configuredWorldName() {
        return plugin.getConfig().getString("spawn.world", "");
    }

    public boolean isConfigured() {
        return plugin.getConfig().getBoolean("spawn.enabled", false);
    }

    public void teleportNow(Player player) {
        getSpawn().ifPresent(player::teleport);
    }

    private Location makeSafe(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = Math.max(world.getMinHeight() + 1, Math.min(location.getBlockY(), world.getMaxHeight() - 2));

        for (int currentY = y; currentY < world.getMaxHeight() - 1; currentY++) {
            Block feet = world.getBlockAt(x, currentY, z);
            Block head = world.getBlockAt(x, currentY + 1, z);
            Block ground = world.getBlockAt(x, currentY - 1, z);
            if (feet.isPassable() && head.isPassable() && ground.getType().isSolid()) {
                Location safe = new Location(world, location.getX(), currentY, location.getZ(), location.getYaw(), location.getPitch());
                return safe;
            }
        }
        return location;
    }
}
