package net.ezplace.groupChat.core;

import net.ezplace.groupChat.GroupChat;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final GroupChat plugin;

    public static class Group {
        private String name;
        private ChatColor color;
        private String prefix;
        private Set<UUID> members = new HashSet<>();
        private int maxSize;

        public Group(String name, String prefix, ChatColor color, int maxSize) {
            this.name = name;
            this.prefix = prefix;
            this.color = color;
            this.maxSize = maxSize;
        }

        public String getName() {
            return name;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public boolean addMember(UUID uuid) {
            if (members.size() >= maxSize) {
                return false;
            }
            return members.add(uuid);
        }

        public boolean removeMember(UUID uuid) {
            return members.remove(uuid);
        }

        public boolean hasMember(UUID uuid) {
            return members.contains(uuid);
        }

        public Set<UUID> getMembers() {
            return Collections.unmodifiableSet(members);
        }

        public int getMaxSize() {
            return maxSize;
        }
    }

    public static class PlayerData {
        private Set<String> joinedGroups = new HashSet<>();
        private String activeGroup;
        private boolean autoTranslate;

        // Getters/Setters

        public Set<String> getJoinedGroups() {
            return joinedGroups;
        }

        public void setJoinedGroups(Set<String> joinedGroups) {
            this.joinedGroups = joinedGroups;
        }

        public String getActiveGroup() {
            return activeGroup;
        }

        public void setActiveGroup(String activeGroup) {
            this.activeGroup = activeGroup;
        }

        public boolean isAutoTranslate() {
            return autoTranslate;
        }

        public void setAutoTranslate(boolean autoTranslate) {
            this.autoTranslate = autoTranslate;
        }
    }

    public GroupManager(GroupChat plugin) {
        this.plugin = plugin;
        loadGroups();
    }

    private void loadGroups() {
        ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("groups");
        if (groupSection != null) {
            for (String groupName : groupSection.getKeys(false)) {
                String prefix = groupSection.getString(groupName + ".prefix", "&7[" + groupName + "] ");
                ChatColor color = ChatColor.valueOf(groupSection.getString(groupName + ".color", "WHITE"));
                int maxMembers = groupSection.getInt(groupName + ".max-members", 50);

                createGroup(groupName, prefix, color, maxMembers);
                plugin.getLogger().info("Grupo cargado: " + groupName);
            }
        }
    }

    public void createGroup(String name, String prefix, ChatColor color, int maxSize) {
        Group group = new Group(name, prefix, color, maxSize);
        groups.put(name.toLowerCase(), group);
    }

    public void removePlayerFromGroup(Player player, String groupName) {
        UUID uuid = player.getUniqueId();
        Group group = groups.get(groupName.toLowerCase());

        if (group == null) {
            return;
        }

        PlayerData data = players.get(uuid);
        if (data != null) {
            group.removeMember(uuid);
            data.getJoinedGroups().remove(groupName.toLowerCase());

            // Si el grupo activo era el que abandonó, resetear
            if (groupName.equalsIgnoreCase(data.getActiveGroup())) {
                data.setActiveGroup(null);
            }

            player.sendMessage(ChatColor.YELLOW + "Has abandonado el grupo: " + group.getPrefix());
        }
    }

    public void addPlayerToGroup(Player player, String groupName) {
        UUID uuid = player.getUniqueId();
        Group group = groups.get(groupName.toLowerCase());

        if (group == null) {
            return;
        }

        PlayerData data = players.computeIfAbsent(uuid, k -> new PlayerData());
        if (group.addMember(uuid)) {
            data.getJoinedGroups().add(groupName.toLowerCase());
            player.sendMessage(ChatColor.GREEN + "Te has unido al grupo: " + group.getPrefix());
        } else {
            player.sendMessage(ChatColor.RED + "El grupo está lleno");
        }
    }

    public void setDefaultGroup(Player player, @Nullable String groupName) {
        UUID uuid = player.getUniqueId();
        PlayerData data = players.get(uuid);

        if (data == null) return;

        if (groupName == null || groupName.isEmpty()) {
            data.setActiveGroup(null);
            player.sendMessage(ChatColor.YELLOW + "Chat global establecido como predeterminado");
        } else {
            String lowerGroup = groupName.toLowerCase();
            if (data.getJoinedGroups().contains(lowerGroup)) {
                data.setActiveGroup(lowerGroup);
                player.sendMessage(ChatColor.GREEN + "Chat de grupo establecido a: " + groupName);
            }
        }
    }

    public boolean hasGroup(Player player, String groupName) {
        UUID uuid = player.getUniqueId();
        PlayerData data = players.get(uuid);
        return data != null && data.getJoinedGroups().contains(groupName.toLowerCase());
    }

    public String getActiveGroup(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = players.get(uuid);
        return data != null ? data.getActiveGroup() : null;
    }

    public boolean isGroupChatActive(Player player) {
        String activeGroup = getActiveGroup(player);
        return activeGroup != null && !activeGroup.isEmpty();
    }

    public Group getGroup(String groupName) {
        return groups.get(groupName.toLowerCase());
    }

    public Collection<Group> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return players.get(uuid);
    }

    public void saveData() {
        // Guardar datos en archivo si es necesario
    }
}