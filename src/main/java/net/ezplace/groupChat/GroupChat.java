package net.ezplace.groupChat;

import net.ezplace.groupChat.commands.GroupChatCommands;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroupChat extends JavaPlugin {

    private  GroupChat instance;

    @Override
    public void onEnable() {
        instance = this;

        GroupChatCommands commandExecutor = new GroupChatCommands();
        getCommand("groupchat").setExecutor(commandExecutor);
        getCommand("groupchat").setTabCompleter(commandExecutor);

    }

    @Override
    public void onDisable() {
        getLogger().info("GroupChat has been disabled!");
    }

    public GroupChat getInstance() {
        return instance;
    }

}
