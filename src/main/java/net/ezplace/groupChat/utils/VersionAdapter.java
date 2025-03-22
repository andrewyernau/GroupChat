package net.ezplace.groupChat.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public abstract class VersionAdapter {
    public abstract void sendPacket(Player player, PacketContainer packet) throws InvocationTargetException;

    public static VersionAdapter create() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        switch(version) {
            case "v1_21_R1":
                return new VersionAdapter_1_21();
            case "v1_20_R3":
                return new VersionAdapter_1_20();
            default:
                return new VersionAdapter_Generic();
        }
    }
}

// Implementación para 1.21
class VersionAdapter_1_21 extends VersionAdapter {
    @Override
    public void sendPacket(Player player, PacketContainer packet) throws InvocationTargetException {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}

class VersionAdapter_1_20 extends VersionAdapter {
    @Override
    public void sendPacket(Player player, PacketContainer packet) throws InvocationTargetException {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}

class VersionAdapter_Generic extends VersionAdapter {
    @Override
    public void sendPacket(Player player, PacketContainer packet) throws InvocationTargetException {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}
