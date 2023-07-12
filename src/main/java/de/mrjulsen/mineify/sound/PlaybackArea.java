package de.mrjulsen.mineify.sound;

import java.io.Serializable;

import de.mrjulsen.mineify.config.ModCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class PlaybackArea implements Serializable {

    private EPlaybackAreaType type;
    private int volume;

    private int radius;

    private int x1, y1, z1;
    private int x2, y2, z2;

    public PlaybackArea(EPlaybackAreaType type, int volume, int radius, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.type = type;
        this.volume = volume;
        this.radius = radius;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public PlaybackArea(int radius, int distance) {
        this(EPlaybackAreaType.RADIUS, distance, radius, 0, 0, 0, 0, 0, 0);
    }

    public PlaybackArea(int x1, int y1, int z1, int x2, int y2, int z2, int distance) {
        this(EPlaybackAreaType.ZONE, distance, 0, x1, y1, z1, x2, y2, z2);
    }

    public PlaybackArea(PlaybackArea p) {
        this(p.getAreaType(), p.getVolume(), p.getRadius(), p.getX1(), p.getY1(), p.getZ1(), p.getX2(), p.getY2(), p.getZ2());
    }

    public EPlaybackAreaType getAreaType() {
        return this.type;
    }

    public int getRadius() {
        return this.radius;
    }

    public int getX1() {
        return this.x1;
    }

    public int getY1() {
        return this.y1;
    }

    public int getZ1() {
        return this.z1;
    }

    public int getX2() {
        return this.x2;
    }

    public int getY2() {
        return this.y2;
    }

    public int getZ2() {
        return this.z2;
    }

    public int getVolume() {
        return this.volume;
    }

    public void check() {
        this.x1 = Mth.clamp(x1, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        this.y1 = Mth.clamp(y1, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        this.z1 = Mth.clamp(z1, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        this.x2 = Mth.clamp(x2, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        this.y2 = Mth.clamp(y2, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        this.z2 = Mth.clamp(z2, -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
        
        this.radius = Mth.clamp(radius, 0, ModCommonConfig.MAX_RADIUS.get());        
        this.volume = Mth.clamp(volume, 0, ModCommonConfig.MAX_VOLUME.get());
    }

    public boolean isInZone(BlockPos relative, BlockPos pos) {
        return this.isInZone(relative.getX(), relative.getY(), relative .getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isInZone(double bx, double by, double bz, double x, double y, double z) {
        double minX = Math.min(this.getX1() + bx, this.getX2() + bx);
        double maxX = Math.max(this.getX1() + bx, this.getX2() + bx);
        double minY = Math.min(this.getY1() + by, this.getY2() + by);
        double maxY = Math.max(this.getY1() + by, this.getY2() + by);
        double minZ = Math.min(this.getZ1() + bz, this.getZ2() + bz);
        double maxZ = Math.max(this.getZ1() + bz, this.getZ2() + bz);

        return x >= minX && x <= maxX + 1 && y >= minY - 1 && y <= maxY + 1 && z >= minZ && z <= maxZ + 1;
    }


    public void setType(EPlaybackAreaType type) {
        this.type = type;        
    }

    public void setRadius(int r) {
        this.radius = r;
    }

    public void setX1(int v) {
        this.x1 = v;
    }

    public void setY1(int v) {
        this.y1 = v;
    }

    public void setZ1(int v) {
        this.z1 = v;
    }

    public void setX2(int v) {
        this.x2 = v;
    }

    public void setY2(int v) {
        this.y2 = v;
    }

    public void setZ2(int v) {
        this.z2 = v;
    }

    public void setVolume(int d) {
        this.volume = d;
    }



    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("type", this.getAreaType().getIndex());
        tag.putInt("volume", this.getVolume());
        switch (this.getAreaType()) {
            case ZONE:
                tag.putInt("x1", this.getX1());
                tag.putInt("y1", this.getY1());
                tag.putInt("z1", this.getZ1());
                tag.putInt("x2", this.getX2());
                tag.putInt("y2", this.getY2());
                tag.putInt("z2", this.getZ2());
                break;
            case RADIUS:
            default:
                tag.putInt("radius", this.getRadius());
                break;
        }

        return tag;
    }

    public static PlaybackArea fromNbt(CompoundTag tag) {
        EPlaybackAreaType type = EPlaybackAreaType.getPlaybackAreaTypeByIndex(tag.getByte("type"));
        int volume = tag.getInt("volume");
        int radius = tag.getInt("radius");
        int x1 = tag.getInt("x1");
        int y1 = tag.getInt("y1");
        int z1 = tag.getInt("z1");
        int x2 = tag.getInt("x2");
        int y2 = tag.getInt("y2");
        int z2 = tag.getInt("z2");

        return new PlaybackArea(type, volume, radius, x1, y1, z1, x2, y2, z2);
    }

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeByte(type.getIndex());
        buffer.writeInt(volume);
        buffer.writeInt(radius);
        buffer.writeInt(x1);
        buffer.writeInt(y1);
        buffer.writeInt(z1);
        buffer.writeInt(x2);
        buffer.writeInt(y2);
        buffer.writeInt(z2);
    }

    public static PlaybackArea deserialize(FriendlyByteBuf buffer) {
        EPlaybackAreaType type = EPlaybackAreaType.getPlaybackAreaTypeByIndex(buffer.readByte());
        int volume = buffer.readInt();
        int radius = buffer.readInt();
        int x1 = buffer.readInt();
        int y1 = buffer.readInt();
        int z1 = buffer.readInt();
        int x2 = buffer.readInt();
        int y2 = buffer.readInt();
        int z2 = buffer.readInt();

        return new PlaybackArea(type, volume, radius, x1, y1, z1, x2, y2, z2);
    }
}
