package de.mrjulsen.mineify.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.api.Api;
import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.screen.SoundBoardScreen;
import de.mrjulsen.mineify.client.screen.SoundPlayerConfigurationScreen;
import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
import de.mrjulsen.mineify.network.packets.DefaultServerResponsePacket;
import de.mrjulsen.mineify.network.packets.NextSoundDataResponsePacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.SoundFilesCountResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundListResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundModificationPacket;
import de.mrjulsen.mineify.network.packets.SoundModificationWithPathPacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.network.packets.StopSoundWithPathPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundCompletionPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.EStreamingMode;
import de.mrjulsen.mineify.sound.ModifiedSoundInstance;
import de.mrjulsen.mineify.sound.SoundBuffer;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;
import de.mrjulsen.mineify.util.Utils;
import de.mrjulsen.mineify.util.exceptions.ConfigException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client methods
 */
public class ClientWrapper {
    
    public static void showSoundSelectionScreen(SoundPlayerBlockEntity entity) {
        Minecraft.getInstance().setScreen(new SoundPlayerConfigurationScreen(entity));
    }

    public static void showSoundBoardScreen() {
        Minecraft.getInstance().setScreen(new SoundBoardScreen());
    }





    public static void handleDownloadSoundPacket(DownloadSoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (!ModClientConfig.ACTIVATION.get())
            return;

        if (!InstanceManager.Client.soundStreamCache.contains(packet.requestId)) {
            InstanceManager.Client.soundStreamCache.put(packet.requestId, new SoundBuffer(packet.maxLength, packet.requestId, packet.streamingMode == EStreamingMode.ON_REQUEST));
        }           
        
        InstanceManager.Client.soundStreamCache.get(packet.requestId).write(packet.data, packet.dataOffset, packet.data.length);
    }

    public static void handlePlaySoundPacket(PlaySoundPacket packet, Supplier<NetworkEvent.Context> ctx) {  
        if (!ModClientConfig.ACTIVATION.get())
            return;

        if (InstanceManager.Client.soundStreamCache.contains(packet.requestId)) {
            final SoundBuffer buff = InstanceManager.Client.soundStreamCache.get(packet.requestId);
            Minecraft.getInstance().getSoundManager().play(new ModifiedSoundInstance(new ResourceLocation("minecraft:ambient.cave"), buff, SoundSource.MASTER, packet.attenuationDistance, packet.volume, packet.pitch, packet.pos, packet.path));
        }
    }

