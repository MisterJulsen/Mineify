package de.mrjulsen.mineify.api;

import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.ServerWrapper;
import de.mrjulsen.mineify.network.packets.DefaultServerResponsePacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * This class contains all methods that should only be called on the server side.
 */
public class ServerApi {
    
    /**
     * Play a custom sound stored on the server.
     * @param file The sound file data.
     * @param players An array of player which should hear that sound.
     * @param pos The position at which the sound should be played.
     * @param volume The volume of the sound.
     * @return The ID of the sound, which can be used to stop or modify the sound while it's playing. This ID is invalid after the sound playback has been finished.
     */
    public static long playSound(SoundFile file, ServerPlayer[] players, BlockPos pos, float volume) {
        return ServerWrapper.sendPlaySoundRequest(file, players, pos, volume);
    }
    
    /**
     * Stop a playing custom sound.
     * @param soundId The ID of the sound you want to stop.
     * @param players The players which should be affected by this action.
     */
    public static void stopSound(long soundId, ServerPlayer[] players) {
        for (ServerPlayer p : players) {
            NetworkManager.sendToClient(new StopSoundPacket(soundId), p);             
        }
    }

    /**
     * Delete a sound on the server.
     * @param filename The filename of the sound.
     * @param owner The owner of the sound.
     * @param visibility The visibility.
     * @param player Notifies this player if an error occurs.
     * @param andThen This code will be executed after the process has been finished.
     */
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, ServerPlayer player, Runnable andThen) {
        ServerWrapper.deleteSound(filename, owner, visibility, player, andThen);
    }

    /**
     * Delete a sound on the server.
     * @param filename The filename of the sound.
     * @param owner The owner of the sound.
     * @param visibility The visibility.
     * @param andThen This code will be executed after the process has been finished.
     */
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, Runnable andThen) {
        deleteSound(filename, owner, visibility, null, andThen);
    }

    /**
     * Delete a sound on the server.
     * @param filename The filename of the sound.
     * @param owner The owner of the sound.
     * @param visibility The visibility.
     */
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility) {
        deleteSound(filename, owner, visibility, null, null);
    }

    /**
     * Sends a response to the client and executes the next code block if available.
     * @param player The player the server should respond to.
     * @param requestId The ID of the Runnable object which should be executed after receiving the packet.
     */
    public static void sendResponse(ServerPlayer player, long requestId) {
        NetworkManager.sendToClient(new DefaultServerResponsePacket(requestId), player);
    }
}
