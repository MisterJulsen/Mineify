package de.mrjulsen.mineify.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.InstanceManager;
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class SoundRequest {

    public static long sendRequestFromServer(SoundFile file, long soundId, ServerPlayer[] players, BlockPos pos, float volume) {
        final long requestId = System.nanoTime();
        Thread receiveThread = new Thread(() -> {  
            try {
                InputStream stream = new FileInputStream(file.buildPath());
                final int maxLength = stream.available();
                byte[] buffer = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                while (stream.read(buffer) != -1) {
                    for (ServerPlayer p : players) {
                        NetworkManager.MOD_CHANNEL.sendTo(new DownloadSoundPacket(requestId, pos, 0, buffer, maxLength, volume), p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
                stream.close();
                System.out.println("Sound reading complete. Playing on " + players.length + " players.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Thread.yield();
            }
        });

        receiveThread.start();

        return requestId;
    }

    public static void uploadFromClient(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader) {        
        ClientWrapper.uploadFromClient(srcPath, filename, visibility, config, uploader);
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
