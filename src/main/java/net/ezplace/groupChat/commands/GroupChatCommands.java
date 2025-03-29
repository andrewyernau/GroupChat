package net.ezplace.groupChat.commands;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import net.ezplace.groupChat.utils.GroupChatMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GroupChatCommands implements CommandExecutor, TabCompleter {
    private final GroupChat plugin;
    private final GroupManager manager;

    public GroupChatCommands(GroupChat plugin, GroupManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GroupChatMessages.getInstance().getMessage("command.onlyplayers"));
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
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("command.usage.join"));
                    return true;
                }

                String groupName = args[1];
                GroupManager.Group group = manager.getGroup(groupName);

                if (group == null) {
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("error.group.notexist", Map.of("group",groupName)));
                    return true;
                }

                if (group.isPrivate()) {
                    // Joining private group requires invite or bypass permission
                    if (player.hasPermission("groupchat.bypass")) {
                        manager.addPlayerToGroup(player, groupName);
                    } else {
                        player.sendMessage(GroupChatMessages.getInstance().getMessage("error.group.cantjoin"));
                        return true;
                    }
                } else {
                    // Public group, allow joining
                    manager.addPlayerToGroup(player, groupName);
                }
                return true;

            case "leave":
                if (args.length < 2) {
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("command.usage.leave"));
                    return true;
                }
                manager.removePlayerFromGroup(player, args[1]);
                return true;

            case "list":
                listGroups(player);
                return true;

//            case "translate":
//                toggleTranslation(player);
//                return true;

            case "help":
                showHelp(player);
                return true;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("command.usage.info"));
                    return true;
                }
                showGroupInfo(player, args[1]);
                return true;
            case "global":
                manager.setDefaultGroup(player, null);
                return true;
            case "invite":
                if (args.length == 2) {
                    String activeGroup = manager.getActiveGroup(player);
                    if (activeGroup == null || activeGroup.isEmpty()) {
                        player.sendMessage(GroupChatMessages.getInstance().getMessage("error.select.groupfirst"));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        plugin.getInvitationManager().createInvite(player, target, activeGroup);
                    }
                    return true;
                }else {
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("command.usage.invite"));
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
                    player.sendMessage(GroupChatMessages.getInstance().getMessage("command.usage.invite",Map.of("group",subCommand)));
                }
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help1"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help2"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help3"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help4"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help5"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help6"));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help7"));
    }

    private void showGroupInfo(Player player, String groupName) {
        GroupManager.Group group = manager.getGroup(groupName);
        if (group == null) {
            player.sendMessage(GroupChatMessages.getInstance().getMessage("error.group.invalid"));
            return;
        }

        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help7",Map.of("group",group.getName())));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.help7",Map.of("members",String.valueOf(group.getMembers().size()))) +
                (group.getMaxSize() > 0 ? "/" + group.getMaxSize() : GroupChatMessages.getInstance().getMessage("command.info.unlimited")));
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.info.prefix") + ChatColor.translateAlternateColorCodes('&', group.getPrefix()));
    }

    private void listGroups(Player player) {
        GroupManager manager = plugin.getGroupManager();
        player.sendMessage(GroupChatMessages.getInstance().getMessage("command.groups.header"));

        for (GroupManager.Group group : manager.getAllGroups()) {
            boolean isMember = manager.hasGroup(player, group.getName());
            String status = isMember ? ChatColor.GREEN + GroupChatMessages.getInstance().getMessage("command.groups.member") : ChatColor.GRAY + "";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    group.getPrefix() + " " + status));
        }
    }

//    private void toggleTranslation(Player player) {
//        UUID uuid = player.getUniqueId();
//        GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(uuid);
//
//        if (data != null) {
//            boolean newStatus = !data.isAutoTranslate();
//            data.setAutoTranslate(newStatus);
//
//            if (newStatus) {
//                player.sendMessage(ChatColor.GREEN + "Traducción automática activada");
//            } else {
//                player.sendMessage(ChatColor.YELLOW + "Traducción automática desactivada");
//            }
//        }
//    }

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
//            options.add("translate");
            options.add("info");
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

            if (subCommand.equals("join") || subCommand.equals("info")) {
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
