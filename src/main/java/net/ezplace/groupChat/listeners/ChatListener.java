package net.ezplace.groupChat.listeners;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import net.ezplace.groupChat.utils.GroupChatMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class ChatListener implements Listener {
    private final GroupChat plugin;

    public ChatListener(GroupChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Ability to write in global temporally (1 message)
        if (!message.isEmpty() && message.charAt(0) == '!') {
            String globalMessage = message.substring(1).trim();

            if (globalMessage.isEmpty()) {
                event.setCancelled(true);
                player.sendMessage(GroupChatMessages.getInstance().getMessage("error.message.empty"));
                return;
            }
            event.setMessage(globalMessage);
            return;
        }

        GroupManager manager = plugin.getGroupManager();

        if (manager.isGroupChatActive(player)) {
            event.setCancelled(true);
            String groupName = manager.getActiveGroup(player);
            String formatted = formatGroupMessage(player, message, groupName);
            sendToGroup(groupName, formatted);
        }
    }

    private String formatGroupMessage(Player player, String message, String groupName) {
        GroupManager.Group group = plugin.getGroupManager().getGroup(groupName);
        if (group == null) return message;

        return ChatColor.translateAlternateColorCodes('&',
                group.getPrefix() + player.getName() + ": " + message);
    }

    public void sendToGroup(String groupName, String formatted) {
        GroupManager.Group group = plugin.getGroupManager().getGroup(groupName);
        if (group == null) return;

        // Multicast
        for (UUID memberId : group.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(formatted);
            }
        }

        // Registrar en consola
        plugin.getLogger().info("[Grupo: " + groupName + "] " + ChatColor.stripColor(formatted));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getGroupManager().getPlayerData(player.getUniqueId());
    }
}