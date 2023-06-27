package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class StopSoundPacket {
    public long soundId;

    public StopSoundPacket(long soundId) {
        this.soundId = soundId;
    }

    public static void encode(StopSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.soundId);
    }

    public static StopSoundPacket decode(FriendlyByteBuf buffer) {
        long soundId = buffer.readLong();

        StopSoundPacket instance = new StopSoundPacket(soundId);
        return instance;
    }

    public static void handle(StopSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleStopSoundPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
