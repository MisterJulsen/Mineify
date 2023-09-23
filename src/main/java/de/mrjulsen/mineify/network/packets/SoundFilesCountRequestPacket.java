package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundFilesCountRequestPacket {
    private final long requestID;
    private final @NotNull ESoundVisibility[] visibilityWhitelist;
    private final @NotNull String[] usersWhitelist;

    public SoundFilesCountRequestPacket(long requestId, @NotNull ESoundVisibility[] visibilityWhitelist, @NotNull String[] usersWhitelist) {
        this.requestID = requestId;
        this.visibilityWhitelist = visibilityWhitelist == null ? new ESoundVisibility[0] : visibilityWhitelist;
        this.usersWhitelist = usersWhitelist == null ? new String[0] : usersWhitelist;
    }

    public static void encode(SoundFilesCountRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
        buffer.writeInt(packet.visibilityWhitelist.length);
        for (int i = 0; i < packet.visibilityWhitelist.length; i++) {
            buffer.writeInt(packet.visibilityWhitelist[i].getIndex());
        }
        buffer.writeInt(packet.usersWhitelist.length);
        for (int i = 0; i < packet.usersWhitelist.length; i++) {
            int l = packet.usersWhitelist[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(l);
            buffer.writeUtf(packet.usersWhitelist[i], l);
        }
    }

    public static SoundFilesCountRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int n = buffer.readInt();
        ESoundVisibility[] visibilityWhitelist = new ESoundVisibility[n];
        for (int i = 0; i < n; i++) {
            visibilityWhitelist[i] = ESoundVisibility.getVisibilityByIndex(buffer.readInt());
        }
        int o = buffer.readInt();
        String[] usersWhitelist = new String[o];
        for (int i = 0; i < o; i++) {
            int l = buffer.readInt();
            usersWhitelist[i] = buffer.readUtf(l);
        }

        SoundFilesCountRequestPacket instance = new SoundFilesCountRequestPacket(requestId, visibilityWhitelist, usersWhitelist);
        return instance;
    }

    public static void handle(SoundFilesCountRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                
                ModMain.LOGGER.debug("Reading sound files...");
                long count = SoundUtils.readSoundsFromDisk(packet.visibilityWhitelist, packet.usersWhitelist).length;
                NetworkManager.sendToClient(new SoundFilesCountResponsePacket(packet.requestID, count), context.get().getSender());
                
                ModMain.LOGGER.debug("Sound file list created.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

