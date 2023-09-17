package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundListRequestPacket {
    private final long requestID;

    public SoundListRequestPacket(long requestId) {
        this.requestID = requestId;
    }

    public static void encode(SoundListRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
    }

    public static SoundListRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();

        SoundListRequestPacket instance = new SoundListRequestPacket(requestId);
        return instance;
    }

    public static void handle(SoundListRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                
                ModMain.LOGGER.debug("Reading sound files...");
                SoundFile[] soundFiles = SoundUtils.readSoundsFromDisk();
                NetworkManager.sendToClient(new SoundListResponsePacket(packet.requestID, soundFiles), context.get().getSender());
                
                ModMain.LOGGER.debug("Sound file list created.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

