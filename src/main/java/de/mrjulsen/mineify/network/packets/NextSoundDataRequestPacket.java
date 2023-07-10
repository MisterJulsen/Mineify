package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class NextSoundDataRequestPacket {
    private final long requestId;
    private final int index;

    public NextSoundDataRequestPacket(long requestId, int index) {
        this.requestId = requestId;
        this.index = index;
    }

    public static void encode(NextSoundDataRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.index);
    }

    public static NextSoundDataRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int index = buffer.readInt();

        NextSoundDataRequestPacket instance = new NextSoundDataRequestPacket(requestId, index);
        return instance;
    }

    public static void handle(NextSoundDataRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
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
                NetworkManager.MOD_CHANNEL.sendTo(new NextSoundDataResponsePacket(packet.requestId, data, hasNext, packet.index), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                
            }

        });
        
        context.get().setPacketHandled(true);      
    }

    
    
}
