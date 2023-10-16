package de.mrjulsen.mineify.sound;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.mojang.blaze3d.audio.Channel;

import de.mrjulsen.mineify.network.InstanceManager;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class ModifiedSoundInstance extends AbstractSoundInstance {

    private final SoundBuffer audioData;
    private final String path; // for accessibility only
    private Channel channel;
    private int attenuationDistance;

    public ModifiedSoundInstance(ResourceLocation pLocation, SoundBuffer audioData, SoundSource pSource, int attenuationDistance, float volume, float pitch, BlockPos position, String path) {
        super(pLocation, pSource, RandomSource.create());
        this.path = path;
        this.pitch = pitch;
        this.volume = volume;
        this.audioData = audioData;
        this.attenuationDistance = attenuationDistance;
        this.pitch = pitch;
        this.x = position.getX() + 0.5D;
        this.y = position.getY() + 0.5D;
        this.z = position.getZ() + 0.5D;

        if (!InstanceManager.Client.playingSoundsCache.contains(this.audioData.getId())) {
            InstanceManager.Client.playingSoundsCache.put(this.audioData.getId(), this);
        }
    }

    public void setSoundChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * 
     * @return A short path of the sound
     */
    public String getPath() {
        return path;
    }

    public void modify(@Nullable Integer attenuationDistance, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        if (channel == null) {
            return;
        }

        if (attenuationDistance != null) {
            channel.setVolume(volume);
            channel.linearAttenuation(Math.max(volume, 1.0F) * (float)attenuationDistance);
        }
        if (pitch != null) channel.setPitch(pitch);

        if (x != null && y != null && z != null) {
            double x1 = x == null ? this.x : x;
            double y1 = y == null ? this.y : y;
            double z1 = z == null ? this.z : z;
            channel.setSelfPosition(new Vec3(x1, y1, z1));
        }
    }

   
    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return new ModifiedSoundBufferLibrary(audioData).getStream(null, looping);
    }
    

    @Override
    public Sound getSound() {        
        return new Sound(this.sound.getPath().getPath(), this.sound.getVolume(), this.sound.getPitch(), this.sound.getWeight(), Sound.Type.FILE, true, false, attenuationDistance);
    }
    
}
