package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoundModificationWithPathPacket {
    public final String shortPath;
    public final @Nullable Integer attenuationDistance;
    public final @Nullable Float pitch;
    public final @Nullable Float volume;
    public final @Nullable Double x;
    public final @Nullable Double y;
    public final @Nullable Double z;

    public SoundModificationWithPathPacket(String shortPath, @Nullable Integer attenuationDistance, @Nullable Float volume, @Nullable Float pitch, @Nullable Double x, @Nullable Double y, @Nullable Double z) {
        this.shortPath = shortPath;
        this.attenuationDistance = attenuationDistance;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(SoundModificationWithPathPacket packet, FriendlyByteBuf buffer) {
        if (packet.shortPath == null) {
            buffer.writeInt(0);
        } else {
            buffer.writeInt(packet.shortPath.getBytes(StandardCharsets.UTF_8).length);
            buffer.writeUtf(packet.shortPath);
        }

        boolean b;
        b = packet.attenuationDistance != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeInt(packet.attenuationDistance);

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
        
        b = packet.z != null;
        buffer.writeBoolean(b);
        if (b) buffer.writeDouble(packet.z);
    }

    public static SoundModificationWithPathPacket decode(FriendlyByteBuf buffer) {
        int l = buffer.readInt();
        String shortPath = null;
        if (l > 0) { 
            shortPath = buffer.readUtf(l);
        }

        @Nullable Integer attenuationDistance = null;
        @Nullable Float volume = null;
        @Nullable Float pitch = null;
        @Nullable Double x = null;
        @Nullable Double y = null;
        @Nullable Double z = null;

        if (buffer.readBoolean()) attenuationDistance = buffer.readInt();
        if (buffer.readBoolean()) volume = buffer.readFloat();
        if (buffer.readBoolean()) pitch = buffer.readFloat();
        if (buffer.readBoolean()) x = buffer.readDouble();
        if (buffer.readBoolean()) y = buffer.readDouble();
        if (buffer.readBoolean()) z = buffer.readDouble();

        SoundModificationWithPathPacket instance = new SoundModificationWithPathPacket(shortPath, attenuationDistance, volume, pitch, x, y, z);
        return instance;
    }

    public static void handle(SoundModificationWithPathPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleSoundModificationWithPathPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
