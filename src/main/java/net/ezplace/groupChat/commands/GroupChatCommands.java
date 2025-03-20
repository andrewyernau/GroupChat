package net.ezplace.groupChat.commands;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class GroupChatCommands implements CommandExecutor, TabCompleter {
    GroupChat plugin;
    GroupChatCommands instance;

    public GroupChatCommands() {
        plugin = GroupChat.getInstance();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        GroupManager manager = GroupChat.getGroupManager();

        if (args.length == 1) {
            String groupName = args[0].toLowerCase();
            if (manager.hasGroup(player, groupName)) {
                manager.setDefaultGroup(player, groupName);
                player.sendMessage(ChatColor.GREEN + "Chat de grupo establecido a: " + groupName);
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }

    public GroupChatCommands getInstance() {
        return instance;
    }
}
