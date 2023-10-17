package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoundFilesCountResponsePacket implements IPacketBase<SoundFilesCountResponsePacket> {
    public long requestId;
    public long count;

    public SoundFilesCountResponsePacket() { }

    public SoundFilesCountResponsePacket(long requestId, long count) {
        this.requestId = requestId;
        this.count = count;
    }

    @Override
    public void encode(SoundFilesCountResponsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeLong(packet.count);
    }

    @Override
    public SoundFilesCountResponsePacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        long count = buffer.readLong();

        SoundFilesCountResponsePacket instance = new SoundFilesCountResponsePacket(requestId, count);
        return instance;
    }

    @Override
    public void handle(SoundFilesCountResponsePacket packet, Supplier<NetworkEvent.Context> context) {  
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleSoundFilesCountResponsePacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
