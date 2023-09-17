package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundFilesCountRequestPacket {
    private final long requestID;

    public SoundFilesCountRequestPacket(long requestId) {
        this.requestID = requestId;
    }

    public static void encode(SoundFilesCountRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
    }

    public static SoundFilesCountRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();

        SoundFilesCountRequestPacket instance = new SoundFilesCountRequestPacket(requestId);
        return instance;
    }

    public static void handle(SoundFilesCountRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                
                ModMain.LOGGER.debug("Reading sound files...");
                long count = SoundUtils.readSoundsFromDisk().length;
                NetworkManager.sendToClient(new SoundFilesCountResponsePacket(packet.requestID, count), context.get().getSender());
                
                ModMain.LOGGER.debug("Sound file list created.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

