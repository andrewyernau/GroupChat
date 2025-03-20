package net.ezplace.groupChat.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public static class Group {
        private String name;
        private ChatColor color;
        private String prefix;
        private Set<UUID> members;
        private int maxSize;

        public Group(String name, String prefix, ChatColor color, int maxSize) {
            this.name = name;
            this.prefix=prefix;
            this.color=color;
            this.maxSize=maxSize;
        }

        public String getPrefix(){
            return this.prefix;
        }
        // Constructor y métodos de gestión de miembros
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

    public void createGroup(String name, String prefix, ChatColor color, int maxSize) {
        Group group = new Group(name, prefix, color, maxSize);
        groups.put(name.toLowerCase(), group);
    }

    public void addPlayerToGroup(Player player, String groupName) {
        // Lógica para añadir jugador al grupo
    }

    public void setDefaultGroup(Player player, String groupName) {
        // Establecer grupo activo
    }

    public boolean hasGroup(Player player, String groupName){
        // Comprobar si usuario esta en un grupo
        return false;
    }

    public String getActiveGroup(Player player){
        String activeGroup = "";
        //Logica para devolver el nombre del grupo activo
        return activeGroup;
    }

    public boolean isGroupChatActive(Player player){
        //Logica para comprobar si el chat esta activo
        return false;
    }

    public Group getGroup(String group){
        return groups.get(group);
    }
}