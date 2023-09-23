package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PlaySoundPacket {
    public final BlockPos pos;
    public final long requestId;
    public final float volume;
    public final float pitch;
    public final String path;
    

    public PlaySoundPacket(long requestId, BlockPos pos, float volume, float pitch, String path) {
        this.pos = pos;
        this.requestId = requestId;
        this.volume = volume;
        this.pitch = pitch;
        this.path = path;
    }

    public static void encode(PlaySoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);        
        int l = packet.path.getBytes(StandardCharsets.UTF_8).length;
        buffer.writeInt(l);
        buffer.writeUtf(packet.path);
    }

    public static PlaySoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        BlockPos pos = buffer.readBlockPos();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int l = buffer.readInt();
        String path = buffer.readUtf(l);

        PlaySoundPacket instance = new PlaySoundPacket(requestId, pos, volume, pitch, path);
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
