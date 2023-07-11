package de.mrjulsen.mineify.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.EStreamingMode;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.PlayerDependDataBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class SoundRequest {

    public static long sendRequestFromServer(SoundFile file, long soundId, ServerPlayer[] players, BlockPos pos, float volume) {
        final long requestId = System.nanoTime();
        new Thread(() -> {  
            try {
                
                ModMain.LOGGER.debug("Sound requested.");
                InstanceManager.Server.fileCache.put(requestId, new PlayerDependDataBuffer(new FileInputStream(file.buildPath()), Arrays.stream(players).map(ServerPlayer::getUUID).toArray(UUID[]::new)));
                PlayerDependDataBuffer stream = InstanceManager.Server.fileCache.get(requestId);
                final int maxLength = ModCommonConfig.STREAMING_MODE.get() == EStreamingMode.ON_REQUEST ? (Constants.PRE_BUFFER_MULTIPLIER * 2) * Constants.DEFAULT_DATA_BLOCK_SIZE : stream.length + Constants.DEFAULT_DATA_BLOCK_SIZE;
                byte[] buffer = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                int part = 0;
                int bytesRead = 0;
                
                while ((bytesRead = stream.readForAll(buffer)) != -1) {
                    for (ServerPlayer p : players) {
                        NetworkManager.MOD_CHANNEL.sendTo(new DownloadSoundPacket(requestId, 0, buffer, maxLength, ModCommonConfig.STREAMING_MODE.get()), p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                        if (part == Constants.PRE_BUFFER_MULTIPLIER) {
                            NetworkManager.MOD_CHANNEL.sendTo(new PlaySoundPacket(requestId, pos, volume), p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                        }
                    }                    

                    if (part >= Constants.PRE_BUFFER_MULTIPLIER) {
                        if (ModCommonConfig.STREAMING_MODE.get() == EStreamingMode.ON_REQUEST) {
                            Thread.yield();
                            return; // The client must ask for more, so this method is done.
                        }    
                    }

                    if (InstanceManager.Server.streamCache.containsKey(requestId)) {
                        Thread.yield();
                        return;
                    }

                    part++;
                }

                for (ServerPlayer p : players) {
                    if (bytesRead == -1 && part < Constants.PRE_BUFFER_MULTIPLIER) {
                        NetworkManager.MOD_CHANNEL.sendTo(new PlaySoundPacket(requestId, pos, volume), p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
                InstanceManager.Server.closeFileStream(requestId);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Thread.yield();
            }

            
            ModMain.LOGGER.debug("Sound request finished.");
        }, "SoundRequest").start();

        return requestId;
    }

    public static void uploadFromClient(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader, long usedBytes) {        
        ClientWrapper.uploadFromClient(srcPath, filename, visibility, config, uploader, usedBytes);
    }

    public static void getSoundListFromServer(Consumer<SoundFile[]> callback) {
        long requestId = System.nanoTime();
        InstanceManager.Client.consumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundListRequestPacket(requestId));
    }

    public static void stopSoundOnClient(long soundId) {
        ClientWrapper.stopSoundOnClient(soundId);
    }
}
