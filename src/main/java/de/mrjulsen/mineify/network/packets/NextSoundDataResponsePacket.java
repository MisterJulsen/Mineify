package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class NextSoundDataResponsePacket implements IPacketBase<NextSoundDataResponsePacket> {
    public long requestId;
    public int index;
    public byte[] data;
    public boolean hasNext;

    public NextSoundDataResponsePacket() { }

    public NextSoundDataResponsePacket(long requestId, byte[] data, boolean hasNext, int index) {
        this.requestId = requestId;
        this.data = data;
        this.hasNext = hasNext;
        this.index = index;
    }

    @Override
    public void encode(NextSoundDataResponsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeInt(packet.index);
        buffer.writeBoolean(packet.hasNext);
        buffer.writeByteArray(packet.data);
    }

    @Override
    public NextSoundDataResponsePacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int index = buffer.readInt();
        boolean hasNext = buffer.readBoolean();
        byte[] data = buffer.readByteArray();

        NextSoundDataResponsePacket instance = new NextSoundDataResponsePacket(requestId, data, hasNext, index);
        return instance;
    }

    @Override
    public void handle(NextSoundDataResponsePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {  
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleNextSoundDataResponsePacket(packet, context));
        });
        context.get().setPacketHandled(true);
    }
}
