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
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class contains all methods that should only be called on the client side.
 */
public class ClientApi {
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
    public static void uploadSound(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader, Runnable andThen) {        
        ClientWrapper.uploadFromClient(srcPath, filename, visibility, config, uploader, -1, andThen);
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
    public static void uploadSound(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, Runnable andThen) {  
        uploadSound(srcPath, filename, visibility, config, Minecraft.getInstance().player.getUUID(), andThen);
    }

    /**
     * Get a list of sounds the client can use.
     * @param callback A consumer which will be executed after the server sent back the data to the client.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getSoundList(Consumer<SoundFile[]> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.consumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundListRequestPacket(requestId));
    }

    /**
     * Creates a sound file with sounds uploaded by the client.
     * @param filename The name of the sound.
     * @param visibility The visibility of the sound.
     * @return A new instance of {@code SoundFile}.
     */
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT) 
    public static SoundFile personalSoundFileOf(String filename, ESoundVisibility visibility) {
        return new SoundFile(filename, Minecraft.getInstance().player.getUUID().toString(), visibility);
    }

    /**
     * Determines if a sound with the given ID is currently playing.
     * @param soundId The ID of the sound you want to check.
     * @return true, if that sound is playing.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isSoundPlaying(long soundId) {
        return InstanceManager.Client.playingSoundsCache.containsKey(soundId);
    }

    /**
     * Delete a sound from the server.
     * @param filename The name of the sound.
     * @param visibility The visibility.
     * @param owner The owner's UUID.
     * @param andThen This code will be executed after this process has been finished.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, Runnable andThen) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.runnableCache.put(requestId, andThen);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundDeleteRequestPacket(requestId, filename, owner.toString(), visibility));
    }

    /**
     * Delete a sound from the server.
     * @param filename The name of the sound.
     * @param visibility The visibility.
     * @param andThen This code will be executed after this process has been finished.
     */
    @OnlyIn(Dist.CLIENT) 
    @SuppressWarnings("resource")
    public static void deleteSound(String filename, ESoundVisibility visibility, Runnable andThen) {
        deleteSound(filename, Minecraft.getInstance().player.getUUID().toString(), visibility, andThen);
    }
}