    public static void handleErrorMessagePacket(ErrorMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {        
        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent(packet.message.titleTranslationKey), new TranslatableComponent(packet.message.descriptionTranslationKey, (Object[])packet.message.data)));   
    }

    public static void handleSoundListResponsePacket(SoundListResponsePacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (InstanceManager.Client.soundListConsumerCache.contains(packet.requestId)) {
            InstanceManager.Client.soundListConsumerCache.getAndRemove(packet.requestId).accept(packet.soundFiles);
        }
    }

    public static void handleSoundFilesCountResponsePacket(SoundFilesCountResponsePacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (InstanceManager.Client.longConsumerCache.contains(packet.requestId)) {
            InstanceManager.Client.longConsumerCache.getAndRemove(packet.requestId).accept(packet.count);
        }
    }

    public static void handleNextSoundDataResponsePacket(NextSoundDataResponsePacket packet, Supplier<NetworkEvent.Context> ctx) {

        if (InstanceManager.Client.soundStreamCache.contains(packet.requestId)) {
            final SoundBuffer buff = InstanceManager.Client.soundStreamCache.get(packet.requestId);
            new Thread(() -> {
                while (buff.currentIndexNeeded() != packet.index && buff.hasSpace(packet.data.length)) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) { }
                }
                buff.write(packet.data, 0, packet.data.length);
                buff.setHasNext(packet.hasNext);
            }).start();
            //InstanceManager.Client.soundStreamCache.remove(packet.requestId);
            //InstanceManager.Client.soundStreamCache.put(packet.requestId, buff);
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handleStopSoundPacket(StopSoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        stopSoundOnClient(packet.soundId);
    }

    public static void handleStopSoundWithPathPacket(StopSoundWithPathPacket packet, Supplier<NetworkEvent.Context> ctx) {
        stopSoundOnClient(packet.shortPath);
    }

    public static void handleSoundModificationPacket(SoundModificationPacket packet, Supplier<NetworkEvent.Context> ctx) {
        modifySoundOnClient(packet.soundId, packet.attenuationDistance, packet.pitch, packet.x, packet.y, packet.z);
    }

    public static void handleSoundModificationWithPathPacket(SoundModificationWithPathPacket packet, Supplier<NetworkEvent.Context> ctx) {
        modifySoundOnClient(packet.shortPath, packet.attenuationDistance, packet.pitch, packet.x, packet.y, packet.z);
    }

    public static void uploadFromClient(String srcPath, String filename, EUserSoundVisibility visibility, ESoundCategory category, AudioFileConfig config, UUID uploader, long usedBytes, Runnable andThen) {        
        Thread sendThread = new Thread(() -> { 
            final long requestId = Api.genRequestId();
            InstanceManager.Client.runnableCache.put(requestId, andThen);
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.convert"), new TextComponent(filename)));
                 
            try (InputStream stream = SoundUtils.convertToOGG(new FileInputStream(srcPath), config)) {
                final int maxSize = stream.available();
                
                if (maxSize > ModCommonConfig.MAX_FILE_SIZE_BYTES.get() && ModCommonConfig.MAX_FILE_SIZE_BYTES.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.file_too_large").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.file_too_large.details", IOUtils.formatBytes(maxSize), IOUtils.formatBytes(ModCommonConfig.MAX_FILE_SIZE_BYTES.get())).getString());
                }

                if (usedBytes > -1 && maxSize + usedBytes > ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() && ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.storage_full").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.storage_full.details", IOUtils.formatBytes(ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get())).getString());
                }

                stream.mark(maxSize);
                double duration = SoundUtils.calculateOggDuration(stream.readAllBytes());
                if (duration > ModCommonConfig.MAX_AUDIO_DURATION.get() && ModCommonConfig.MAX_AUDIO_DURATION.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.duration_too_long").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.duration_too_long.details", SoundUtils.formattedDuration((int)duration), SoundUtils.formattedDuration(ModCommonConfig.MAX_AUDIO_DURATION.get())).getString());
                }
                stream.reset();

                //Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.started"), new TextComponent(filename)));
                byte[] buffer = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                while (stream.read(buffer) != -1) {                    
                    NetworkManager.MOD_CHANNEL.sendToServer(new UploadSoundPacket(requestId, buffer, maxSize));
                }            

                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.completed"), new TextComponent(filename)));
                NetworkManager.MOD_CHANNEL.sendToServer(new UploadSoundCompletionPacket(requestId, uploader, visibility, filename, category));
            } catch (ConfigException e) {
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TextComponent(e.getMessage()), new TextComponent(e.getDetails())));
                InstanceManager.Client.runnableCache.remove(requestId);
            } catch (Exception e) {
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.error"), new TextComponent(e.getMessage())));
                InstanceManager.Client.runnableCache.remove(requestId);
                ModMain.LOGGER.error("An error occurred while uploading a sound file.");
                e.printStackTrace();
            }
        });

        sendThread.start();

    }

    public static void stopSoundOnClient(long soundId) {
        if (InstanceManager.Client.playingSoundsCache.contains(soundId)) {
            Minecraft.getInstance().getSoundManager().stop(InstanceManager.Client.playingSoundsCache.getAndRemove(soundId));
        }
    }

    public static void stopSoundOnClient(String shortPath) {
        InstanceManager.Client.playingSoundsCache.forEach(x -> shortPath == null || shortPath.isBlank() || x.getPath().equals(shortPath), (id, soundFile) -> {
            Minecraft.getInstance().getSoundManager().stop(InstanceManager.Client.playingSoundsCache.getAndRemove(id));
        });
    }

    public static void modifySoundOnClient(long soundId, @Nullable Integer attenuationDistance, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        if (InstanceManager.Client.playingSoundsCache.contains(soundId)) {
            InstanceManager.Client.playingSoundsCache.getAndRemove(soundId).modify(attenuationDistance, pitch, x, y, z);
        }
    }

    public static void modifySoundOnClient(String shortPath, @Nullable Integer attenuationDistance, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        InstanceManager.Client.playingSoundsCache.forEach(a -> shortPath == null || shortPath.isBlank() || a.getPath().equals(shortPath), (id, soundFile) -> {
            InstanceManager.Client.playingSoundsCache.get(id).modify(attenuationDistance, pitch, x, y, z);
        });
    }

    public static void handleRunnablePacket(DefaultServerResponsePacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (InstanceManager.Client.runnableCache.contains(packet.requestId)) {
            Utils.executeIfNotNull(InstanceManager.Client.runnableCache.getAndRemove(packet.requestId));
        }
    }
}
