package de.mrjulsen.mineify.api;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.SoundDeleteRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundFilesCountRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundFilesSizeRequestPacket;
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
        ClientWrapper.stopSoundOnClient(soundId);
    }

    /**
     * Stops any playing sound at the given location.
     * @param soundId The ID of the sound you want to stop.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void stopSound(String shortPath) {
        ClientWrapper.stopSoundOnClient(shortPath);
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
    @SuppressWarnings("resource")
    public static void uploadSound(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader, Runnable andThen) {        
        getPlayerSoundsSize(new String[] { Minecraft.getInstance().player.getStringUUID() }, (size) -> {
            ClientWrapper.uploadFromClient(srcPath, filename, visibility, config, uploader, size, andThen);
        });
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
     * Get a list of sounds available to the client. Returns null, if no sounds are available.
     * @param visibilityWhitelist an array of allowed repositories or {@code null} for everything.
     * @param usersWhitelist an array of allowed users or {@code null} for everything.
     * @param callback A consumer which contains the response data.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getSoundList(ESoundVisibility[] visibilityWhitelist, String[] usersWhitelist, Consumer<SoundFile[]> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.soundListConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundListRequestPacket(requestId, visibilityWhitelist, usersWhitelist));
    }

    /**
     * Get the count of all sounds available to the client.
     * @param visibilityWhitelist an array of allowed repositories or {@code null} for everything.
     * @param usersWhitelist an array of allowed users or {@code null} for everything.
     * @param callback A consumer which contains the response data.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getAvailableSoundsCount(ESoundVisibility[] visibilityWhitelist, String[] usersWhitelist, Consumer<Long> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.longConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundFilesCountRequestPacket(requestId, visibilityWhitelist, usersWhitelist));
    }

    /**
     * Get the size of all sounds available to the client.
     * @param visibilityWhitelist an array of allowed repositories or {@code null} for everything.
     * @param usersWhitelist an array of allowed users or {@code null} for everything.
     * @param callback A consumer which contains the response data.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getAvailableSoundsSize(ESoundVisibility[] visibilityWhitelist, String[] usersWhitelist, Consumer<Long> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.longConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundFilesSizeRequestPacket(requestId, visibilityWhitelist, usersWhitelist));
    }

    /**
     * Get the count of all sounds available to the client.
     * @param visibilityWhitelist an array of allowed repositories or {@code null} for everything.
     * @param usersWhitelist an array of allowed users or {@code null} for everything.
     * @param callback A consumer which contains the response data.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getPlayerSoundsCount(String[] usersWhitelist, Consumer<Long> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.longConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundFilesCountRequestPacket(requestId, Arrays.stream(EUserSoundVisibility.values()).map(x -> x.toESoundVisibility()).toArray(ESoundVisibility[]::new), usersWhitelist));
    }

    /**
     * Get the size of all sounds available to the client.
     * @param visibilityWhitelist an array of allowed repositories or {@code null} for everything.
     * @param usersWhitelist an array of allowed users or {@code null} for everything.
     * @param callback A consumer which contains the response data.
     */
    @OnlyIn(Dist.CLIENT) 
    public static void getPlayerSoundsSize(String[] usersWhitelist, Consumer<Long> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.longConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundFilesSizeRequestPacket(requestId, Arrays.stream(EUserSoundVisibility.values()).map(x -> x.toESoundVisibility()).toArray(ESoundVisibility[]::new), usersWhitelist));
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
        return InstanceManager.Client.playingSoundsCache.contains(soundId);
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
