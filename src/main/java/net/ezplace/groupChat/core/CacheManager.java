//package net.ezplace.groupChat.core;
//
//import net.ezplace.groupChat.GroupChat;
//import org.bukkit.Bukkit;
//
//import java.sql.*;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//
//public class CacheManager {
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private final Map<String, CachedTranslation> cache = new ConcurrentHashMap<>();
//    private final GroupChat plugin;
//    private final DatabaseManager db;
//
//    public CacheManager(GroupChat plugin) {
//        this.plugin = plugin;
//        this.db = new DatabaseManager(plugin);
//        scheduler.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.HOURS);
//        scheduler.scheduleAtFixedRate(this::cleanupDatabaseCache, 24, 24, TimeUnit.HOURS);
//    }
//
//    private void cleanupCache() {
//        cache.entrySet().removeIf(entry ->
//                System.currentTimeMillis() > entry.getValue().getExpirationTime()
//        );
//    }
//
//    public void cacheTranslation(String templateHash, String translatedText) {
//        long ttl = plugin.getConfig().getLong("translation.cache_ttl", 3600) * 1000;
//        CachedTranslation entry = new CachedTranslation(
//                translatedText,
//                System.currentTimeMillis() + ttl
//        );
//
//        // Almacenar en memoria
//        cache.put(templateHash, entry);
//
//        // Almacenar en BD async
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//            db.cacheTranslation(templateHash, translatedText);
//        });
//    }
//
//    private void cleanupDatabaseCache() {
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//            db.cleanupOldEntries(30); // Limpiar entradas mayores a 30 días
//        });
//    }
//
//    public Optional<String> getCachedTranslation(String templateHash) {
//        CachedTranslation entry = cache.get(templateHash);
//        if (entry != null && entry.getExpirationTime() > System.currentTimeMillis()) {
//            return Optional.of(entry.getTranslatedText());
//        }
//        return Optional.empty();
//    }
//    public DatabaseManager getDatabase(){
//        return db;
//    }
//
//    public static class CachedTranslation {
//        private final String translatedText;
//        private final long expirationTime;
//
//        public CachedTranslation(String translatedText, long expirationTime) {
//            this.translatedText = translatedText;
//            this.expirationTime = expirationTime;
//        }
//
//        // Getters
//        public String getTranslatedText() {
//            return translatedText;
//        }
//
//        public long getExpirationTime() {
//            return expirationTime;
//        }
//
//    }
//
//    public class DatabaseManager {
//        private final GroupChat plugin;
//        private Connection connection;
//
//        public DatabaseManager(GroupChat plugin) {
//            this.plugin = plugin;
//            initialize();
//        }
//
//        private void initialize() {
//            try {
//                Class.forName("org.sqlite.JDBC");
//                connection = DriverManager.getConnection(
//                        "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/translations.db"
//                );
//
//                try (Statement stmt = connection.createStatement()) {
//                    stmt.executeUpdate(
//                            "CREATE TABLE IF NOT EXISTS translations (" +
//                                    "hash TEXT PRIMARY KEY," +
//                                    "translated_text TEXT NOT NULL," +
//                                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
//                                    ");"
//                    );
//                }
//            } catch (Exception e) {
//                plugin.getLogger().log(Level.SEVERE, "Error inicializando base de datos", e);
//            }
//        }
//
//        public void cacheTranslation(String hash, String translatedText) {
//            String sql = "INSERT OR REPLACE INTO translations (hash, translated_text) VALUES (?, ?)";
//
//            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//                pstmt.setString(1, hash);
//                pstmt.setString(2, translatedText);
//                pstmt.executeUpdate();
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.SEVERE, "Error guardando traducción", e);
//            }
//        }
//
//        public String getCachedTranslation(String hash) {
//            String sql = "SELECT translated_text FROM translations WHERE hash = ?";
//
//            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//                pstmt.setString(1, hash);
//                ResultSet rs = pstmt.executeQuery();
//
//                if (rs.next()) {
//                    return rs.getString("translated_text");
//                }
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.SEVERE, "Error obteniendo traducción", e);
//            }
//            return null;
//        }
//
//        public void cleanupOldEntries(int days) {
//            String sql = "DELETE FROM translations WHERE DATE(created_at) < DATE('now', '-' || ? || ' days')";
//
//            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//                pstmt.setInt(1, days);
//                pstmt.executeUpdate();
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.SEVERE, "Error limpiando traducciones", e);
//            }
//        }
//
//        public void close() {
//            try {
//                if (connection != null && !connection.isClosed()) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.SEVERE, "Error cerrando conexión", e);
//            }
//        }
//    }
//}
