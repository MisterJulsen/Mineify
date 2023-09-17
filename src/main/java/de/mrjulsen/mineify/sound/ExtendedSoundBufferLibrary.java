package de.mrjulsen.mineify.sound;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;

public class ExtendedSoundBufferLibrary extends SoundBufferLibrary {

    private SoundBuffer clientInputStream;

    public ExtendedSoundBufferLibrary(SoundBuffer audioStream) {
        super(Minecraft.getInstance().getResourceManager());
        this.clientInputStream = audioStream;
    }

    
    @Override
    public CompletableFuture<AudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return (AudioStream) new ExtendedOggAudioStream(clientInputStream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}
