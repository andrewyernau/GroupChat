package net.ezplace.groupChat.core;

import net.ezplace.groupChat.GroupChat;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, CachedTranslation> cache = new ConcurrentHashMap<>();
    private final GroupChat plugin;

    public CacheManager(GroupChat plugin) {
        this.plugin = plugin;
        scheduler.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.HOURS);
    }

    private void cleanupCache() {
        cache.entrySet().removeIf(entry ->
                System.currentTimeMillis() > entry.getValue().getExpirationTime()
        );
    }

    public void cacheTranslation(String templateHash, String translatedText) {
        long ttl = plugin.getConfig().getLong("translation.cache_ttl", 3600) * 1000;
        cache.put(templateHash, new CachedTranslation(
                translatedText,
                System.currentTimeMillis() + ttl
        ));
    }

    public Optional<String> getCachedTranslation(String templateHash) {
        CachedTranslation entry = cache.get(templateHash);
        if (entry != null && entry.getExpirationTime() > System.currentTimeMillis()) {
            return Optional.of(entry.getTranslatedText());
        }
        return Optional.empty();
    }

    public static class CachedTranslation {
        private final String translatedText;
        private final long expirationTime;

        public CachedTranslation(String translatedText, long expirationTime) {
            this.translatedText = translatedText;
            this.expirationTime = expirationTime;
        }

        // Getters
        public String getTranslatedText() {
            return translatedText;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
