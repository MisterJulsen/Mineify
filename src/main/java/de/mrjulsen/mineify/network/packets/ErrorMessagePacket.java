package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.ToastMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ErrorMessagePacket {
    public final ToastMessage message;

    public ErrorMessagePacket(ToastMessage message) {
        this.message = message;
    }

    public static void encode(ErrorMessagePacket packet, FriendlyByteBuf buffer) {
        packet.message.serialize(buffer);
    }

    public static ErrorMessagePacket decode(FriendlyByteBuf buffer) {
        ToastMessage message = ToastMessage.deserialize(buffer);
        ErrorMessagePacket instance = new ErrorMessagePacket(message);
        return instance;
    }

    public static void handle(ErrorMessagePacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleErrorMessagePacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
