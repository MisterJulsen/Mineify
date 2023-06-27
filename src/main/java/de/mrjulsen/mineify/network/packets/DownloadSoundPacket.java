package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DownloadSoundPacket {
    public BlockPos pos;
    public long requestId;
    public int dataOffset;
    public int maxLength;
    public float volume;
    public byte[] data;

    public DownloadSoundPacket(long requestId, BlockPos pos, int dataOffset, byte[] data, int maxLength, float volume) {
        this.data = data;
        this.pos = pos;
        this.dataOffset = dataOffset;
        this.requestId = requestId;
        this.volume = volume;
        this.maxLength = maxLength;
    }

    public static void encode(DownloadSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.dataOffset);
        buffer.writeInt(packet.maxLength);
        buffer.writeFloat(packet.volume);
        buffer.writeByteArray(packet.data);
    }

    public static DownloadSoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        BlockPos pos = buffer.readBlockPos();
        int dataOffset = buffer.readInt();
        int maxLength = buffer.readInt();
        float volume = buffer.readFloat();
        byte[] data = buffer.readByteArray();

        DownloadSoundPacket instance = new DownloadSoundPacket(requestId, pos, dataOffset, data, maxLength, volume);
        return instance;
    }

    public static void handle(DownloadSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleDownloadSoundPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }

    
}
