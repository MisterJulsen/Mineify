package de.mrjulsen.mineify.network.packets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.ToastMessage;
import de.mrjulsen.mineify.network.UploaderUsercache;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class UploadSoundCompletionPacket {
    private final long requestId;
    private final UUID uploaderUUID;
    private final EUserSoundVisibility visibility;
    private final String filename;

    public UploadSoundCompletionPacket(long requestId, UUID uploader, EUserSoundVisibility visibility, String filename) {
        this.uploaderUUID = uploader;
        this.visibility = visibility;
        this.filename = filename;
        this.requestId = requestId;
    }

    public static void encode(UploadSoundCompletionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeUUID(packet.uploaderUUID);
        buffer.writeEnum(packet.visibility);
        buffer.writeUtf(packet.filename);
    }

    public static UploadSoundCompletionPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        UUID uploader = buffer.readUUID();
        EUserSoundVisibility visibility = buffer.readEnum(EUserSoundVisibility.class);
        String filename = buffer.readUtf();

        UploadSoundCompletionPacket instance = new UploadSoundCompletionPacket(requestId, uploader, visibility, filename);
        return instance;
    }

    public static void handle(UploadSoundCompletionPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                if (InstanceManager.Server.streamCache.containsKey(packet.requestId)) {
                    String dirPath = IOUtils.getSoundDirectoryPath(packet.visibility.toESoundVisibility(), packet.uploaderUUID);
                    String soundPath = IOUtils.getSoundPath(packet.filename, packet.visibility.toESoundVisibility(), packet.uploaderUUID);
                    IOUtils.createDirectory(dirPath); 
                    try (FileOutputStream outputStream = new FileOutputStream(soundPath)) {
                        InstanceManager.Server.streamCache.get(packet.requestId).writeTo(outputStream);
                    } catch (IOException e) {
                        NetworkManager.MOD_CHANNEL.sendTo(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", "Unable to upload sound file.")), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                        e.printStackTrace();
                    }

                    try {
                        InstanceManager.Server.streamCache.get(packet.requestId).close();
                    } catch (IOException e) {
                        NetworkManager.MOD_CHANNEL.sendTo(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", e.getMessage())), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                        e.printStackTrace();
                    }
                    InstanceManager.Server.streamCache.remove(packet.requestId);
                }

                UploaderUsercache.INSTANCE.add(packet.uploaderUUID.toString());
                UploaderUsercache.INSTANCE.save(Constants.DEFAULT_USERCACHE_PATH);

                Utils.giveAdvancement(context.get().getSender(), "first_upload", "requirement");

                NetworkManager.MOD_CHANNEL.sendTo(new RefreshSoundListPacket(), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }).start();
        });
        
        context.get().setPacketHandled(true);      
    }    
}
