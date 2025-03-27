package net.ezplace.groupChat.core;

import com.deepl.api.DeepLException;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.utils.TranslationAPI;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationManager {
    private final GroupChat plugin;
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%\\w+%|\\{[^}]+}");
    private final TranslationAPI translateAPI;
    private final CacheManager cacheManager;
    private final Set<String> excludedPlaceholders;

    public TranslationManager(GroupChat plugin, TranslationAPI translateAPI) {
        this.plugin = plugin;
        this.translateAPI = translateAPI;
        this.cacheManager = new CacheManager(plugin);

        // Cargar placeholders excluidos
        excludedPlaceholders = new HashSet<>();
        List<String> configExcluded = plugin.getConfig().getStringList("translation.excluded_placeholders");
        if (configExcluded != null) {
            excludedPlaceholders.addAll(configExcluded);
        }
    }

    public String translateIfNeeded(Player receiver, String originalText) {
        if (!shouldTranslate(receiver)) return originalText;


        String template = extractTemplate(originalText);
        String targetLang = getPlayerLanguage(receiver);
        String cacheKey = generateCacheKey(template, targetLang);

        // Intentar obtener de caché
        Optional<String> cached = cacheManager.getCachedTranslation(cacheKey);
        if (cached.isPresent()) {
            return applyDynamicValues(originalText, cached.get());
        }
        GroupManager.PlayerData data = plugin.getGroupManager().getPlayerData(receiver.getUniqueId());
        GroupManager.Group group = plugin.getGroupManager().getGroup(data.getActiveGroup());
        if (group != null && group.shouldTranslate()) {
            return handleGroupTranslation(originalText, group.getTranslateLang());
        }
        // Traducir si no está en caché
        try {
            plugin.getLogger().info(template+ " MENSAJE A TRADUCIR"); //DEBUG
            String translatedTemplate = translateAPI.translate(template, targetLang);
            plugin.getLogger().info(translatedTemplate+ " MENSAJE DEVUELTO?"); //DEBUG
            translatedTemplate = postProcessTranslated(translatedTemplate);

            // Guardar en caché
            cacheManager.cacheTranslation(cacheKey, translatedTemplate);

            // Aplicar valores dinámicos
            return applyDynamicValues(originalText, translatedTemplate);
        } catch (Exception e) {
            plugin.getLogger().warning("Error al traducir: " + e.getMessage());
            return originalText;
        }
    }

    private String handleGroupTranslation(String text, String lang) {
        String template = extractTemplate(text);
        String cacheKey = generateCacheKey(template, lang);

        return cacheManager.getCachedTranslation(cacheKey)
                .map(translated -> applyDynamicValues(text, translated))
                .orElseGet(() -> {
                    String translated = null;
                    try {
                        translated = translateAPI.translate(template, lang);
                    } catch (DeepLException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    cacheManager.cacheTranslation(cacheKey, translated);
                    return applyDynamicValues(text, translated);
                });
    }


    public boolean shouldTranslate(Player receiver) {
        String groupName = plugin.getGroupManager().getActiveGroup(receiver);
        //I'm not adding support language to english translation since it is expected that all consoles
        //write messages in english
        if (Objects.equals(groupName, "en")) return false;

        GroupManager.Group group = plugin.getGroupManager().getGroup(groupName);
        return group != null &&
                group.shouldTranslate() &&
                plugin.getGroupManager().getPlayerData(receiver.getUniqueId()).isAutoTranslate();
    }

    public String getPlayerLanguage(Player receiver) {
        String groupName = plugin.getGroupManager().getActiveGroup(receiver);
        if (groupName == null) return "es";

        GroupManager.Group group = plugin.getGroupManager().getGroup(groupName);
        return group != null ? group.getTranslateLang() : "es";
    }

    public String generateCacheKey(String template, String targetLang) {
        return template.hashCode() + ":" + targetLang;
    }

    public String extractTemplate(String originalText) {
        if (!plugin.getConfig().getBoolean("translation.placeholder_handling", true)) {
            return originalText;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(originalText);

        while (matcher.find()) {
            String placeholder = matcher.group();
            if (excludedPlaceholders.contains(placeholder)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement("__PH" + placeholder.hashCode() + "__"));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public String postProcessTranslated(String translated) {
        // Normalizar resultado (puede ser necesario para algunos servicios de traducción)
        return translated.trim();
    }

    public String applyDynamicValues(String originalText, String translatedTemplate) {
        if (!plugin.getConfig().getBoolean("translation.placeholder_handling", true)) {
            return translatedTemplate;
        }

        // Obtener todos los placeholders del texto original
        Matcher originalMatcher = PLACEHOLDER_PATTERN.matcher(originalText);
        List<String> originalPlaceholders = new ArrayList<>();

        while (originalMatcher.find()) {
            originalPlaceholders.add(originalMatcher.group());
        }

        // Buscar marcadores en el texto traducido y reemplazarlos con los placeholders originales
        String result = translatedTemplate;
        for (String placeholder : originalPlaceholders) {
            String marker = "__PH" + placeholder.hashCode() + "__";
            result = result.replace(marker, placeholder);
        }

        return result;
    }
}
