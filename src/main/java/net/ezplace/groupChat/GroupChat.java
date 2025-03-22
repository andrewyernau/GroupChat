package net.ezplace.groupChat;

import net.ezplace.groupChat.commands.GroupChatCommands;
import net.ezplace.groupChat.core.GroupManager;
import net.ezplace.groupChat.core.TranslationManager;
import net.ezplace.groupChat.listeners.PacketListener;
import net.ezplace.groupChat.utils.DeeplTranslationAPI;
import net.ezplace.groupChat.utils.GoogleTranslationAPI;
import net.ezplace.groupChat.utils.TranslationAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroupChat extends JavaPlugin {

    private static GroupChat instance;
    private static GroupManager groupManager;
    private static TranslationManager translationManager;
    private static PacketListener packetListener;

    @Override
    public void onEnable() {
        instance = this;

        GroupChatCommands commandExecutor = new GroupChatCommands();
        getCommand("groupchat").setExecutor(commandExecutor);
        getCommand("groupchat").setTabCompleter(commandExecutor);
        groupManager = new GroupManager();
        packetListener = new PacketListener(this);

        TranslationAPI translationAPI;
        String translationProvider = getConfig().getString("translation.provider", "google");
        String apiKey = getConfig().getString("translation.apiKey", "");

        if (translationProvider.equalsIgnoreCase("deepl")) {
            translationAPI = new DeeplTranslationAPI(apiKey);
            translationManager = new TranslationManager(this,translationAPI);
        } else {
            // Default
            translationAPI = new GoogleTranslationAPI(apiKey);
            translationManager = new TranslationManager(this,translationAPI);
        }

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

    public static TranslationManager getTranslationManager(){
        return translationManager;
    }

}
