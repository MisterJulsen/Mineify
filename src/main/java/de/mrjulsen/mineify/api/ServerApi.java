package de.mrjulsen.mineify.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.ServerWrapper;
import de.mrjulsen.mineify.network.packets.DefaultServerResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundModificationPacket;
import de.mrjulsen.mineify.network.packets.SoundModificationWithPathPacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.network.packets.StopSoundWithPathPacket;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
     * @return The ID of the sound, which can be used to stop or modify the sound while it's playing, or 0 if no players are given. This ID is invalid after the sound playback has been finished.
     */
    @Nonnull
    public static long playSound(SoundFile file, ServerPlayer[] players, BlockPos pos, int attenuationDistance, float volume, float pitch) {
        return ServerWrapper.sendPlaySoundRequest(file, players, pos, attenuationDistance, volume, pitch);
    }

    @Nonnull
    public static long playSound(SoundFile file, PlaybackArea area, Level level, BlockPos pos, int attenuationDistance, float volume, float pitch) {
        return ServerWrapper.sendPlaySoundRequest(file, getAffectedPlayers(area, level, pos), pos, attenuationDistance, volume, pitch);
    }

    public static ServerPlayer[] getAffectedPlayers(PlaybackArea areaDefinition, Level level, BlockPos pos) {
        switch (areaDefinition.getAreaType()) {
            case ZONE:
                return level.players().stream().filter(p -> p instanceof ServerPlayer && areaDefinition.isInZone(pos.getX(), pos.getY(), pos.getZ(), p.position().x(), p.position().y(), p.position().z())).toArray(ServerPlayer[]::new);
            case RADIUS:
            default:
                return level.players().stream().filter(p -> p instanceof ServerPlayer && p.position().distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) <= areaDefinition.getRadius()).toArray(ServerPlayer[]::new);
        }
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
     * Stop a playing custom sound.
     * @param shortPath The sound's location or null for all sounds.
     * @param players The players which should be affected by this action.
     */
    public static void stopSound(String shortPath, ServerPlayer[] players) {
        for (ServerPlayer p : players) {
            NetworkManager.sendToClient(new StopSoundWithPathPacket(shortPath), p);             
        }
    }

    public static void modifySound(long soundId, ServerPlayer[] players, @Nullable Integer attenuationDistance, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        for (ServerPlayer p : players) {
            NetworkManager.sendToClient(new SoundModificationPacket(soundId, attenuationDistance, pitch, x, y, z), p);             
        }
    }
    
    public static void modifySound(String shortPath, ServerPlayer[] players, @Nullable Integer attenuationDistance, @Nullable Float volume, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        for (ServerPlayer p : players) {
            NetworkManager.sendToClient(new SoundModificationWithPathPacket(shortPath, attenuationDistance, volume, pitch, x, y, z), p);             
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
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, ESoundCategory category, ServerPlayer player, Runnable andThen) {
        ServerWrapper.deleteSound(filename, owner, visibility, category, player, andThen);
    }

    /**
     * Delete a sound on the server.
     * @param filename The filename of the sound.
     * @param owner The owner of the sound.
     * @param visibility The visibility.
     * @param andThen This code will be executed after the process has been finished.
     */
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, ESoundCategory category, Runnable andThen) {
        deleteSound(filename, owner, visibility, category, null, andThen);
    }

    /**
     * Delete a sound on the server.
     * @param filename The filename of the sound.
     * @param owner The owner of the sound.
     * @param visibility The visibility.
     */
    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, ESoundCategory category) {
        deleteSound(filename, owner, visibility, category, null);
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
