package de.mrjulsen.mineify.sound;

import java.io.File;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.UploaderUsercache;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;
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

    public boolean canModify(UUID uuid) {
        return this.canModify(uuid.toString());
    }

    public boolean canModify(String uuid) {
        return this.getVisibility() == ESoundVisibility.PUBLIC || (this.getVisibility() == ESoundVisibility.PRIVATE && this.getOwner().equals(uuid));
    }

    public boolean exists() {
        return new File(this.buildPath()).exists();
    }

    public final int readDurationInSeconds() {
        return calcDurationSeconds();
    }

    public final int calcDuration() {
        SoundDataCache cache = SoundDataCache.loadOrCreate(Constants.DEFAULT_SOUND_DATA_CACHE);
        this.setCachedDurationInSeconds(cache.get(this.buildPath()).getDuration());
        cache.save(Constants.DEFAULT_SOUND_DATA_CACHE);

        return this.getDurationInSeconds();
    }

    public final int getDurationInSeconds() {
        return this.cachedDuration;
    }

    public void setCachedDurationInSeconds(int duration) {
        this.cachedDuration = duration;
    }

    @OnlyIn(Dist.CLIENT)
    public final LocalTime getDuration() {
        int seconds = this.getDurationInSeconds();
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return LocalTime.of(hours, minutes, secs);
    }

    public String buildPath() {
        return SoundUtils.buildPath(filename, ownerUUID, visibility);
    }

    public String buildShortPath() {
        return SoundUtils.buildShortPath(filename, ownerUUID, visibility);
    }

    private int calcDurationSeconds() {
        try {
            String oggFilePath = this.buildPath();
            return (int)SoundUtils.calculateOggDuration(oggFilePath);
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
        
        return new SoundFile(SoundUtils.buildPath(filename, owner, visibility), owner, visibility);
    }

    public static SoundFile fromShortPath(String shortPath) {
        String[] data = shortPath.split("/");

        if (data.length < 2) {
            return null;
        }

        SoundFile file = null;
        if (data.length == 2) {
            file = new SoundFile(data[1], data[0], ESoundVisibility.SERVER);
        } else {
            file = new SoundFile(data[2], data[0], ESoundVisibility.getVisibilityByName(data[1]));
        }

        return file;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
