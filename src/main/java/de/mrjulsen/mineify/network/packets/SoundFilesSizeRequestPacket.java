package de.mrjulsen.mineify.network.packets;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundFilesSizeRequestPacket {
    private final long requestID;

    public SoundFilesSizeRequestPacket(long requestId) {
        this.requestID = requestId;
    }

    public static void encode(SoundFilesSizeRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
    }

    public static SoundFilesSizeRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();

        SoundFilesSizeRequestPacket instance = new SoundFilesSizeRequestPacket(requestId);
        return instance;
    }

    public static void handle(SoundFilesSizeRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {                
                ModMain.LOGGER.debug("Reading sound files...");
                long count = Arrays.stream(SoundUtils.readSoundsFromDisk()).mapToLong(x -> x.getSize()).sum();
                NetworkManager.sendToClient(new SoundFilesCountResponsePacket(packet.requestID, count), context.get().getSender());
                
                ModMain.LOGGER.debug("Sound file list created.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

