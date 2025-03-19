package net.ezplace.groupChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class GroupChatCommands implements CommandExecutor, TabCompleter {

    GroupChatCommands instance;

    public GroupChatCommands() {
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }

    public GroupChatCommands getInstance() {
        return instance;
    }
}
