package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PlaySoundPacket {
    public BlockPos pos;
    public long requestId;
    public float volume;
    

    public PlaySoundPacket(long requestId, BlockPos pos, float volume) {
        this.pos = pos;
        this.requestId = requestId;
        this.volume = volume;
    }

    public static void encode(PlaySoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeFloat(packet.volume);
    }

    public static PlaySoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        BlockPos pos = buffer.readBlockPos();
        float volume = buffer.readFloat();

        PlaySoundPacket instance = new PlaySoundPacket(requestId, pos, volume);
        return instance;
    }

    public static void handle(PlaySoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWrapper.handlePlaySoundPacket(packet, context);
            });
        });
        
        context.get().setPacketHandled(true);      
    }
}
