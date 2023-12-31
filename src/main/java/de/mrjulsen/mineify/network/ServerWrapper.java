package de.mrjulsen.mineify.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.api.Api;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.client.ToastMessage;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.EStreamingMode;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.PlayerDependDataBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class ServerWrapper {

    public static long sendPlaySoundRequest(SoundFile file, ServerPlayer[] players, BlockPos pos, int attenuationDistance, float volume, float pitch) {
        if (players == null || players.length <= 0) {
            return 0;
        }
        
        final long requestId = Api.genRequestId();
        new Thread(() -> {  
            try {
                ModMain.LOGGER.debug("Sound requested.");
                final String path = file.buildPath();
                final String shortPath = file.buildShortPath();
                InstanceManager.Server.fileCache.put(requestId, new PlayerDependDataBuffer(new FileInputStream(path), Arrays.stream(players).map(ServerPlayer::getUUID).toArray(UUID[]::new)));
                PlayerDependDataBuffer stream = InstanceManager.Server.fileCache.get(requestId);
                final int maxLength = ModCommonConfig.STREAMING_MODE.get() == EStreamingMode.ON_REQUEST ? (Constants.PRE_BUFFER_MULTIPLIER * 2) * Constants.DEFAULT_DATA_BLOCK_SIZE : stream.length + Constants.DEFAULT_DATA_BLOCK_SIZE;
                byte[] buffer = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                int part = 0;
                int bytesRead = 0;
                
                while ((bytesRead = stream.readForAll(buffer)) != -1) {
                    for (ServerPlayer p : players) {
                        NetworkManager.sendToClient(new DownloadSoundPacket(requestId, 0, buffer, maxLength, ModCommonConfig.STREAMING_MODE.get()), p);
                        if (part == Constants.PRE_BUFFER_MULTIPLIER) {
                            NetworkManager.sendToClient(new PlaySoundPacket(requestId, pos, attenuationDistance, volume, pitch, shortPath), p);
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
                        NetworkManager.sendToClient(new PlaySoundPacket(requestId, pos, attenuationDistance, volume, pitch, shortPath), p);
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

    public static void getSoundList(ESoundCategory[] categories, ESoundVisibility[] visibilityWhitelist, String[] usersWhitelist, Consumer<SoundFile[]> callback) {
        long requestId = Api.genRequestId();
        InstanceManager.Client.soundListConsumerCache.put(requestId, callback);
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundListRequestPacket(requestId, visibilityWhitelist == null ? new ESoundVisibility[0] : visibilityWhitelist, usersWhitelist == null ? new String[0] : usersWhitelist, categories == null ? new ESoundCategory[0] : categories));
    }

    public static void deleteSound(String filename, String owner, ESoundVisibility visibility, ESoundCategory category, ServerPlayer player, Runnable andThen) {
        ModMain.LOGGER.debug("Delete sound '" + filename + "' started.");
        new Thread(() -> {                
            if (visibility != ESoundVisibility.SERVER) {
                int tries = 0;
                File f = new File(new SoundFile(filename, owner, visibility, category).buildPath());

                while (f.exists() && tries < 10) {
                    try {
                        f.delete();
                    } catch (Exception e) {
                        tries++;
                        if (tries >= 10 && player != null) {
                            NetworkManager.sendToClient(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", "Unable to delete sound file.")), player);
                        }
                    }
                }
            }

            andThen.run();
            ModMain.LOGGER.debug("Sound deleted.");
        }, "DeleteSound").start();
    }
}
