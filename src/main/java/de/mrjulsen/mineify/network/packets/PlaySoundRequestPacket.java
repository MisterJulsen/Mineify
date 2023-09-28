package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PlaySoundRequestPacket {
    public final BlockPos pos;
    public final long requestId;
    public final PlaybackArea area;
    public final float volume;
    public final float pitch;
    public final String path;
    

    public PlaySoundRequestPacket(long requestId,PlaybackArea area, BlockPos pos, float volume, float pitch, String path) {
        this.pos = pos;
        this.requestId = requestId;
        this.area = area;
        this.volume = volume;
        this.pitch = pitch;
        this.path = path;
    }

    public static void encode(PlaySoundRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        packet.area.serialize(buffer);
        buffer.writeBlockPos(packet.pos);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);        
        int l = packet.path.getBytes(StandardCharsets.UTF_8).length;
        buffer.writeInt(l);
        buffer.writeUtf(packet.path);
    }

    public static PlaySoundRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        PlaybackArea area = PlaybackArea.deserialize(buffer);
        BlockPos pos = buffer.readBlockPos();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int l = buffer.readInt();
        String path = buffer.readUtf(l);

        PlaySoundRequestPacket instance = new PlaySoundRequestPacket(requestId, area, pos, volume, pitch, path);
        return instance;
    }

    public static void handle(PlaySoundRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            ServerApi.playSound(SoundFile.fromShortPath(packet.path), packet.area, context.get().getSender().level, packet.pos, packet.volume, packet.pitch);
        });
        
        context.get().setPacketHandled(true);      
    }
}
