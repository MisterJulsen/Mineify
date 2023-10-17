package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.ETrigger;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SimplePlaylist;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class SoundPlayerBlockEntityPacket implements IPacketBase<SoundPlayerBlockEntityPacket> {
    private BlockPos pos;
    private SimplePlaylist playlist;
    private PlaybackArea playbackArea;
    private float volume;
    private float pitch;
    private boolean locked;
    private ETrigger trigger;

    public SoundPlayerBlockEntityPacket() { }

    public SoundPlayerBlockEntityPacket(BlockPos pos, SimplePlaylist playlist, float volume, float pitch, PlaybackArea playbackArea, boolean locked, ETrigger trigger) {
        this.pos = pos;
        this.playlist = playlist;
        this.playbackArea = playbackArea;
        this.locked = locked;
        this.trigger = trigger;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void encode(SoundPlayerBlockEntityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.locked);
        buffer.writeByte(packet.trigger.getIndex());
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
        packet.playlist.serialize(buffer);
        packet.playbackArea.serialize(buffer);
    }

    @Override
    public SoundPlayerBlockEntityPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean locked = buffer.readBoolean();
        ETrigger trigger = ETrigger.getTriggerByIndex(buffer.readByte());
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        SimplePlaylist playlist = SimplePlaylist.deserialize(buffer);
        PlaybackArea playbackArea = PlaybackArea.deserialize(buffer);

        SoundPlayerBlockEntityPacket instance = new SoundPlayerBlockEntityPacket(pos, playlist, volume, pitch, playbackArea, locked, trigger);
        return instance;
    }

    @Override
    public void handle(SoundPlayerBlockEntityPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            Level level = context.get().getSender().getLevel();

            if (!level.isLoaded(packet.pos))
                return;

            if (level.getBlockEntity(packet.pos) instanceof SoundPlayerBlockEntity blockEntity) {
                blockEntity.getPlaylist().setPlaylist(packet.playlist.getSounds());
                blockEntity.getPlaylist().setLoop(packet.playlist.isLoop());
                blockEntity.getPlaylist().setRandom(packet.playlist.isRandom());
                blockEntity.getPlaylist().setPlaybackArea(packet.playbackArea);
                blockEntity.getPlaylist().setVolume(packet.volume);
                blockEntity.getPlaylist().setPitch(packet.pitch);
                blockEntity.setTrigger(packet.trigger);
                if (packet.locked) {
                    blockEntity.lock(context.get().getSender().getUUID());
                } else {                    
                    blockEntity.unlock();
                }
            }
        });
        
        context.get().setPacketHandled(true);      
    }
}
