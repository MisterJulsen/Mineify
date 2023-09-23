package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class StopSoundWithPathPacket {
    public final String shortPath;

    public StopSoundWithPathPacket(String shortPath) {
        this.shortPath = shortPath;
    }

    public static void encode(StopSoundWithPathPacket packet, FriendlyByteBuf buffer) {
        if (packet.shortPath == null) {
            buffer.writeInt(0);
        } else {
            buffer.writeInt(packet.shortPath.getBytes(StandardCharsets.UTF_8).length);
            buffer.writeUtf(packet.shortPath);
        }
    }

    public static StopSoundWithPathPacket decode(FriendlyByteBuf buffer) {
        int l = buffer.readInt();
        String shortPath = null;
        if (l > 0) { 
            shortPath = buffer.readUtf(l);
        }

        StopSoundWithPathPacket instance = new StopSoundWithPathPacket(shortPath);
        return instance;
    }

    public static void handle(StopSoundWithPathPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleStopSoundWithPathPacket(packet, context));
        });
        
        context.get().setPacketHandled(true);      
    }
}
