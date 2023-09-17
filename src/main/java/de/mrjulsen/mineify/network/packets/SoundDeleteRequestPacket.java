package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.SoundRequest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SoundDeleteRequestPacket {
    private final long requestId;
    private final String filename;
    private final String fileOwner;
    private final ESoundVisibility visibility;

    public SoundDeleteRequestPacket(long requestId, String filename, String fileOwner, ESoundVisibility visibility) {
        this.filename = filename;
        this.fileOwner = fileOwner;
        this.visibility = visibility;
        this.requestId = requestId;
    }

    public static void encode(SoundDeleteRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeUtf(packet.filename);
        buffer.writeUtf(packet.fileOwner);
        buffer.writeEnum(packet.visibility);
    }

    public static SoundDeleteRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        String filename = buffer.readUtf();
        String fileOwner = buffer.readUtf();
        ESoundVisibility visibility = buffer.readEnum(ESoundVisibility.class);

        SoundDeleteRequestPacket instance = new SoundDeleteRequestPacket(requestId, filename, fileOwner, visibility);
        return instance;
    }

    public static void handle(SoundDeleteRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            SoundRequest.deleteSoundOnServer(packet.filename, packet.fileOwner, packet.visibility, context.get().getSender(), () -> {
                NetworkManager.MOD_CHANNEL.sendTo(new DefaultServerResponsePacket(packet.requestId), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                //NetworkManager.MOD_CHANNEL.sendTo(new RefreshSoundListPacket(), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            });
        });
        
        context.get().setPacketHandled(true);      
    }
}
