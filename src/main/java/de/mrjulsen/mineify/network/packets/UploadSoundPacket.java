package de.mrjulsen.mineify.network.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

import de.mrjulsen.mineify.network.InstanceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class UploadSoundPacket {
    private final long requestId;
    private final int maxSize; 
    private final byte[] data;

    public UploadSoundPacket(long requestId, byte[] data, int maxSize) {
        this.data = data;
        this.maxSize = maxSize;
        this.requestId = requestId;
    }

    public static void encode(UploadSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.maxSize);
        buffer.writeByteArray(packet.data);
    }

    public static UploadSoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int maxSize = buffer.readInt();
        byte[] data = buffer.readByteArray();

        UploadSoundPacket instance = new UploadSoundPacket(requestId, data, maxSize);
        return instance;
    }

    public static void handle(UploadSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            if (!InstanceManager.Server.streamCache.containsKey(packet.requestId)) {
                InstanceManager.Server.streamCache.put(packet.requestId, new ByteArrayOutputStream(packet.maxSize));
            }
            
            try {
                InstanceManager.Server.streamCache.get(packet.requestId).write(packet.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        context.get().setPacketHandled(true);      
    }

    
    
}
