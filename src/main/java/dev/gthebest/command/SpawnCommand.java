package dev.gthebest.command;

import dev.gthebest.MoonSpawn;
import dev.gthebest.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpawnCommand implements CommandExecutor {

    private final MoonSpawn plugin;
    private final MessageService messages;

    public SpawnCommand(MoonSpawn plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messages.send(sender, "only-player");
            return true;
        }

        if (!sender.hasPermission("moonspawn.spawn")) {
            messages.send(sender, "no-permission");
            return true;
        }

        plugin.teleports().requestTeleport((Player) sender);
        return true;
    }
}
