package de.mrjulsen.mineify.sound;

import java.io.File;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.UploaderUsercache;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoundFile implements Serializable {
    private final String filename;
    private final String ownerUUID;
    private final String path;
    private final long size;
    private final ESoundVisibility visibility;

    private int cachedDuration = 0;

    public SoundFile(String path, String ownerUUID, ESoundVisibility visibility) {
        this.path = path;
        this.filename = IOUtils.getFileNameWithoutExtension(path);
        this.ownerUUID = ownerUUID;  
        this.size = new File(path).length();
        this.visibility = visibility;
    }

    private SoundFile(String path, String ownerUUID, ESoundVisibility visibility, String filename, long size, int cachedDuration) {
        this.path = path;
        this.filename = filename;
        this.ownerUUID = ownerUUID;  
        this.size = size;
        this.visibility = visibility;
        this.cachedDuration = cachedDuration;
    } 

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeUtf(filename);
        buffer.writeUtf(ownerUUID);
        buffer.writeUtf(path);
        buffer.writeLong(size);
        buffer.writeInt(visibility.getIndex());
        buffer.writeInt(cachedDuration);
    }

    public static SoundFile deserialize(FriendlyByteBuf buffer) {
        String filename = buffer.readUtf();
        String ownerUUID = buffer.readUtf();
        String path = buffer.readUtf();
        long size = buffer.readLong();
        ESoundVisibility visibility = ESoundVisibility.getVisibilityByIndex(buffer.readInt());
        int cachedDuration = buffer.readInt();

        return new SoundFile(path, ownerUUID, visibility, filename, size, cachedDuration);
    }

    public final String getName() {
        return this.filename;
    }
    
    public final String getOwner() {
        return ownerUUID;
    }

    public final ESoundVisibility getVisibility() {
        return this.visibility;
    }

    public final String getNameOfOwner(boolean pullMissing) {
        return ownerUUID == null ? "Unknown" : UploaderUsercache.INSTANCE.get(this.ownerUUID, pullMissing);
    }

    public final String getFilePath() {
        return this.path;
    }

    public final long getSize() {
        return this.size;
    }

    public final String getSizeFormatted() {
        return IOUtils.formatBytes(this.getSize());
    }

    public boolean visibleFor(UUID uuid) {
        return this.visibleFor(uuid.toString());
    }

    public boolean visibleFor(String uuid) {
        return this.getVisibility() != ESoundVisibility.PRIVATE || this.getOwner().equals(uuid);
    }

    

    public final int readDurationInSeconds() {
        return calcDurationSeconds(this.getName(), this.getOwner(), this.getVisibility());
    }

    public final int calcDuration() {
        SoundDataCache cache = SoundDataCache.loadOrCreate(Constants.DEFAULT_SOUND_DATA_CACHE);
        this.setCachedDurationInSeconds(cache.get(this.buildPath()).getDuration());
        cache.save(Constants.DEFAULT_SOUND_DATA_CACHE);

        return this.getDurationInSecondsFromCache();
    }

    public final int getDurationInSecondsFromCache() {
        return this.cachedDuration;
    }

    public void setCachedDurationInSeconds(int duration) {
        this.cachedDuration = duration;
    }

    @OnlyIn(Dist.CLIENT)
    public final LocalTime getDuration() {
        int seconds = this.getDurationInSecondsFromCache();
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return LocalTime.of(hours, minutes, secs);
    }

    public String buildPath() {
        if (this.getVisibility() == ESoundVisibility.SERVER) {
            return String.format("%s/%s.ogg", Constants.CUSTOM_SOUNDS_SERVER_PATH, this.getName());
        } else {
            return String.format("%s/%s/%s/%s.ogg", Constants.CUSTOM_SOUNDS_SERVER_PATH, this.getVisibility().getName(), this.getOwner(), this.getName());
        }
    }

    public static String buildPath(String filename, String owner, ESoundVisibility visibility) {
        if (visibility == ESoundVisibility.SERVER) {
            return String.format("%s/%s.ogg", Constants.CUSTOM_SOUNDS_SERVER_PATH, filename);
        } else {
            return String.format("%s/%s/%s/%s.ogg", Constants.CUSTOM_SOUNDS_SERVER_PATH, visibility.getName(), owner, filename);
        }
    }

    /* UNUSED
    private static int calcDurationSecondsClient(String filename, String owner, ESoundVisibility visibility) {
        float lengthSeconds = 0;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            String filePath = SoundFile.buildPath(filename, owner, visibility);
            IntBuffer intbuffer = memorystack.mallocInt(1);
            long handle = STBVorbis.stb_vorbis_open_filename(filePath, intbuffer, (STBVorbisAlloc)null);            
            STBVorbisInfo stbvorbisinfo = STBVorbisInfo.mallocStack(memorystack);
            STBVorbis.stb_vorbis_get_info(handle, stbvorbisinfo);
            lengthSeconds = STBVorbis.stb_vorbis_stream_length_in_seconds(handle);
            STBVorbis.stb_vorbis_close(handle);
        } catch (Exception e) {
        }
         
        return (int)lengthSeconds;
    }
    */

    private static int calcDurationSeconds(String filename, String owner, ESoundVisibility visibility) {
       
        try {
            String oggFilePath = buildPath(filename, owner, visibility);
            return (int)Utils.calculateOggDuration(oggFilePath);
        } catch (Exception e) { return 0;}
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof SoundFile s) {
            return
                filename.equals(s.getName()) && 
                ownerUUID.equals(s.getOwner()) &&
                visibility.equals(s.getVisibility());
        }
        return false;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("filename", this.getName());
        tag.putInt("visibility", this.getVisibility().getIndex());
        tag.putString("owner", this.getOwner());
        return tag;
    }

    public static SoundFile fromNbt(CompoundTag tag) {
        String filename = tag.getString("filename");
        ESoundVisibility visibility = ESoundVisibility.getVisibilityByIndex(tag.getInt("visibility"));
        String owner = tag.getString("owner");
        
        return new SoundFile(SoundFile.buildPath(filename, owner, visibility), owner, visibility);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
