package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class NextSoundDataRequestPacket implements IPacketBase<NextSoundDataRequestPacket> {
    private long requestId;
    private int index;

    public NextSoundDataRequestPacket() { }

    public NextSoundDataRequestPacket(long requestId, int index) {
        this.requestId = requestId;
        this.index = index;
    }

    @Override
    public void encode(NextSoundDataRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.index);
    }

    @Override
    public NextSoundDataRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int index = buffer.readInt();

        NextSoundDataRequestPacket instance = new NextSoundDataRequestPacket(requestId, index);
        return instance;
    }

    @Override
    public void handle(NextSoundDataRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            
            if (InstanceManager.Server.fileCache.containsKey(packet.requestId)) {                
                byte[] data = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                boolean hasNext = false;
                try {
                    int bytesRead = InstanceManager.Server.fileCache.get(packet.requestId).read(context.get().getSender().getUUID(), data);
                    hasNext = bytesRead > -1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                NetworkManager.sendToClient(new NextSoundDataResponsePacket(packet.requestId, data, hasNext, packet.index), context.get().getSender());
                
            }

        });
        
        context.get().setPacketHandled(true);      
    }

    
    
}
