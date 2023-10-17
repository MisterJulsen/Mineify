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

public class PlaySoundRequestPacket implements IPacketBase<PlaySoundRequestPacket> {
    public BlockPos pos;
    public long requestId;
    public PlaybackArea area;
    public int attenuationDistance;
    public float volume;
    public float pitch;
    public String path;
    public ESoundCategory category;    

    public PlaySoundRequestPacket() { }

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

    @Override
    public void encode(PlaySoundRequestPacket packet, FriendlyByteBuf buffer) {
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

    @Override
    public PlaySoundRequestPacket decode(FriendlyByteBuf buffer) {
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

    @Override
    public void handle(PlaySoundRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            ServerApi.playSound(SoundFile.fromShortPath(packet.path, packet.category), packet.area, context.get().getSender().level, packet.pos, packet.attenuationDistance, packet.volume, packet.pitch);
        });
        
        context.get().setPacketHandled(true);      
    }
}
