package dev.gthebest.service;

import dev.gthebest.config.PluginSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class LanguageService {

    private static final List<String> SUPPORTED_LANGUAGES = Collections.unmodifiableList(Arrays.asList("tr", "en", "es"));

    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private FileConfiguration language;

    public LanguageService(JavaPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        reload();
    }

    public void reload() {
        String code = normalize(settings.language());
        File file = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!file.exists()) {
            code = "tr";
            file = new File(plugin.getDataFolder(), "lang/tr.yml");
        }
        this.language = YamlConfiguration.loadConfiguration(file);
    }

    public String getString(String path) {
        return language.getString(path, path);
    }

    public List<String> getStringList(String path) {
        List<String> list = language.getStringList(path);
        if (list.isEmpty()) {
            return Collections.singletonList(getString(path));
        }
        return list;
    }

    public boolean isSupported(String language) {
        return SUPPORTED_LANGUAGES.contains(normalize(language));
    }

    public List<String> supportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    public String normalize(String language) {
        if (language == null) {
            return "tr";
        }
        return language.toLowerCase(Locale.ROOT);
    }
}
