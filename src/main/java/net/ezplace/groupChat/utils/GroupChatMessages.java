package net.ezplace.groupChat.utils;

import net.ezplace.groupChat.GroupChat;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GroupChatMessages {
    private static GroupChatMessages instance;
    private YamlConfiguration messages;
    private Map<String, String> messageCache = new HashMap<>();
    private static File file;
    private static YamlConfiguration config;
    private static String LANGUAGE;

    public GroupChatMessages() {
        instance = this;
    }

    public static GroupChatMessages getInstance() {
        if (instance == null) {
            instance = new GroupChatMessages();
            instance.loadConfig();
        }
        return instance;
    }

    private void loadConfig() {
        config = new YamlConfiguration();
        config.options().parseComments(true);

        File configFile = new File(GroupChat.getInstance().getDataFolder(), "config.yml"); // Assuming config is in config.yml
        if (!configFile.exists()) {
            GroupChat.getInstance().saveResource("config.yml", false); // Save default config
        }

        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LANGUAGE = config.getString("lang", "en");
    }


    public void loadMessages() {

        File langFolder = new File(GroupChat.getInstance().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        file = new File(langFolder, LANGUAGE + ".yml");

        if (!file.exists()) {
            GroupChat.getInstance().saveResource("lang/" + "en.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
        messageCache.clear();

        messages.getKeys(true).forEach(key -> messageCache.put(key, messages.getString(key, key)));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messageCache.getOrDefault(key, key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message.replace("&", "§");
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }
}
