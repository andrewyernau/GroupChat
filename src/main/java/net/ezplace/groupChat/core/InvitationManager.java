package net.ezplace.groupChat.core;

import net.ezplace.groupChat.GroupChat;
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
            throw new IllegalArgumentException("Nombre de grupo no puede ser nulo");
        }

        pendingInvites
                .computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(groupName.toLowerCase(), inviter.getUniqueId());

        target.sendMessage(ChatColor.GOLD + "Invitación al grupo " + groupName +
                " recibida de " + inviter.getName() + ". Usa /groupchat accept " + groupName);
    }

    public boolean acceptInvite(Player player, String groupName) {
        if (groupName == null) {
            player.sendMessage(ChatColor.RED + "Grupo inválido");
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
