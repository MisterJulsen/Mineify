package de.mrjulsen.mineify.api;

import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.SoundRequest;
import de.mrjulsen.mineify.network.packets.SoundDeleteRequestPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class contains all methods that should only be called on the client side.
 */
public class MineifyApiClient {
    /**
     * Stop a playing custom sound.
     * @param soundId The ID of the sound you want to stop.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void stopSound(long soundId) {
        SoundRequest.stopSoundOnClient(soundId);
    }

    /**
     * Upload a custom sound.
     * @param srcPath The source path of the sound on the client's pc.
     * @param filename The filename of the sound on the server.
     * @param visibility Controls which players can see and modify the sound.
     * @param config Some audio settings. Use {@code AudioFileConfig.DEFAULT} to use the default settings.
     * @param uploader The UUID of the player, who ownes the sound file.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void uploadSound(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader) {        
        ClientWrapper.uploadFromClient(srcPath, filename, visibility, config, uploader, -1);
    }

    /**
     * Upload a custom sound.
     * @param srcPath The source path of the sound on the client's pc.
     * @param filename The filename of the sound on the server.
     * @param visibility Controls which players can see and modify the sound.
     * @param config Some audio settings. Use {@code AudioFileConfig.DEFAULT} to use the default settings.
     */
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT) 
    public static void uploadSound(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config) {  
        uploadSound(srcPath, filename, visibility, config, Minecraft.getInstance().player.getUUID());
    }

    /**
     * Get a list of sounds the client can use.
     * @param callback A consumer which will be executed after the server sent back the data to the client.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getSoundList(Consumer<SoundFile[]> callback) {
        SoundRequest.getSoundListFromServer(callback);
    }

    /**
     * Creates a sound file with sounds uploaded by the client.
     * @param filename The name of the sound.
     * @param visibility The visibility of the sound.
     * @return A new instance of {@code SoundFile}.
     */
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT) 
    public static SoundFile soundFileOf(String filename, ESoundVisibility visibility) {
        return new SoundFile(filename, Minecraft.getInstance().player.getUUID().toString(), visibility);
    }

    @OnlyIn(Dist.CLIENT) 
    public static boolean isSoundPlaying(long soundId) {
        return InstanceManager.Client.playingSoundsCache.containsKey(soundId);
    }

    @OnlyIn(Dist.CLIENT) 
    public static void deleteSound(String filename, EUserSoundVisibility visibility, UUID owner) {
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundDeleteRequestPacket(filename, owner.toString(), visibility.toESoundVisibility()));
    }

    @OnlyIn(Dist.CLIENT) 
    @SuppressWarnings("resource")
    public static void deleteSound(String filename, EUserSoundVisibility visibility) {
        deleteSound(filename, visibility, Minecraft.getInstance().player.getUUID());
    }
}
