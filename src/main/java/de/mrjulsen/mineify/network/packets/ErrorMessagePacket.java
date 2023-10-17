package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.ToastMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ErrorMessagePacket implements IPacketBase<ErrorMessagePacket> {
    public ToastMessage message;

    public ErrorMessagePacket() { }

    public ErrorMessagePacket(ToastMessage message) {
        this.message = message;
    }

    @Override
    public void encode(ErrorMessagePacket packet, FriendlyByteBuf buffer) {
        packet.message.serialize(buffer);
    }

    @Override
    public ErrorMessagePacket decode(FriendlyByteBuf buffer) {
        ToastMessage message = ToastMessage.deserialize(buffer);
        ErrorMessagePacket instance = new ErrorMessagePacket(message);
        return instance;
    }

    @Override
    public void handle(ErrorMessagePacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleErrorMessagePacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
