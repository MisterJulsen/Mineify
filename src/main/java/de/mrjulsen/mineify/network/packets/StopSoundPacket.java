package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class StopSoundPacket implements IPacketBase<StopSoundPacket> {
    public long soundId;

    public StopSoundPacket() { }

    public StopSoundPacket(long soundId) {
        this.soundId = soundId;
    }

    @Override
    public void encode(StopSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.soundId);
    }

    @Override
    public StopSoundPacket decode(FriendlyByteBuf buffer) {
        long soundId = buffer.readLong();

        StopSoundPacket instance = new StopSoundPacket(soundId);
        return instance;
    }

    @Override
    public void handle(StopSoundPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleStopSoundPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
