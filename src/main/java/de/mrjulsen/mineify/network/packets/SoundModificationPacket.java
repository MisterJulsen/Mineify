package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoundModificationPacket {
    public final long soundId;
    public final @Nullable Float volume;
    public final @Nullable Float pitch;
    public final @Nullable Double x;
    public final @Nullable Double y;
    public final @Nullable Double z;

    public SoundModificationPacket(long soundId, @Nullable Float volume, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        this.soundId = soundId;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(SoundModificationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.soundId);

        boolean b;
        b = packet.volume != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeFloat(packet.volume);

        b = packet.pitch != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeFloat(packet.pitch);
        
        b = packet.x != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeDouble(packet.x);
        
        b = packet.y != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeDouble(packet.y);
        
        b = packet.y != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeDouble(packet.y);
    }

    public static SoundModificationPacket decode(FriendlyByteBuf buffer) {
        long id = buffer.readLong();

        @Nullable Float volume = null;
        @Nullable Float pitch = null;
        @Nullable Double x = null;
        @Nullable Double y = null;
        @Nullable Double z = null;

        if (buffer.readBoolean()) volume = buffer.readFloat();
        if (buffer.readBoolean()) pitch = buffer.readFloat();
        if (buffer.readBoolean()) x = buffer.readDouble();
        if (buffer.readBoolean()) y = buffer.readDouble();
        if (buffer.readBoolean()) z = buffer.readDouble();

        SoundModificationPacket instance = new SoundModificationPacket(id, volume, pitch, x, y, z);
        return instance;
    }

    public static void handle(SoundModificationPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleSoundModificationPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
