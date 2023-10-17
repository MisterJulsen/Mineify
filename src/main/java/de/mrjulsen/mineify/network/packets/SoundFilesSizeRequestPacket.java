package de.mrjulsen.mineify.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundFilesSizeRequestPacket implements IPacketBase<SoundFilesSizeRequestPacket> {
    private long requestID;
    private @NotNull ESoundVisibility[] visibilityWhitelist;
    private @NotNull String[] usersWhitelist;

    public SoundFilesSizeRequestPacket() { }

    public SoundFilesSizeRequestPacket(long requestId, @NotNull ESoundVisibility[] visibilityWhitelist, @NotNull String[] usersWhitelist) {
        this.requestID = requestId;
        this.visibilityWhitelist = visibilityWhitelist == null ? new ESoundVisibility[0] : visibilityWhitelist;
        this.usersWhitelist = usersWhitelist == null ? new String[0] : usersWhitelist;
    }

    @Override
    public void encode(SoundFilesSizeRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requestID);
        buffer.writeInt(packet.visibilityWhitelist.length);
        for (int i = 0; i < packet.visibilityWhitelist.length; i++) {
            buffer.writeInt(packet.visibilityWhitelist[i].getIndex());
        }
        buffer.writeInt(packet.usersWhitelist.length);
        for (int i = 0; i < packet.usersWhitelist.length; i++) {
            int l = packet.usersWhitelist[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(l);
            buffer.writeUtf(packet.usersWhitelist[i], l);
        }
    }

    @Override
    public SoundFilesSizeRequestPacket decode(FriendlyByteBuf buffer) {
        long requestId = buffer.readLong();
        int n = buffer.readInt();
        ESoundVisibility[] visibilityWhitelist = new ESoundVisibility[n];
        for (int i = 0; i < n; i++) {
            visibilityWhitelist[i] = ESoundVisibility.getVisibilityByIndex(buffer.readInt());
        }
        int o = buffer.readInt();
        String[] usersWhitelist = new String[o];
        for (int i = 0; i < o; i++) {
            int l = buffer.readInt();
            usersWhitelist[i] = buffer.readUtf(l);
        }

        SoundFilesSizeRequestPacket instance = new SoundFilesSizeRequestPacket(requestId, visibilityWhitelist, usersWhitelist);
        return instance;
    }

    @Override
    public void handle(SoundFilesSizeRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            new Thread(() -> {                
                ModMain.LOGGER.debug("Reading sound files size...");
                long count = Arrays.stream(SoundUtils.readSoundsFromDisk(null, packet.visibilityWhitelist, packet.usersWhitelist)).mapToLong(x -> x.getSize()).sum();
                NetworkManager.sendToClient(new SoundFilesCountResponsePacket(packet.requestID, count), context.get().getSender());
                
                ModMain.LOGGER.debug("Finished reading sound files size.");
            }, "SoundFileListReader").start();
        });
        
        context.get().setPacketHandled(true);      
    }



    
    
}

