package de.mrjulsen.mineify.blocks.blockentity;

import java.io.FileNotFoundException;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.client.ETrigger;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.SoundRequest;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;

public class SoundPlayerBlockEntity extends BlockEntity {

    private final Random rand = new Random();

    // Properties
    private SoundFile[] playlist = new SoundFile[0];
    private UUID owner = null;
    private ETrigger trigger = ETrigger.NONE;
    private boolean loop = false;
    private boolean random = false;
    private boolean isPlaying = false;
    private PlaybackArea playbackArea = new PlaybackArea(Constants.DEFAULT_PLAYBACK_AREA_RADIUS, Constants.DEFAULT_PLAYBACK_AREA_DISTANCE);

    // ticking
    private short currentTrack = 0;
    private long playNextAt = 0;
    private long currentSoundId = 0;
    private boolean powered = false;

    //helper variables
    private boolean nextTrackRequested = false;


    protected SoundPlayerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SoundPlayerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUND_PLAYER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        
        CompoundTag playlist = compound.getCompound("playlist");
        this.playlist = new SoundFile[playlist.size()];
        for (String key : playlist.getAllKeys()) {
            this.playlist[Integer.parseInt(key)] = SoundFile.fromNbt(playlist.getCompound(key));
        }
        if (compound.contains("owner")) {
            try {
                this.owner = compound.getUUID("owner");
            } catch (Exception e) {
                this.owner = null;
            }
        }
        this.trigger = ETrigger.getTriggerByIndex(compound.getByte("trigger"));
        this.loop = compound.getBoolean("loop");
        this.random = compound.getBoolean("random");
        this.isPlaying = compound.getBoolean("playing");
        this.currentTrack = compound.getShort("currentTrackIndex");
        this.playNextAt = compound.getLong("playNextAt");
        this.currentSoundId = compound.getLong("currentSoundId");
        this.powered = compound.getBoolean("powered");
        this.setPlaybackArea(PlaybackArea.fromNbt(compound.getCompound("playbackArea")));
        
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {   
        CompoundTag playlist = new CompoundTag();
        for (int i = 0 ; i < this.playlist.length; i++) {
            playlist.put(String.valueOf(i), this.playlist[i].toNbt());
        }
        tag.put("playlist", playlist);
        if (this.getOwnerUUID() != null) {
            tag.putUUID("owner", this.getOwnerUUID());
        } else {
            tag.putBoolean("owner", false);
        }
        tag.putByte("trigger", this.getTrigger().getIndex());
        tag.putBoolean("loop", this.isLooping());
        tag.putBoolean("random", this.isRandom());
        tag.putBoolean("playing", this.isPlaying);
        tag.putShort("currentTrackIndex", this.currentTrack);
        tag.putLong("playNextAt", this.playNextAt);
        tag.putLong("currentSoundId", this.getCurrentSoundId());
        tag.putBoolean("powered", this.isPowered());
        tag.put("playbackArea", this.getPlaybackArea().toNbt());

        super.saveAdditional(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        this.load(pkt.getTag());
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 3, 512);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!this.isPlaying())
            return;
        
