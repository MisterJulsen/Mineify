package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundListRequestPacket implements IPacketBase<SoundListRequestPacket> {
    private long requestID;
    private @NotNull ESoundCategory[] categoryWhitelist;
    private @NotNull ESoundVisibility[] visibilityWhitelist;
    private @NotNull String[] usersWhitelist;

    public SoundListRequestPacket() { }

    public SoundListRequestPacket(long requestId, @NotNull ESoundVisibility[] visibilityWhitelist, @NotNull String[] usersWhitelist, @NotNull ESoundCategory[] categoryWhitelist) {
        this.requestID = requestId;
        this.visibilityWhitelist = visibilityWhitelist == null ? new ESoundVisibility[0] : visibilityWhitelist;
        this.usersWhitelist = usersWhitelist == null ? new String[0] : usersWhitelist;
        this.categoryWhitelist = categoryWhitelist == null ? new ESoundCategory[0] : categoryWhitelist;
    }

    @Override
    public void encode(SoundListRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
        buffer.writeInt(packet.visibilityWhitelist.length);
        for (int i = 0; i < packet.visibilityWhitelist.length; i++) {
            buffer.writeInt(packet.visibilityWhitelist[i].getIndex());
        }
        buffer.writeInt(packet.categoryWhitelist.length);
        for (int i = 0; i < packet.categoryWhitelist.length; i++) {
            buffer.writeInt(packet.categoryWhitelist[i].getIndex());
        }
        buffer.writeInt(packet.usersWhitelist.length);
        for (int i = 0; i < packet.usersWhitelist.length; i++) {
            int l = packet.usersWhitelist[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(l);
            buffer.writeUtf(packet.usersWhitelist[i], l);
        }
    }

    @Override
    public SoundListRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int n = buffer.readInt();
        ESoundVisibility[] visibilityWhitelist = new ESoundVisibility[n];
        for (int i = 0; i < n; i++) {
            visibilityWhitelist[i] = ESoundVisibility.getVisibilityByIndex(buffer.readInt());
        }
        int p = buffer.readInt();
        ESoundCategory[] categoryWhitelist = new ESoundCategory[p];
        for (int i = 0; i < p; i++) {
            categoryWhitelist[i] = ESoundCategory.getCategoryByIndex(buffer.readInt());
        }
        int o = buffer.readInt();
        String[] usersWhitelist = new String[o];
        for (int i = 0; i < o; i++) {
            int l = buffer.readInt();
            usersWhitelist[i] = buffer.readUtf(l);
        }

        SoundListRequestPacket instance = new SoundListRequestPacket(requestId, visibilityWhitelist, usersWhitelist, categoryWhitelist);
        return instance;
    }

    @Override
    public void handle(SoundListRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {
                
                ModMain.LOGGER.debug("Reading sound files...");
                SoundFile[] soundFiles = SoundUtils.readSoundsFromDisk(packet.categoryWhitelist, packet.visibilityWhitelist, packet.usersWhitelist);
                NetworkManager.sendToClient(new SoundListResponsePacket(packet.requestID, soundFiles), context.get().getSender());
                
                ModMain.LOGGER.debug("Sound file list created.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

