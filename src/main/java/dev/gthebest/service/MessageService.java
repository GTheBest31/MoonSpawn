package dev.gthebest.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.gthebest.config.PluginSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageService {

    private static final Pattern LEGACY_HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Map<Character, Character> SMALL_CAPS = createSmallCaps();

    private final JavaPlugin plugin;
    private final PluginSettings settings;
    private final LanguageService languageService;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character(ChatColor.COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final Cache<String, String> renderCache = Caffeine.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public MessageService(JavaPlugin plugin, PluginSettings settings, LanguageService languageService) {
        this.plugin = plugin;
        this.settings = settings;
        this.languageService = languageService;
    }

    public void send(CommandSender sender, String path) {
        send(sender, path, placeholders());
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(render(languageService.getString("prefix") + languageService.getString(path), placeholders));
    }

    public void sendList(CommandSender sender, String path, Map<String, String> placeholders) {
        List<String> lines = languageService.getStringList(path);
        for (String line : lines) {
            sender.sendMessage(render(line, placeholders));
        }
    }

    public void actionbar(Player player, String path, Map<String, String> placeholders) {
        if (!settings.actionbarEnabled()) {
            return;
        }
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(render(languageService.getString(path), placeholders)));
    }

    public void title(Player player, String titlePath, String subtitlePath, Map<String, String> placeholders) {
        if (!settings.titleEnabled()) {
            return;
        }
        String title = render(languageService.getString(titlePath), placeholders);
        String subtitle = settings.subtitleEnabled() ? render(languageService.getString(subtitlePath), placeholders) : "";
        player.sendTitle(title, subtitle, settings.titleFadeIn(), settings.titleStay(), settings.titleFadeOut());
    }

    public String render(String raw, Map<String, String> placeholders) {
        String replaced = applyPlaceholders(raw, placeholders);
        String cacheKey = settings.smallCaps() + "|" + replaced;
        String cached = renderCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        String normalized = normalizeLegacyHex(replaced);
        if (settings.smallCaps()) {
            normalized = toSmallCapsOutsideTags(normalized);
        }

        String rendered;
        try {
            Component component = miniMessage.deserialize(normalized);
            rendered = legacySerializer.serialize(component);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Could not parse message: " + raw);
            rendered = ChatColor.translateAlternateColorCodes('&', normalized);
        }
        renderCache.put(cacheKey, rendered);
        return rendered;
    }

    public Map<String, String> placeholders() {
        return new HashMap<>();
    }

    public Map<String, String> placeholders(String key, Object value) {
        Map<String, String> placeholders = placeholders();
        placeholders.put(key, String.valueOf(value));
        return placeholders;
    }

    public Map<String, String> placeholders(String key, Object value, String secondKey, Object secondValue) {
        Map<String, String> placeholders = placeholders(key, value);
        placeholders.put(secondKey, String.valueOf(secondValue));
        return placeholders;
    }

    public LanguageService languages() {
        return languageService;
    }

    private String applyPlaceholders(String raw, Map<String, String> placeholders) {
        String result = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private String normalizeLegacyHex(String message) {
        Matcher matcher = LEGACY_HEX.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String toSmallCapsOutsideTags(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        boolean tag = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (current == '<') {
                tag = true;
                builder.append(current);
                continue;
            }
            if (current == '>') {
                tag = false;
                builder.append(current);
                continue;
            }
            builder.append(tag ? current : toSmallCap(current));
        }
        return builder.toString();
    }

    private char toSmallCap(char input) {
        Character mapped = SMALL_CAPS.get(Character.toLowerCase(input));
        if (mapped == null) {
            return input;
        }
        return mapped;
    }

    private static Map<Character, Character> createSmallCaps() {
        Map<Character, Character> map = new HashMap<>();
        String normal = "abcdefghijklmnopqrstuvwxyz";
        String small = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        for (int i = 0; i < normal.length(); i++) {
            map.put(normal.charAt(i), small.charAt(i));
        }
        return map;
    }
}
