package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoundListResponsePacket {
    public long requestId;
    public SoundFile[] soundFiles;

    public SoundListResponsePacket(long requestId, SoundFile[] soundFiles) {
        this.requestId = requestId;
        this.soundFiles = soundFiles;
    }

    public static void encode(SoundListResponsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.soundFiles.length);
        for (int i = 0; i < packet.soundFiles.length; i++) {
            packet.soundFiles[i].serialize(buffer);
        }
    }

    public static SoundListResponsePacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int l = buffer.readInt();
        SoundFile[] soundFiles = new SoundFile[l];
        for (int i = 0; i < soundFiles.length; i++) {
            soundFiles[i] = SoundFile.deserialize(buffer);
        }

        SoundListResponsePacket instance = new SoundListResponsePacket(requestId, soundFiles);
        return instance;
    }

    public static void handle(SoundListResponsePacket packet, Supplier<NetworkEvent.Context> context) {  
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleSoundListResponsePacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
