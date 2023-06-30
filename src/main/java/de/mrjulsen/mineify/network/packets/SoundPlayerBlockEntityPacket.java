package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.ETrigger;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.PlaylistData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class SoundPlayerBlockEntityPacket {
    private final BlockPos pos;
    private final PlaylistData playlist;
    private final PlaybackArea playbackArea;
    private final boolean locked;
    private final ETrigger trigger;

     public SoundPlayerBlockEntityPacket(BlockPos pos, PlaylistData playlist, PlaybackArea playbackArea, boolean locked, ETrigger trigger) {
        this.pos = pos;
        this.playlist = playlist;
        this.playbackArea = playbackArea;
        this.locked = locked;
        this.trigger = trigger;
    }

    public static void encode(SoundPlayerBlockEntityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.locked);
        buffer.writeByte(packet.trigger.getIndex());
        packet.playlist.serialize(buffer);
        packet.playbackArea.serialize(buffer);
    }

    public static SoundPlayerBlockEntityPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean locked = buffer.readBoolean();
        ETrigger trigger = ETrigger.getTriggerByIndex(buffer.readByte());
        PlaylistData playlist = PlaylistData.deserialize(buffer);
        PlaybackArea playbackArea = PlaybackArea.deserialize(buffer);

        SoundPlayerBlockEntityPacket instance = new SoundPlayerBlockEntityPacket(pos, playlist, playbackArea, locked, trigger);
        return instance;
    }

    public static void handle(SoundPlayerBlockEntityPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            Level level = context.get().getSender().getLevel();

            if (!level.isLoaded(packet.pos))
                return;

            if (level.getBlockEntity(packet.pos) instanceof SoundPlayerBlockEntity blockEntity) {
                blockEntity.setPlaylist(packet.playlist.sounds);
                blockEntity.setLooping(packet.playlist.loop);
                blockEntity.setRandom(packet.playlist.random);
                blockEntity.setPlaybackArea(packet.playbackArea);
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
