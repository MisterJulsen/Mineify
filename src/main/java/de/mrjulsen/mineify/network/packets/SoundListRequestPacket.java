package de.mrjulsen.mineify.network.packets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.sound.SoundDataCache;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.IOUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SoundListRequestPacket {
    private long requestID;

    public SoundListRequestPacket(long requestId) {
        this.requestID = requestId;
    }

    public static void encode(SoundListRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
    }

    public static SoundListRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();

        SoundListRequestPacket instance = new SoundListRequestPacket(requestId);
        return instance;
    }

    public static void handle(SoundListRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                SoundFile[] soundFiles = getSounds();
                NetworkManager.MOD_CHANNEL.sendTo(new SoundListResponsePacket(packet.requestID, soundFiles), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }).start();
        });
        
        context.get().setPacketHandled(true);      
    }



    private static SoundFile[] getSounds() {
        List<SoundFile> soundFiles = Collections.synchronizedList(new ArrayList<>()); // Contains all visible sounds for that specific user

        // Add all server sounds
        searchForOggFiles(Constants.CUSTOM_SOUNDS_SERVER_PATH).forEach(path -> soundFiles.add(new SoundFile(path, "Server", ESoundVisibility.SERVER)));
        
        // Get all sounds of private folder
        forEachDirectoryInFolder(String.format("%s/%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, ESoundVisibility.PRIVATE.getName()), (userFolderPath, userFolderName) -> { 
            List<String> oggFiles = searchForOggFiles(userFolderPath);
            oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.PRIVATE)));
        });

        // Get all sounds of shared folder
        forEachDirectoryInFolder(String.format("%s/%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, ESoundVisibility.SHARED.getName()), (userFolderPath, userFolderName) -> {
            List<String> oggFiles = searchForOggFiles(userFolderPath);
            oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.SHARED)));
        });

        // Get all sounds of public folder
        forEachDirectoryInFolder(String.format("%s/%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, ESoundVisibility.PUBLIC.getName()), (userFolderPath, userFolderName) -> {
            List<String> oggFiles = searchForOggFiles(userFolderPath);
            oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.PUBLIC)));
        });

        applyCacheData(soundFiles);        

        return soundFiles.toArray(SoundFile[]::new);
    }

    private synchronized static void applyCacheData(List<SoundFile> sounds) {
        SoundDataCache cache = SoundDataCache.loadOrCreate(Constants.DEFAULT_SOUND_DATA_CACHE);
        sounds.forEach(x -> x.setCachedDurationInSeconds(cache.get(x.buildPath()).getDuration()));
        cache.save(Constants.DEFAULT_SOUND_DATA_CACHE);
    }

    /***** FILE SYSTEM *****/
    private static List<String> searchForOggFiles(String folder) {
        List<String> sounds = new ArrayList<>();
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            return sounds;
        }

        File[] files = new File(folder).listFiles();
        if (files == null) {
            return sounds;
        }

        for (File f : files) {
            if (f.isDirectory() || !IOUtils.getFileExtension(f.getName()).equals(Constants.SOUND_FILE_EXTENSION))
                continue;
            
            sounds.add(f.getPath());            
        }
        return sounds;
    }

    private static void forEachDirectoryInFolder(String folder, BiConsumer<String, String> consumer) {
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (!f.isDirectory())
                continue;
            
            consumer.accept(f.getPath(), f.getName());        
        }
    }
    
}

