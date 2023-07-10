package de.mrjulsen.mineify.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.screen.SoundPlayerConfigurationScreen;
import de.mrjulsen.mineify.client.screen.PlaylistScreen;
import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.SoundRequest;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
import de.mrjulsen.mineify.network.packets.NextSoundDataResponsePacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.RefreshSoundListPacket;
import de.mrjulsen.mineify.network.packets.SoundListResponsePacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundCompletionPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundPacket;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ExtendedSoundInstance;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.ReadWriteBuffer;
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

public class ClientWrapper {
    
    public static void showSoundSelectionScreen(SoundPlayerBlockEntity entity) {
        Minecraft.getInstance().setScreen(new SoundPlayerConfigurationScreen(entity));
    }

    public static void handleDownloadSoundPacket(DownloadSoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (!ModClientConfig.ACTIVATION.get())
            return;

        if (!InstanceManager.Client.soundStreamCache.containsKey(packet.requestId)) {
            InstanceManager.Client.soundStreamCache.put(packet.requestId, new ReadWriteBuffer(packet.maxLength, packet.requestId, packet.streamRequest));
        }           
        
        InstanceManager.Client.soundStreamCache.get(packet.requestId).write(packet.data, packet.dataOffset, packet.data.length);
    }

    public static void handlePlaySoundPacket(PlaySoundPacket packet, Supplier<NetworkEvent.Context> ctx) {  
        if (!ModClientConfig.ACTIVATION.get())
            return;

        if (InstanceManager.Client.soundStreamCache.containsKey(packet.requestId)) {
            final ReadWriteBuffer buff = InstanceManager.Client.soundStreamCache.get(packet.requestId);
            Minecraft.getInstance().getSoundManager().play(new ExtendedSoundInstance(new ResourceLocation("minecraft:ambient.cave"), buff, SoundSource.MASTER, packet.volume, packet.pos));
        }
    }

    public static void handleErrorMessagePacket(ErrorMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {        
        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent(packet.message.titleTranslationKey), new TranslatableComponent(packet.message.descriptionTranslationKey, (Object[])packet.message.data)));   
    }

    @SuppressWarnings("resource")
    public static void handleRefreshSoundListPacket(RefreshSoundListPacket packet, Supplier<NetworkEvent.Context> ctx) {        
        if (Minecraft.getInstance().screen instanceof PlaylistScreen screen) {
            screen.reload(); 
        }
        
    }

    public static void handleSoundListResponsePacket(SoundListResponsePacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (InstanceManager.Client.consumerCache.containsKey(packet.requestId)) {
            InstanceManager.Client.consumerCache.get(packet.requestId).accept(packet.soundFiles);
            InstanceManager.Client.consumerCache.remove(packet.requestId);
        }
    }

    public static void handleNextSoundDataResponsePacket(NextSoundDataResponsePacket packet, Supplier<NetworkEvent.Context> ctx) {

        if (InstanceManager.Client.soundStreamCache.containsKey(packet.requestId)) {
            final ReadWriteBuffer buff = InstanceManager.Client.soundStreamCache.get(packet.requestId);
            new Thread(() -> {
                while (buff.currentIndexNeeded() != packet.index && buff.hasSpace(packet.data.length)) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) { }
                }
                buff.write(packet.data, 0, packet.data.length);
                buff.setHasNext(packet.hasNext);
            }).start();
            InstanceManager.Client.soundStreamCache.remove(packet.requestId);
            InstanceManager.Client.soundStreamCache.put(packet.requestId, buff);
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handleStopSoundPacket(StopSoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        SoundRequest.stopSoundOnClient(packet.soundId);
    }

    public static void uploadFromClient(String srcPath, String filename, EUserSoundVisibility visibility, AudioFileConfig config, UUID uploader, long usedBytes) {        
        Thread sendThread = new Thread(() -> { 
            final long requestId = System.nanoTime();
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.convert"), new TextComponent(filename)));
                 
            try (InputStream stream = IOUtils.convertToOGG(new FileInputStream(srcPath), config)) {
                final int maxSize = stream.available();
                
                if (maxSize > ModCommonConfig.MAX_FILE_SIZE_BYTES.get() && ModCommonConfig.MAX_FILE_SIZE_BYTES.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.file_too_large").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.file_too_large.details", IOUtils.formatBytes(maxSize), IOUtils.formatBytes(ModCommonConfig.MAX_FILE_SIZE_BYTES.get())).getString());
                }

                if (maxSize + usedBytes > ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() && ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.storage_full").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.storage_full.details", IOUtils.formatBytes(ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get())).getString());
                }

                stream.mark(maxSize);
                double duration = Utils.calculateOggDuration(stream.readAllBytes());
                if (duration > ModCommonConfig.MAX_AUDIO_DURATION.get() && ModCommonConfig.MAX_AUDIO_DURATION.get() >= 0) {
                    throw new ConfigException(new TranslatableComponent("gui.mineify.soundselection.upload.duration_too_long").getString(), new TranslatableComponent("gui.mineify.soundselection.upload.duration_too_long.details", Utils.formattedDuration((int)duration), Utils.formattedDuration(ModCommonConfig.MAX_AUDIO_DURATION.get())).getString());
                }
                stream.reset();

                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.started"), new TextComponent(filename)));
                byte[] buffer = new byte[Constants.DEFAULT_DATA_BLOCK_SIZE];
                while (stream.read(buffer) != -1) {                    
                    NetworkManager.MOD_CHANNEL.sendToServer(new UploadSoundPacket(requestId, buffer, maxSize));
                }
                NetworkManager.MOD_CHANNEL.sendToServer(new UploadSoundCompletionPacket(requestId, uploader, visibility, filename));                
            } catch (ConfigException e) {
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TextComponent(e.getMessage()), new TextComponent(e.getDetails())));
            } catch (Exception e) {
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.error"), new TextComponent(e.getMessage())));
                e.printStackTrace();
            } finally {
                Thread.yield();
            }
        });

        sendThread.start();

    }

    public static void stopSoundOnClient(long soundId) {
        if (InstanceManager.Client.playingSoundsCache.containsKey(soundId)) {
            Minecraft.getInstance().getSoundManager().stop(InstanceManager.Client.playingSoundsCache.get(soundId));
        }
    }
}
