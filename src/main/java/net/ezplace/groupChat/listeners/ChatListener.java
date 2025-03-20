package net.ezplace.groupChat.listeners;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.core.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    GroupChat plugin = GroupChat.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GroupManager manager = plugin.getGroupManager();

        if (manager.isGroupChatActive(player)) {
            event.setCancelled(true);
            String groupName = manager.getActiveGroup(player);
            String formatted = formatGroupMessage(player, event.getMessage(), groupName);

            // Multicast to group
            sendToGroup(groupName, formatted);
        }
    }

    private String formatGroupMessage(Player player, String message, String group) {
        GroupManager.Group g = plugin.getGroupManager().getGroup(group);
        return String.format("%s%s: %s",
                g.getPrefix(),
                player.getName(),
                ChatColor.translateAlternateColorCodes('&', message)
        );
    }

    public void sendToGroup(String groupName, String formatted){

    }
}