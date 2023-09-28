package de.mrjulsen.mineify.sound;

import java.io.FileNotFoundException;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class Playlist extends SimplePlaylist {

    public static final Playlist DEFAULT = new Playlist(new SoundFile[0], false, false);

    private boolean isPlaying = false;
    private PlaybackArea playbackArea = new PlaybackArea(Constants.DEFAULT_PLAYBACK_AREA_RADIUS, Constants.DEFAULT_PLAYBACK_AREA_DISTANCE);
    
    // ticking
    private short currentTrack = 0;
    private long playNextAt = 0;
    private long currentSoundId = 0;

    //helper variables
    private boolean nextTrackRequested = false;
    private Runnable syncFunc = null;

    public Playlist(SoundFile[] sounds, boolean loop, boolean random) {
        super(sounds, loop, random);
    }

    public static Playlist fromNbt(CompoundTag compound) {
        CompoundTag playlistTag = compound.getCompound("playlist");
        SoundFile[] sounds = new SoundFile[playlistTag.size()];
        for (String key : playlistTag.getAllKeys()) {
            sounds[Integer.parseInt(key)] = SoundFile.fromNbt(playlistTag.getCompound(key));
        }
        boolean loop = compound.getBoolean("loop");
        boolean random = compound.getBoolean("random");
        
        Playlist playlist = new Playlist(sounds, loop, random);
        playlist.setPlaying(compound.getBoolean("playing"));
        playlist.setCurrentTrack(compound.getShort("currentTrackIndex"));
        playlist.setTimeToPlayNext(compound.getLong("playNextAt"));
        playlist.setCurrentSoundId(compound.getLong("currentSoundId"));
        playlist.setPlaybackArea(PlaybackArea.fromNbt(compound.getCompound("playbackArea")));

        return playlist;
    }

    public void toNbt(CompoundTag tag) {   
        CompoundTag playlist = new CompoundTag();
        SoundFile[] sounds = this.getSounds();
        for (int i = 0 ; i < sounds.length; i++) {
            playlist.put(String.valueOf(i), sounds[i].toNbt());
        }
        tag.put("playlist", playlist);
        tag.putBoolean("loop", this.isLoop());
        tag.putBoolean("random", this.isRandom());
        tag.putBoolean("playing", this.isPlaying);
        tag.putShort("currentTrackIndex", this.currentTrack);
        tag.putLong("playNextAt", this.playNextAt);
        tag.putLong("currentSoundId", this.getCurrentSoundId());
        tag.put("playbackArea", this.getPlaybackArea().toNbt());
    }

    public Playlist withSyncFunc(Runnable syncFunc) {
        this.syncFunc = syncFunc;
        return this;
    }

    
    public void setSyncFunc(Runnable syncFunc) {
        this.syncFunc = syncFunc;
    }    

    public SoundFile getSoundAt(short index) {
        return index < 0 || index >= this.getSounds().length ? null : this.getSounds()[index];
    }

    public SoundFile getPlayingSound() {
        return this.getSoundAt(this.getPlayingTrackIndex());
    }

    public long getTimeToPlayNext() {
        return this.playNextAt;
    }

    public short getPlayingTrackIndex() {
        return this.currentTrack;
    }

    public long getCurrentSoundId() {
        return this.currentSoundId;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public boolean isPlaylistEmpty() {
        return this.getSounds() == null || this.getSounds().length <= 0;
    }

    public PlaybackArea getPlaybackArea() {
        return this.playbackArea;
    }

    public void setPlaylist(SoundFile[] playlist) {
        this.sounds = playlist;
        Utils.executeIfNotNull(syncFunc);
    }

    public void setLoop(boolean b) {
        this.loop = b;
        Utils.executeIfNotNull(syncFunc);
    }

    public void setRandom(boolean b) {
        this.random = b;
        Utils.executeIfNotNull(syncFunc);
    }

    public void setPlaybackArea(PlaybackArea area) {
        this.playbackArea = area;
        this.playbackArea.check();
        Utils.executeIfNotNull(syncFunc);
    }

    public void calcAndSetTimeToPlayNext(long seconds) {
        this.setTimeToPlayNext(System.nanoTime() + (seconds * 1000000000));
    }

    public void setTimeToPlayNext(long time) {
        this.playNextAt = time;
        Utils.executeIfNotNull(syncFunc);
    }

    public void setCurrentTrack(short index) {
        this.currentTrack = index;
        Utils.executeIfNotNull(syncFunc);
    }

    private void setPlaying(boolean b) {
        this.isPlaying = b;
        Utils.executeIfNotNull(syncFunc);
    }

    private void playTrack(Level level, BlockPos pos) {
        if (this.isPlaylistEmpty() || this.getPlayingSound() == null) {
            this.stop(level);
            return;
        }

        nextTrackRequested = true;
        SoundFile playingSound = this.getPlayingSound();
        
        ModMain.LOGGER.debug("Play new track: " + playingSound);
        new Thread(() -> {
            try {
                if (!playingSound.exists()) {
                    throw new FileNotFoundException("The following sound file doesn't exist: " + playingSound);
                }

                this.setPlaying(true);        
                this.calcAndSetTimeToPlayNext(playingSound.calcDuration() + 1);
                this.stopPlayingSound(level);
                this.setCurrentSoundId(ServerApi.playSound(playingSound, ServerApi.getAffectedPlayers(this.getPlaybackArea(), level, pos), pos, this.getPlaybackArea().getVolume(), 1));
            } catch (Exception e) {
                ModMain.LOGGER.warn("Unable to play sound file: " + e.getMessage());
            } finally {
                nextTrackRequested = false;
            }
        }, "PlaySoundTrigger").start();
    }

    private void setCurrentSoundId(long soundId) {
        this.currentSoundId = soundId;
        Utils.executeIfNotNull(syncFunc);
    }

    private void onTrackPlaying(Level level, BlockPos pos) {        
        SoundFile current = this.getPlayingSound();
        if (current == null) {
            return;
        }

        if (this.getTimeToPlayNext() < System.nanoTime()) {
            this.playNext(level, pos);
        }
    }

    public void playNext(Level level, BlockPos pos) {        
        if (nextTrackRequested) {
            return;
        }

        if (this.isRandom()) {
            short i = (short)Constants.RANDOM.nextInt(0, this.getSounds().length);
            this.setCurrentTrack(i);
        } else {
            currentTrack++;
            if (this.getPlayingTrackIndex() >= this.getSounds().length) {
                if (this.isLoop()) {
                    this.setCurrentTrack((short)0);
                } else {
                    this.stop(level);
                }
            }
        }

        this.playTrack(level, pos);
    }

    public void playPrevious(Level level, BlockPos pos) {        
        
        if (nextTrackRequested) {
            return;
        }

        if (this.isRandom()) {
            short i = (short)Constants.RANDOM.nextInt(0, this.getSounds().length);
            this.setCurrentTrack(i);
        } else {
            currentTrack--;
            if (this.getPlayingTrackIndex() < 0) {
                if (this.isLoop()) {
                    this.setCurrentTrack((short)(this.getSounds().length - 1));
                } else {
                    this.stop(level);
                }
            }
        }        
        
        this.playTrack(level, pos);
    }

    public void play(Level level, BlockPos pos) {
        this.start(level, pos);
    }

    private void start(Level level, BlockPos pos) {
        if (nextTrackRequested) {
            return;
        }

        this.stop(level);
        this.setTimeToPlayNext(0);
        this.setCurrentTrack((short)0);
        this.playTrack(level, pos);
    }

    private void stopPlayingSound(Level level) {
        if (!level.isClientSide) {
            for (ServerPlayer p : level.players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                NetworkManager.sendToClient(new StopSoundPacket(this.getCurrentSoundId()), p);
            }
        }
        
    }

    public void stop(Level level) {
        this.setPlaying(false);
        this.stopPlayingSound(level);
    }


    public void tick(Level level, BlockPos pos) {
        if (!this.isPlaying())
            return;
        
        this.onTrackPlaying(level, pos);
    }
    
}
