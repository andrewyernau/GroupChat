package net.ezplace.groupChat.core;

import com.deepl.api.DeepLException;

import net.ezplace.groupChat.GroupChat;
import net.ezplace.groupChat.utils.TranslationAPI;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class TranslationManager {
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%\\w+%|\\{[^}]+}"); // Detecta %placeholder% y {placeholder}
    private final TranslationAPI translateAPI;// Añadir esta propiedad
    private final GroupChat plugin;

    // Inyección de dependencias a través del constructor
    public TranslationManager(GroupChat plugin,TranslationAPI translateAPI) {
        this.plugin = plugin;
        this.translateAPI = translateAPI;
    }

    public String translateIfNeeded(Player receiver, String originalText) {
        // Si el jugador no tiene traducción activa, devolver original
        if (!shouldTranslate(receiver)) return originalText;

        // Identificar plantilla sin valores dinámicos
        String template = extractTemplate(originalText);
        String targetLang = getPlayerLanguage(receiver);
        String cacheKey = generateCacheKey(template, targetLang);

        // Buscar en caché
        String translatedTemplate = translationCache.computeIfAbsent(cacheKey, k -> {
            String translated = null;
            try {
                translated = translateAPI.translate(template, targetLang);
            } catch (DeepLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return postProcessTranslated(translated); // Mantiene placeholders
        });

        // Reinsertar valores dinámicos
        return applyDynamicValues(originalText, translatedTemplate);
    }

    // El resto de tu código...

    public boolean shouldTranslate(Player receiver){
        //logica para ver si el usuario quiere que se le traduzca
        return false;
    }

    public String getPlayerLanguage(Player receiver){
        //logica para obtener el grupo del jugador (language)
        return null;
    }

    public String generateCacheKey(String template, String targetLang){
        return template + ":" + targetLang;
    }

    public String postProcessTranslated(String translated){
        // lógica para procesar la traducción si es necesario
        return translated;
    }

    public String extractTemplate(String originalText){
        return null;
    }

    public String applyDynamicValues(String originalText,String translatedTemplate){
        return null;
    }
}
