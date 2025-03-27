package net.ezplace.groupChat.commands;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import org.bukkit.Bukkit;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores");
            return true;
        }

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

                String groupName = args[1];
                GroupManager.Group group = manager.getGroup(groupName);

                if (group == null) {
                    player.sendMessage(ChatColor.RED + "El grupo " + groupName + " no existe.");
                    return true;
                }

                if (group.isPrivate()) {
                    // Joining private group requires invite or bypass permission
                    if (player.hasPermission("groupchat.bypass")) {
                        manager.addPlayerToGroup(player, groupName);
                    } else {
                        player.sendMessage(ChatColor.RED + "No puedes unirte a este grupo.");
                        return true; // Important: Stop further execution
                    }
                } else {
                    // Public group, allow joining
                    manager.addPlayerToGroup(player, groupName);
                }
                return true;

            case "leave":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /groupchat leave <grupo>");
                    return true;
                }
                manager.removePlayerFromGroup(player, args[1]);
                return true;

            case "list":
                listGroups(player);
                return true;

            case "translate":
                toggleTranslation(player);
                return true;

            case "help":
                showHelp(player);
                return true;
            case "global":
                manager.setDefaultGroup(player, null);
                return true;
            case "invite":
                if (args.length == 2) {
                    String activeGroup = manager.getActiveGroup(player);
                    if (activeGroup == null || activeGroup.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "¡Primero selecciona un grupo con /groupchat <grupo>!");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        plugin.getInvitationManager().createInvite(player, target, activeGroup);
                    }
                    return true;
                }else {
                    player.sendMessage(ChatColor.RED + "Uso: /groupchat invite <jugador>");
                    return true;
                }
            case "accept":
                plugin.getInvitationManager().acceptInvite((Player)sender,
                        plugin.getGroupManager().getActiveGroup((Player)sender));
                return true;

            default:
                if (manager.hasGroup(player, subCommand)) {
                    manager.setDefaultGroup(player, subCommand);
                } else {
                    player.sendMessage(ChatColor.RED + "No perteneces al grupo: " + subCommand);
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
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("join");
            options.add("leave");
            options.add("list");
            options.add("translate");
            options.add("help");
            options.add("accept");
            GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(player.getUniqueId());
            if (data != null) {
                options.addAll(data.getJoinedGroups());
                options.add("global");
                options.add("invite");
            }

            return options.stream()
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("join")) {
                return plugin.getGroupManager().getAllGroups().stream()
                        .filter(
                                group ->
                                        !group.isPrivate()
                                                || player.hasPermission("groupchat.bypass")
                                                || isInvited(player, group.getName()))
                        .map(GroupManager.Group::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (subCommand.equals("invite")){
                List<String> completions;
                completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                return completions;
            }

            if (subCommand.equals("leave")) {
                GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(player.getUniqueId());
                if (data != null) {
                    return data.getJoinedGroups().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }

            if (subCommand.equals("accept")) {
                // Return a list of groups the player has pending invites for
                return getPendingInviteGroups(player).stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
    private boolean isInvited(Player player, String groupName) {
        UUID playerUUID = player.getUniqueId();
        if (plugin.getInvitationManager().getPendingInvites().containsKey(playerUUID)) {
            return plugin.getInvitationManager().getPendingInvites().get(playerUUID).containsKey(groupName.toLowerCase());
        }
        return false;
    }

    private List<String> getPendingInviteGroups(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (plugin.getInvitationManager().getPendingInvites().containsKey(playerUUID)) {
            return new ArrayList<>(plugin.getInvitationManager().getPendingInvites().get(playerUUID).keySet());
        }
        return Collections.emptyList();
    }
}
