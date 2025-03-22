package net.ezplace.groupChat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.ezplace.groupChat.GroupChat;
import org.bukkit.entity.Player;

public class PacketListener {
    GroupChat plugin = GroupChat.getInstance(); //Va a dar null pointer exception?
    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL,
                        PacketType.Play.Server.CHAT,
                        PacketType.Play.Server.TITLE) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (shouldTranslate(event.getPlayer())) {
                            event.setCancelled(true);
                            resendTranslated(event);
                        }
                    }
                });
    }

    public boolean shouldTranslate(Player eventPlayer){
        //logica a implementar para traducir
        return false;
    }

    public void resendTranslated(PacketEvent event){
        //logica para reenviar el nuevo mensaje;
    }
}
