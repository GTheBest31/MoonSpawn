package dev.gthebest.command;

import dev.gthebest.MoonSpawn;
import dev.gthebest.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class MoonSpawnCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "info", "lang");

    private final MoonSpawn plugin;

    public MoonSpawnCommand(MoonSpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission("moonspawn.admin")) {
            messages.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sendInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadServices();
            plugin.messages().send(sender, "reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("lang")) {
            handleLanguage(sender, args);
            return true;
        }

        sendInfo(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("moonspawn.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("lang")) {
            return filter(plugin.messages().languages().supportedLanguages(), args[1]);
        }

        return Collections.emptyList();
    }

    private void sendInfo(CommandSender sender) {
        Map<String, String> placeholders = plugin.messages().placeholders();
        placeholders.put("version", plugin.getDescription().getVersion());
        placeholders.put("language", plugin.settings().language());
        placeholders.put("spawn", plugin.spawns().isConfigured() ? plugin.spawns().configuredWorldName() : "not set");
        plugin.messages().sendList(sender, "info", placeholders);
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        MessageService messages = plugin.messages();
        if (args.length < 2) {
            messages.send(sender, "language-unknown", messages.placeholders("languages",
                    String.join(", ", messages.languages().supportedLanguages())));
            return;
        }

        String language = args[1].toLowerCase(Locale.ROOT);
        if (!messages.languages().isSupported(language)) {
            messages.send(sender, "language-unknown", messages.placeholders("languages",
                    String.join(", ", messages.languages().supportedLanguages())));
            return;
        }

        plugin.getConfig().set("settings.language", language);
        plugin.saveConfig();
        plugin.reloadServices();
        plugin.messages().send(sender, "language-set", plugin.messages().placeholders("language", language));
    }

    private List<String> filter(List<String> source, String input) {
        String lowered = input.toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowered))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
