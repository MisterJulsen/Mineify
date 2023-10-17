package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DefaultServerResponsePacket implements IPacketBase<DefaultServerResponsePacket> {
    public long requestId;
    
    public DefaultServerResponsePacket() { }

    public DefaultServerResponsePacket(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void encode(DefaultServerResponsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
    }

    @Override
    public DefaultServerResponsePacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();

        DefaultServerResponsePacket instance = new DefaultServerResponsePacket(requestId);
        return instance;
    }

    @Override
    public void handle(DefaultServerResponsePacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWrapper.handleRunnablePacket(packet, context);
            });
        });
        
        context.get().setPacketHandled(true);      
    }
}
