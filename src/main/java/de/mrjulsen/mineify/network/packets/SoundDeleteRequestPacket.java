package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.ServerWrapper;
import de.mrjulsen.mineify.sound.ESoundCategory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundDeleteRequestPacket implements IPacketBase<SoundDeleteRequestPacket> {
    private long requestId;
    private String filename;
    private String fileOwner;
    private ESoundVisibility visibility;
    private ESoundCategory category;

    public SoundDeleteRequestPacket() { }

    public SoundDeleteRequestPacket(long requestId, String filename, String fileOwner, ESoundVisibility visibility, ESoundCategory category) {
        this.filename = filename;
        this.fileOwner = fileOwner;
        this.visibility = visibility;
        this.requestId = requestId;
        this.category = category;
    }

    @Override
    public void encode(SoundDeleteRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestId);
        buffer.writeUtf(packet.filename);
        buffer.writeUtf(packet.fileOwner);
        buffer.writeEnum(packet.visibility);
        buffer.writeEnum(packet.category);
    }

    @Override
    public SoundDeleteRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        String filename = buffer.readUtf();
        String fileOwner = buffer.readUtf();
        ESoundVisibility visibility = buffer.readEnum(ESoundVisibility.class);
        ESoundCategory category = buffer.readEnum(ESoundCategory.class);

        SoundDeleteRequestPacket instance = new SoundDeleteRequestPacket(requestId, filename, fileOwner, visibility, category);
        return instance;
    }

    @Override
    public void handle(SoundDeleteRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            ServerWrapper.deleteSound(packet.filename, packet.fileOwner, packet.visibility, packet.category, context.get().getSender(), () -> {
                ServerApi.sendResponse(context.get().getSender(), packet.requestId);
            });
        });
        
        context.get().setPacketHandled(true);      
    }
}
