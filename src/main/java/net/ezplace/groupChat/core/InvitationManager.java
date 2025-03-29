package net.ezplace.groupChat.core;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.utils.GroupChatMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationManager {
    private GroupChat plugin;
    private final Map<UUID, Map<String, UUID>> pendingInvites = new ConcurrentHashMap<>();

    public InvitationManager(GroupChat plugin) {
        this.plugin = plugin;
    }

    public void createInvite(Player inviter, Player target, String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException(GroupChatMessages.getInstance().getMessage("error.group.null"));
        }
        String formattedPrefix = ChatColor.translateAlternateColorCodes('&', groupName);
        pendingInvites
                .computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(groupName.toLowerCase(), inviter.getUniqueId());
        target.sendMessage(GroupChatMessages.getInstance().getMessage("message.invite.receive",Map.of("group",formattedPrefix,"inviter",inviter.getName())));
    }

    public boolean acceptInvite(Player player, String groupName) {
        if (groupName == null) {
            player.sendMessage(GroupChatMessages.getInstance().getMessage("error.group.invalid"));
            return false;
        }

        Map<String, UUID> playerInvites = pendingInvites.get(player.getUniqueId());
        if (playerInvites != null) {
            UUID inviterId = playerInvites.remove(groupName.toLowerCase());
            if (inviterId != null) {
                Player inviter = Bukkit.getPlayer(inviterId);
                if (inviter != null && inviter.isOnline()) {
                    return plugin.getGroupManager().addToPrivateGroup(player, groupName, inviter);
                }
            }
        }
        return false;
    }

    public Map<UUID, Map<String, UUID>> getPendingInvites(){
        return pendingInvites;
    }
}
