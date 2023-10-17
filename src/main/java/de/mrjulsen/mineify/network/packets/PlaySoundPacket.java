package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PlaySoundPacket implements IPacketBase<PlaySoundPacket> {
    public BlockPos pos;
    public long requestId;
    public int attenuationDistance;
    public float pitch;
    public float volume;
    public String path;

    public PlaySoundPacket() { }

    public PlaySoundPacket(long requestId, BlockPos pos, int attenuationDistance, float volume, float pitch, String path) {
        this.pos = pos;
        this.requestId = requestId;
        this.attenuationDistance = attenuationDistance;
        this.volume = volume;
        this.pitch = pitch;
        this.path = path;
    }

    @Override
    public void encode(PlaySoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.attenuationDistance);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);        
        int l = packet.path.getBytes(StandardCharsets.UTF_8).length;
        buffer.writeInt(l);
        buffer.writeUtf(packet.path);
    }

    @Override
    public PlaySoundPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        BlockPos pos = buffer.readBlockPos();
        int attenuationDistance = buffer.readInt();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int l = buffer.readInt();
        String path = buffer.readUtf(l);

        PlaySoundPacket instance = new PlaySoundPacket(requestId, pos, attenuationDistance, volume, pitch, path);
        return instance;
    }

    @Override
    public void handle(PlaySoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWrapper.handlePlaySoundPacket(packet, context);
            });
        });
        
        context.get().setPacketHandled(true);      
    }
}
