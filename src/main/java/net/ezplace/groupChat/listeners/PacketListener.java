//package net.ezplace.groupChat.listeners;
//
//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.ProtocolLibrary;
//import com.comphenix.protocol.ProtocolManager;
//import com.comphenix.protocol.events.ListenerPriority;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketContainer;
//import com.comphenix.protocol.events.PacketEvent;
//import com.comphenix.protocol.wrappers.WrappedChatComponent;
//import net.ezplace.groupChat.GroupChat;
//import net.ezplace.groupChat.utils.VersionAdapter;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import java.util.UUID;
//
//public class PacketListener {
//    private final GroupChat plugin1;
//    private final VersionAdapter versionAdapter;
//
//    public PacketListener(GroupChat plugin) {
//        this.plugin1 = plugin;
//        this.versionAdapter = VersionAdapter.create();
//    }
//
//    public void register() {
//        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
//
//        protocolManager.addPacketListener(new PacketAdapter(
//                plugin1,
//                ListenerPriority.NORMAL,
//                PacketType.Play.Server.SYSTEM_CHAT,
//                PacketType.Play.Server.CHAT,
//                PacketType.Play.Server.SET_TITLE_TEXT,
//                PacketType.Play.Server.SET_SUBTITLE_TEXT,
//                PacketType.Play.Server.SET_ACTION_BAR_TEXT
//        ) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                if (event.isCancelled()) return;
//
//                Player player = event.getPlayer();
//                PacketContainer packet = event.getPacket();
//
//                // 1. Determinar si es mensaje del jugador
//                if (isPlayerMessage(packet)) return;
//
//                // 2. Extraer texto original
//                String original = extractPacketText(packet);
//                if (original == null || original.isEmpty()) return;
//
//                // 3. Traducir
//                String translated = plugin1.getTranslationManager().translateIfNeeded(player, original);
//
//                // 4. Reenviar si es diferente
//                if (!original.equals(translated)) {
//                    event.setCancelled(true);
//                    resendTranslatedPacket(player, packet.deepClone(), translated);
//                }
//            }
//        });
//    }
//
//    private boolean isPlayerMessage(PacketContainer packet) {
//        if (packet.getType() == PacketType.Play.Server.SYSTEM_CHAT) {
//            // Los mensajes del sistema nunca son de jugadores
//            return false;
//        }
//        try {
//            // Method para versiones 1.19+
//            Integer position = packet.getIntegers().optionRead(0).orElse(null);
//            return position != null && position == 2;
//        } catch (Exception e) {
//            // Mthod alternativo para versiones anteriores
//            return packet.getUUIDs().size() > 0 &&
//                    !packet.getUUIDs().read(0).equals(new UUID(0, 0));
//        }
//    }
//
//
//    private String extractPacketText(PacketContainer packet) {
//        try {
//            if (packet.getType() == PacketType.Play.Server.SYSTEM_CHAT ||
//                    packet.getType() == PacketType.Play.Server.CHAT) {
//
//                WrappedChatComponent component = packet.getChatComponents().read(0);
//                if (component != null) {
//                    return component.getJson();
//                }
//                return null;
//            } else if (packet.getType() == PacketType.Play.Server.SET_TITLE_TEXT ||
//                    packet.getType() == PacketType.Play.Server.SET_SUBTITLE_TEXT ||
//                    packet.getType() == PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
//
//                return packet.getStrings().read(0);
//            }
//            return null;
//        } catch (Exception e) {
//            plugin1.getLogger().warning("Error extrayendo texto: " + e.getMessage());
//            return null;
//        }
//    }
//
//    private void resendTranslatedPacket(Player player, PacketContainer originalPacket, String translated) {
//        Bukkit.getScheduler().runTask(plugin1, () -> { // Cambiar a sync
//            try {
//                PacketContainer newPacket = originalPacket.deepClone();
//
//                if (newPacket.getType() == PacketType.Play.Server.CHAT ||
//                        newPacket.getType() == PacketType.Play.Server.SYSTEM_CHAT) {
//
//                    WrappedChatComponent component = WrappedChatComponent.fromJson(translated);
//                    newPacket.getChatComponents().write(0, component);
//                }
//
//                else {
//                    newPacket.getStrings().write(0, translated);
//                }
//
//                versionAdapter.sendPacket(player, newPacket);
//            } catch (Exception e) {
//                plugin1.getLogger().severe("Error: " + e.getMessage());
//            }
//        });
//    }
//}
