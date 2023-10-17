package de.mrjulsen.mineify.network.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

import de.mrjulsen.mineify.network.InstanceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class UploadSoundPacket implements IPacketBase<UploadSoundPacket> {
    private long requestId;
    private int maxSize; 
    private byte[] data;

    public UploadSoundPacket() { }

    public UploadSoundPacket(long requestId, byte[] data, int maxSize) {
        this.data = data;
        this.maxSize = maxSize;
        this.requestId = requestId;
    }

    @Override
    public void encode(UploadSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.maxSize);
        buffer.writeByteArray(packet.data);
    }

    @Override
    public UploadSoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int maxSize = buffer.readInt();
        byte[] data = buffer.readByteArray();

        UploadSoundPacket instance = new UploadSoundPacket(requestId, data, maxSize);
        return instance;
    }

    @Override
    public void handle(UploadSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
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
