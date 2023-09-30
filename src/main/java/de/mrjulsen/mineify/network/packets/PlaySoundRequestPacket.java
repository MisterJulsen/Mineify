package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PlaySoundRequestPacket {
    public final BlockPos pos;
    public final long requestId;
    public final PlaybackArea area;
    public final int attenuationDistance;
    public final float volume;
    public final float pitch;
    public final String path;
    public final ESoundCategory category;
    

    public PlaySoundRequestPacket(long requestId, PlaybackArea area, BlockPos pos, int attenuationDistance, float volume, float pitch, String path, ESoundCategory category) {
        this.pos = pos;
        this.requestId = requestId;
        this.area = area;
        this.volume = volume;
        this.attenuationDistance = attenuationDistance;
        this.pitch = pitch;
        this.path = path;
        this.category = category;
    }

    public static void encode(PlaySoundRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        packet.area.serialize(buffer);
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.attenuationDistance);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);        
        int l = packet.path.getBytes(StandardCharsets.UTF_8).length;
        buffer.writeInt(l);
        buffer.writeUtf(packet.path);
        buffer.writeEnum(packet.category);
    }

    public static PlaySoundRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        PlaybackArea area = PlaybackArea.deserialize(buffer);
        BlockPos pos = buffer.readBlockPos();
        int attenuationDistance = buffer.readInt();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int l = buffer.readInt();
        String path = buffer.readUtf(l);
        ESoundCategory category = buffer.readEnum(ESoundCategory.class);

        PlaySoundRequestPacket instance = new PlaySoundRequestPacket(requestId, area, pos, attenuationDistance, volume, pitch, path, category);
        return instance;
    }

    public static void handle(PlaySoundRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            ServerApi.playSound(SoundFile.fromShortPath(packet.path, packet.category), packet.area, context.get().getSender().level, packet.pos, packet.attenuationDistance, packet.volume, packet.pitch);
        });
        
        context.get().setPacketHandled(true);      
    }
}
