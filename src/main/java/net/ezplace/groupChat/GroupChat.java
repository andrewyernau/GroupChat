package net.ezplace.groupChat;

import net.ezplace.groupChat.commands.GroupChatCommands;
import net.ezplace.groupChat.core.GroupManager;
import net.ezplace.groupChat.core.InvitationManager;
import net.ezplace.groupChat.core.TranslationManager;
import net.ezplace.groupChat.listeners.ChatListener;
import net.ezplace.groupChat.listeners.PacketListener;
import net.ezplace.groupChat.utils.DeeplTranslationAPI;
import net.ezplace.groupChat.utils.GoogleTranslationAPI;
import net.ezplace.groupChat.utils.TranslationAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroupChat extends JavaPlugin {

    private static GroupChat instance;
    private GroupManager groupManager;
    private TranslationManager translationManager;
    private PacketListener packetListener;
    private ChatListener chatListener;
    private InvitationManager invitationManager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        instance = this;
        saveDefaultConfig();
        groupManager = new GroupManager(this);

        groupManager.loadGroups();
        groupManager.loadData();
        invitationManager = new InvitationManager(this);


        GroupChatCommands commandExecutor = new GroupChatCommands(this);
        getCommand("groupchat").setExecutor(commandExecutor);
        getCommand("groupchat").setTabCompleter(commandExecutor);

        TranslationAPI translationAPI;
        String translationProvider = getConfig().getString("translation.provider", "google");
        String apiKey = getConfig().getString("translation.apiKey", "");

        if (translationProvider.equalsIgnoreCase("deepl")) {
            translationAPI = new DeeplTranslationAPI(apiKey);
        } else {
            translationAPI = new GoogleTranslationAPI(apiKey);
        }

        translationManager = new TranslationManager(this, translationAPI);

        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        // Registrar interceptor de paquetes (si ProtocolLib está disponible)
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            packetListener = new PacketListener(this);
            packetListener.register();
            getLogger().info("ProtocolLib encontrado, interceptación de paquetes activada");
        } else {
            getLogger().warning("ProtocolLib no encontrado, la traducción automática de mensajes del servidor no funcionará");
        }

        getLogger().info("GroupChat ha sido habilitado!");
    }

    @Override
    public void onDisable() {
        if (groupManager != null) {
            saveAllData();
        }
        getLogger().info("GroupChat ha sido deshabilitado!");
    }

    public static GroupChat getInstance() {
        return instance;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public TranslationManager getTranslationManager() {
        return translationManager;
    }

    public InvitationManager getInvitationManager(){
        return invitationManager;
    }

    private void saveAllData() {
        getGroupManager().saveData();
    }

    private record PlayerQuitListener(GroupChat plugin) implements Listener {
        @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                plugin.getGroupManager().saveData();
            }
        }
}
