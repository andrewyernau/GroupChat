package net.ezplace.groupChat;

import net.ezplace.groupChat.commands.GroupChatCommands;
import net.ezplace.groupChat.core.GroupManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroupChat extends JavaPlugin {

    private static GroupChat instance;
    private static GroupManager groupManager;

    @Override
    public void onEnable() {
        instance = this;

        GroupChatCommands commandExecutor = new GroupChatCommands();
        getCommand("groupchat").setExecutor(commandExecutor);
        getCommand("groupchat").setTabCompleter(commandExecutor);
        groupManager = new GroupManager();
    }

    @Override
    public void onDisable() {
        getLogger().info("GroupChat has been disabled!");
    }

    public static GroupChat getInstance() {
        return instance;
    }

    public static GroupManager getGroupManager(){
        return groupManager;
    }

}
