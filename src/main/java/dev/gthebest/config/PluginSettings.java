package dev.gthebest.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class PluginSettings {

    private final FileConfiguration config;

    public PluginSettings(FileConfiguration config) {
        this.config = config;
    }

    public String language() {
        return config.getString("settings.language", "tr").toLowerCase();
    }

    public boolean smallCaps() {
        return config.getBoolean("settings.small-caps", true);
    }

    public boolean isMetricsEnabled() {
        return config.getBoolean("settings.metrics", true);
    }

    public int actionbarUpdateTicks() {
        return Math.max(1, config.getInt("settings.update-actionbar-every-ticks", 20));
    }

    public int delaySeconds() {
        return Math.max(0, config.getInt("teleport.delay-seconds", 3));
    }

    public int cooldownSeconds() {
        return Math.max(0, config.getInt("teleport.cooldown-seconds", 10));
    }

    public boolean cancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    public boolean cancelOnDamage() {
        return config.getBoolean("teleport.cancel-on-damage", true);
    }

    public boolean safeTeleport() {
        return config.getBoolean("teleport.safe-teleport", true);
    }

    public boolean centerBlock() {
        return config.getBoolean("teleport.center-block", false);
    }

    public boolean joinTeleport() {
        return config.getBoolean("join.teleport-to-spawn", false);
    }

    public boolean firstJoinOnly() {
        return config.getBoolean("join.first-join-only", false);
    }

    public boolean respawnTeleport() {
        return config.getBoolean("respawn.teleport-to-spawn", true);
    }

    public boolean titleEnabled() {
        return config.getBoolean("messages.title.enabled", true);
    }

    public boolean subtitleEnabled() {
        return config.getBoolean("messages.subtitle.enabled", true);
    }

    public boolean actionbarEnabled() {
        return config.getBoolean("messages.actionbar.enabled", true);
    }

    public int titleFadeIn() {
        return Math.max(0, config.getInt("messages.title.fade-in", 10));
    }

    public int titleStay() {
        return Math.max(0, config.getInt("messages.title.stay", 35));
    }

    public int titleFadeOut() {
        return Math.max(0, config.getInt("messages.title.fade-out", 15));
    }

    public boolean soundEnabled() {
        return config.getBoolean("effects.sound.enabled", true);
    }

    public String soundName() {
        return config.getString("effects.sound.name", "ENTITY_ENDERMAN_TELEPORT");
    }

    public float soundVolume() {
        return (float) config.getDouble("effects.sound.volume", 1.0D);
    }

    public float soundPitch() {
        return (float) config.getDouble("effects.sound.pitch", 1.2D);
    }

    public boolean particleEnabled() {
        return config.getBoolean("effects.particle.enabled", true);
    }

    public String particleName() {
        return config.getString("effects.particle.name", "PORTAL");
    }

    public int particleCount() {
        return Math.max(0, config.getInt("effects.particle.count", 40));
    }

    public double particleOffset() {
        return Math.max(0.0D, config.getDouble("effects.particle.offset", 0.4D));
    }
}
