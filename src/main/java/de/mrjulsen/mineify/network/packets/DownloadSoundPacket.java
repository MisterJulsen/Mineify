package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.sound.EStreamingMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DownloadSoundPacket {
    public final long requestId;
    public final int dataOffset;
    public final int maxLength;
    public final EStreamingMode streamingMode;
    public final byte[] data;
    

    public DownloadSoundPacket(long requestId, int dataOffset, byte[] data, int maxLength, EStreamingMode streamingMode) {
        this.data = data;
        this.dataOffset = dataOffset;
        this.requestId = requestId;
        this.streamingMode = streamingMode;
        this.maxLength = maxLength;
    }

    public static void encode(DownloadSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.dataOffset);
        buffer.writeInt(packet.maxLength);
        buffer.writeEnum(packet.streamingMode);
        buffer.writeByteArray(packet.data);
    }

    public static DownloadSoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int dataOffset = buffer.readInt();
        int maxLength = buffer.readInt();
        EStreamingMode streamingMode = buffer.readEnum(EStreamingMode.class);
        byte[] data = buffer.readByteArray();

        DownloadSoundPacket instance = new DownloadSoundPacket(requestId, dataOffset, data, maxLength, streamingMode);
        return instance;
    }

    public static void handle(DownloadSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWrapper.handleDownloadSoundPacket(packet, context);
            });
        });
        
        context.get().setPacketHandled(true);      
    }

    
}
