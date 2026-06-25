package dev.gthebest;

import dev.gthebest.command.MoonSpawnCommand;
import dev.gthebest.command.SetSpawnCommand;
import dev.gthebest.command.SpawnCommand;
import dev.gthebest.config.PluginSettings;
import dev.gthebest.listener.PlayerListener;
import dev.gthebest.service.LanguageService;
import dev.gthebest.service.MessageService;
import dev.gthebest.service.SpawnService;
import dev.gthebest.service.TeleportService;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class MoonSpawn extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 0;

    private PluginSettings settings;
    private LanguageService languageService;
    private MessageService messageService;
    private SpawnService spawnService;
    private TeleportService teleportService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("lang/tr.yml");
        saveResourceIfMissing("lang/en.yml");
        saveResourceIfMissing("lang/es.yml");

        reloadServices();
        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (settings.isMetricsEnabled() && BSTATS_PLUGIN_ID > 0) {
            new Metrics(this, BSTATS_PLUGIN_ID);
        }

        getLogger().info("MoonSpawn enabled.");
    }

    @Override
    public void onDisable() {
        if (teleportService != null) {
            teleportService.cancelAll();
        }
    }

    public void reloadServices() {
        reloadConfig();
        this.settings = new PluginSettings(getConfig());
        this.languageService = new LanguageService(this, settings);
        this.messageService = new MessageService(this, settings, languageService);
        this.spawnService = new SpawnService(this, settings);

        if (this.teleportService != null) {
            this.teleportService.cancelAll();
        }
        this.teleportService = new TeleportService(this, settings, messageService, spawnService);
    }

    public PluginSettings settings() {
        return settings;
    }

    public MessageService messages() {
        return messageService;
    }

    public SpawnService spawns() {
        return spawnService;
    }

    public TeleportService teleports() {
        return teleportService;
    }

    private void registerCommands() {
        getCommand("spawn").setExecutor(new SpawnCommand(this, messageService));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this, messageService));
        MoonSpawnCommand adminCommand = new MoonSpawnCommand(this);
        getCommand("moonspawn").setExecutor(adminCommand);
        getCommand("moonspawn").setTabCompleter(adminCommand);
    }

    private void saveResourceIfMissing(String path) {
        if (!getDataFolder().toPath().resolve(path).toFile().exists()) {
            saveResource(path, false);
        }
    }
}
