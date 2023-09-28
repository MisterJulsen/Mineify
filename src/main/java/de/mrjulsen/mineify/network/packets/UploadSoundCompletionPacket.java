package de.mrjulsen.mineify.network.packets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.client.ToastMessage;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.UploaderUsercache;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class UploadSoundCompletionPacket {
    private final long requestId;
    private final UUID uploaderUUID;
    private final EUserSoundVisibility visibility;
    private final ESoundCategory category;
    private final String filename;

    public UploadSoundCompletionPacket(long requestId, UUID uploader, EUserSoundVisibility visibility, String filename, ESoundCategory category) {
        this.uploaderUUID = uploader;
        this.visibility = visibility;
        this.filename = filename;
        this.requestId = requestId;
        this.category = category;
    }

    public static void encode(UploadSoundCompletionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeUUID(packet.uploaderUUID);
        buffer.writeEnum(packet.visibility);
        buffer.writeEnum(packet.category);
        buffer.writeUtf(packet.filename);
    }

    public static UploadSoundCompletionPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        UUID uploader = buffer.readUUID();
        EUserSoundVisibility visibility = buffer.readEnum(EUserSoundVisibility.class);
        ESoundCategory category = buffer.readEnum(ESoundCategory.class);
        String filename = buffer.readUtf();

        UploadSoundCompletionPacket instance = new UploadSoundCompletionPacket(requestId, uploader, visibility, filename, category);
        return instance;
    }

    public static void handle(UploadSoundCompletionPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                
                ModMain.LOGGER.debug("Finishing sound upload...");
                if (InstanceManager.Server.streamCache.containsKey(packet.requestId)) {
                    String dirPath = SoundUtils.getSoundDirectoryPath(packet.visibility.toESoundVisibility(), packet.uploaderUUID, packet.category);
                    String soundPath = SoundUtils.getSoundPath(packet.filename, packet.visibility.toESoundVisibility(), packet.uploaderUUID, packet.category);
                    IOUtils.createDirectory(dirPath); 
                    try (FileOutputStream outputStream = new FileOutputStream(soundPath)) {
                        InstanceManager.Server.streamCache.get(packet.requestId).writeTo(outputStream);
                    } catch (IOException e) {
                        NetworkManager.sendToClient(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", "Unable to upload sound file.")), context.get().getSender());
                        e.printStackTrace();
                    }

                    try {
                        InstanceManager.Server.streamCache.get(packet.requestId).close();
                    } catch (IOException e) {
                        NetworkManager.sendToClient(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", e.getMessage())), context.get().getSender());
                        e.printStackTrace();
                    }
                    InstanceManager.Server.streamCache.remove(packet.requestId);
                }

                UploaderUsercache.INSTANCE.add(packet.uploaderUUID.toString());
                UploaderUsercache.INSTANCE.save(Constants.DEFAULT_USERCACHE_PATH);

                Utils.giveAdvancement(context.get().getSender(), "first_upload", "requirement");
                ModMain.LOGGER.debug("Sound upload finished.");

                ServerApi.sendResponse(context.get().getSender(), packet.requestId);
            }).start();
        });
        
        context.get().setPacketHandled(true);      
    }    
}