        this.onTrackPlaying();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoundPlayerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(level, pos, state);
        }
    }

    private void clientSync() {
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }    

    public SoundFile[] getPlaylist() {
        return this.playlist;
    }

    public SoundFile getSoundAt(short index) {
        return index < 0 || index >= this.getPlaylist().length ? null : this.getPlaylist()[index];
    }

    public SoundFile getPlaying() {
        return this.getSoundAt(this.getPlayingTrackIndex());
    }

    public long getTimeToPlayNext() {
        return this.playNextAt;
    }

    public short getPlayingTrackIndex() {
        return this.currentTrack;
    }

    public UUID getOwnerUUID() {
        return this.owner;
    }

    public boolean isLocked() {
        return this.getOwnerUUID() != null;
    }

    public boolean canVisit(UUID uuid) {
        return !this.isLocked() || this.getOwnerUUID().equals(uuid);
    }

    public long getCurrentSoundId() {
        return this.currentSoundId;
    }

    public ETrigger getTrigger() {
        return this.trigger;
    }

    public boolean isLooping() {
        return this.loop;
    }

    public boolean isRandom() {
        return this.random;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public boolean isPlaylistEmpty() {
        return this.getPlaylist() == null || this.getPlaylist().length <= 0;
    }

    public PlaybackArea getPlaybackArea() {        
        return this.playbackArea;
    }

    public void setPlaylist(SoundFile[] playlist) {
        this.playlist = playlist;
        this.clientSync();
    }

    public void lock(UUID uuid) {
        this.owner = uuid;
        this.clientSync();
    }

    public void unlock() {
        this.owner = null;
        this.clientSync();
    }

    public void setTrigger(ETrigger trigger) {
        this.trigger = trigger;
        this.clientSync();
    }

    public void setLooping(boolean b) {
        this.loop = b;
        this.clientSync();
    }

    public void setRandom(boolean b) {
        this.random = b;
        this.clientSync();
    }

    public void setPlaybackArea(PlaybackArea area) {
        this.playbackArea = area;
        this.playbackArea.check();
        this.clientSync();
    }

    public void calcTimeToPlayNext(long seconds) {
        this.setTimeToPlayNext(System.nanoTime() + (seconds * 1000000000));
    }

    public void setTimeToPlayNext(long time) {
        this.playNextAt = time;
        this.clientSync();
    }

    public void setCurrentTrack(short index) {
        this.currentTrack = index;
        this.clientSync();
    }

    private void setPlaying(boolean b) {
        this.isPlaying = b;
        this.clientSync();
    }

    private void playTrack() {
        if (this.isPlaylistEmpty() || this.getPlaying() == null) {
            this.stop();
            return;
        }

        nextTrackRequested = true;
        
        ModMain.LOGGER.debug("Play new track: " + this.getPlaying());
        new Thread(() -> {
            try {
                if (!this.getPlaying().exists()) {
                    throw new FileNotFoundException("The following sound file doesn't exist: " + this.getPlaying());
                }

                this.setPlaying(true);        
                this.calcTimeToPlayNext(this.getPlaying().calcDuration() + 1);
                this.stopPlayingSound();
                this.setCurrentSoundId(ServerApi.playSound(this.getPlaying(), this.getAffectedPlayers(), this.getBlockPos(), this.getPlaybackArea().getVolume()));
            } catch (Exception e) {
                ModMain.LOGGER.warn("Unable to play sound file: " + e.getMessage());
            } finally {
                nextTrackRequested = false;
            }
        }, "PlaySoundTrigger").start();
    }

    private void setCurrentSoundId(long soundId) {
        this.currentSoundId = soundId;
        this.clientSync();
    }

    private void onTrackPlaying() {
        
        SoundFile current = this.getPlaying();
        if (current == null) {
            return;
        }

        if (this.getTimeToPlayNext() < System.nanoTime()) {
            this.playNext();
        }
    }

    public void playNext() {        
        if (nextTrackRequested) {
            return;
        }

        if (this.isRandom()) {
            short i = (short)rand.nextInt(0, this.getPlaylist().length);
            this.setCurrentTrack(i);
        } else {
            currentTrack++;
            if (this.getPlayingTrackIndex() >= this.getPlaylist().length) {
                if (this.isLooping()) {
                    this.setCurrentTrack((short)0);
                } else {
                    this.stop();
                }
            }
        }

        this.playTrack();
    }

    public void playPrevious() {        
        
        if (nextTrackRequested) {
            return;
        }

        if (this.isRandom()) {
            short i = (short)rand.nextInt(0, this.getPlaylist().length);
            this.setCurrentTrack(i);
        } else {
            currentTrack--;
            if (this.getPlayingTrackIndex() < 0) {
                if (this.isLooping()) {
                    this.setCurrentTrack((short)(this.getPlaylist().length - 1));
                } else {
                    this.stop();
                }
            }
        }        
        
        this.playTrack();
    }

    public void start() {

        if (nextTrackRequested) {
            return;
        }

        this.stop();
        this.setTimeToPlayNext(0);
        this.setCurrentTrack((short)0);
        this.playTrack();
    }

    public void stopPlayingSound() {
        if (!this.level.isClientSide) {
            for (ServerPlayer p : this.getLevel().players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                NetworkManager.MOD_CHANNEL.sendTo(new StopSoundPacket(this.getCurrentSoundId()), p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);                
            }
        }
        
    }

    public void stop() {
        this.setPlaying(false);
        this.stopPlayingSound();
    }

    public ServerPlayer[] getAffectedPlayers() {
        switch (this.getPlaybackArea().getAreaType()) {
            case ZONE:
                return this.getLevel().players().stream().filter(p -> p instanceof ServerPlayer && this.getPlaybackArea().isInZone(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), p.position().x(), p.position().y(), p.position().z())).toArray(ServerPlayer[]::new);
            case RADIUS:
            default:
                return this.getLevel().players().stream().filter(p -> p instanceof ServerPlayer && p.position().distanceTo(new Vec3(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ())) <= this.getPlaybackArea().getRadius()).toArray(ServerPlayer[]::new);
        }
    }

    public void setPowered(boolean b) {
        this.powered = b;
        this.clientSync();        

        if (b && (this.getTrigger() == ETrigger.CONTINUOUS_REDSTONE || this.getTrigger() == ETrigger.REDSTONE_IMPULSE)) {
            this.start();            
        } else if (!b && this.getTrigger() == ETrigger.CONTINUOUS_REDSTONE) {
            this.stop();
        }
    }

    public boolean isPowered() {
        return this.powered;
    }
}
