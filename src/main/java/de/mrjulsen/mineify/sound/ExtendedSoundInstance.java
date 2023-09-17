package de.mrjulsen.mineify.sound;

import java.util.concurrent.CompletableFuture;

import de.mrjulsen.mineify.network.InstanceManager;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ExtendedSoundInstance extends AbstractSoundInstance {

    private final SoundBuffer audioData;

    public ExtendedSoundInstance(ResourceLocation pLocation, SoundBuffer audioData, SoundSource pSource, float volume, BlockPos position) {
        super(pLocation, pSource);
        this.volume = volume;
        this.audioData = audioData;
        this.x = position.getX() + 0.5D;
        this.y = position.getY() + 0.5D;
        this.z = position.getZ() + 0.5D;

        if (!InstanceManager.Client.playingSoundsCache.containsKey(this.audioData.getId())) {
            InstanceManager.Client.playingSoundsCache.put(this.audioData.getId(), this);
        }
    }   

   
    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return new ExtendedSoundBufferLibrary(audioData).getStream(null, looping);
    }
    

    @Override
    public Sound getSound() {
        
        return new Sound(this.sound.getPath().getPath(), this.sound.getVolume(), this.sound.getPitch(), this.sound.getWeight(), Sound.Type.FILE, true, false, this.sound.getAttenuationDistance());
    }
    
}
