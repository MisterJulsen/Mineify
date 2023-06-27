package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class RefreshSoundListPacket {


    public static void encode(RefreshSoundListPacket packet, FriendlyByteBuf buffer) {
        
    }

    public static RefreshSoundListPacket decode(FriendlyByteBuf buffer) {
        RefreshSoundListPacket instance = new RefreshSoundListPacket();
        return instance;
    }

    public static void handle(RefreshSoundListPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleRefreshSoundListPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
