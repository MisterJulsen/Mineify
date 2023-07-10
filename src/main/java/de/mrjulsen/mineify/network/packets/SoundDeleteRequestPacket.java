package de.mrjulsen.mineify.network.packets;

import java.io.File;
import java.util.function.Supplier;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.ToastMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SoundDeleteRequestPacket {
    private final String filename;
    private final String fileOwner;
    private final ESoundVisibility visibility;

    public SoundDeleteRequestPacket(String filename, String fileOwner, ESoundVisibility visibility) {
        this.filename = filename;
        this.fileOwner = fileOwner;
        this.visibility = visibility;
    }

    public static void encode(SoundDeleteRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.filename);
        buffer.writeUtf(packet.fileOwner);
        buffer.writeEnum(packet.visibility);
    }

    public static SoundDeleteRequestPacket decode(FriendlyByteBuf buffer) {
        String filename = buffer.readUtf();
        String fileOwner = buffer.readUtf();
        ESoundVisibility visibility = buffer.readEnum(ESoundVisibility.class);

        SoundDeleteRequestPacket instance = new SoundDeleteRequestPacket(filename, fileOwner, visibility);
        return instance;
    }

    public static void handle(SoundDeleteRequestPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() -> {
            
            ModMain.LOGGER.debug("Delete sound '" + packet.filename + "' started.");
            new Thread(() -> {                
                if (packet.visibility != ESoundVisibility.SERVER) {
                    int tries = 0;
                    File f = new File(String.format("%s/%s/%s/%s.%s", 
                        Constants.CUSTOM_SOUNDS_SERVER_PATH,
                        packet.visibility.getName(),
                        packet.fileOwner,
                        packet.filename,
                        Constants.SOUND_FILE_EXTENSION
                    ));

                    while (f.exists() && tries < 10) {
                        try {
                            f.delete();
                        } catch (Exception e) {
                            tries++;
                            if (tries >= 10) {
                                NetworkManager.MOD_CHANNEL.sendTo(new ErrorMessagePacket(new ToastMessage("gui.mineify.soundselection.task_fail", "Unable to delete sound file.")), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                            }
                        }
                    }

                    NetworkManager.MOD_CHANNEL.sendTo(new RefreshSoundListPacket(), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                }
                
                ModMain.LOGGER.debug("Sound deleted.");
            }, "DeleteSound").start();

        });
        
        context.get().setPacketHandled(true);      
    }
}
