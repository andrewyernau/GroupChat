package net.ezplace.groupChat.commands;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupChatCommands implements CommandExecutor, TabCompleter {
    private final GroupChat plugin;

    public GroupChatCommands(GroupChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores");
            return true;
        }

        Player player = (Player) sender;
        GroupManager manager = plugin.getGroupManager();

        if (args.length < 1) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /groupchat join <grupo>");
                    return true;
                }
                manager.addPlayerToGroup(player, args[1]);
                break;

            case "leave":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /groupchat leave <grupo>");
                    return true;
                }
                manager.removePlayerFromGroup(player, args[1]);
                break;

            case "list":
                listGroups(player);
                break;

            case "translate":
                toggleTranslation(player);
                break;

            case "help":
                showHelp(player);
                break;

            default:
                String groupName = subCommand;
                if (manager.hasGroup(player, groupName)) {
                    manager.setDefaultGroup(player, groupName);
                    player.sendMessage(ChatColor.GREEN + "Chat de grupo establecido a: " + groupName);
                } else {
                    player.sendMessage(ChatColor.RED + "No perteneces al grupo: " + groupName);
                }
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== GroupChat Ayuda ===");
        player.sendMessage(ChatColor.YELLOW + "/groupchat <grupo> - Activa el chat para un grupo");
        player.sendMessage(ChatColor.YELLOW + "/groupchat join <grupo> - Unirse a un grupo");
        player.sendMessage(ChatColor.YELLOW + "/groupchat leave <grupo> - Abandonar un grupo");
        player.sendMessage(ChatColor.YELLOW + "/groupchat list - Listar grupos disponibles");
        player.sendMessage(ChatColor.YELLOW + "/groupchat translate - Activar/desactivar traducción automática");
        player.sendMessage(ChatColor.YELLOW + "/groupchat help - Mostrar esta ayuda");
    }

    private void listGroups(Player player) {
        GroupManager manager = plugin.getGroupManager();
        player.sendMessage(ChatColor.GOLD + "=== Grupos disponibles ===");

        for (GroupManager.Group group : manager.getAllGroups()) {
            boolean isMember = manager.hasGroup(player, group.getName());
            String status = isMember ? ChatColor.GREEN + "[Miembro]" : ChatColor.GRAY + "";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    group.getPrefix() + " " + status));
        }
    }

    private void toggleTranslation(Player player) {
        UUID uuid = player.getUniqueId();
        GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(uuid);

        if (data != null) {
            boolean newStatus = !data.isAutoTranslate();
            data.setAutoTranslate(newStatus);

            if (newStatus) {
                player.sendMessage(ChatColor.GREEN + "Traducción automática activada");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Traducción automática desactivada");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("join");
            options.add("leave");
            options.add("list");
            options.add("translate");
            options.add("help");
            GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(player.getUniqueId());
            if (data != null) {
                options.addAll(data.getJoinedGroups());
            }

            return options.stream()
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("join")) {
                return plugin.getGroupManager().getAllGroups().stream()
                        .map(GroupManager.Group::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (subCommand.equals("leave")) {
                GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(player.getUniqueId());
                if (data != null) {
                    return data.getJoinedGroups().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        return Collections.emptyList();
    }
}
