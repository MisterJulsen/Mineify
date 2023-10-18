package de.mrjulsen.mineify.network;

import java.util.Optional;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.packets.DefaultServerResponsePacket;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
import de.mrjulsen.mineify.network.packets.IPacketBase;
import de.mrjulsen.mineify.network.packets.NextSoundDataRequestPacket;
import de.mrjulsen.mineify.network.packets.NextSoundDataResponsePacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.PlaySoundRequestPacket;
import de.mrjulsen.mineify.network.packets.SetCooldownPacket;
import de.mrjulsen.mineify.network.packets.SoundDeleteRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundFilesCountRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundFilesCountResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundFilesSizeRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundListResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundModificationPacket;
import de.mrjulsen.mineify.network.packets.SoundModificationWithPathPacket;
import de.mrjulsen.mineify.network.packets.SoundPlayerBlockEntityPacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.network.packets.StopSoundWithPathPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundCompletionPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    public static final String PROTOCOL_VERSION = String.valueOf(1);
    private static int currentId = 0;

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModMain.MOD_ID, "network")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();
    
    public static void registerNetworkPackets() {
        registerPacket(UploadSoundPacket.class, new UploadSoundPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(UploadSoundCompletionPacket.class, new UploadSoundCompletionPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SoundDeleteRequestPacket.class, new SoundDeleteRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SoundListRequestPacket.class, new SoundListRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SoundPlayerBlockEntityPacket.class, new SoundPlayerBlockEntityPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(NextSoundDataRequestPacket.class, new NextSoundDataRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SoundFilesCountRequestPacket.class, new SoundFilesCountRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SoundFilesSizeRequestPacket.class, new SoundFilesSizeRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(PlaySoundRequestPacket.class, new PlaySoundRequestPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SetCooldownPacket.class, new SetCooldownPacket(), NetworkDirection.PLAY_TO_SERVER);

        
        registerPacket(DownloadSoundPacket.class, new DownloadSoundPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SoundListResponsePacket.class, new SoundListResponsePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(StopSoundPacket.class, new StopSoundPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(ErrorMessagePacket.class, new ErrorMessagePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(NextSoundDataResponsePacket.class, new NextSoundDataResponsePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(PlaySoundPacket.class, new PlaySoundPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(DefaultServerResponsePacket.class, new DefaultServerResponsePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SoundFilesCountResponsePacket.class, new SoundFilesCountResponsePacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(StopSoundWithPathPacket.class, new StopSoundWithPathPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SoundModificationPacket.class, new SoundModificationPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SoundModificationWithPathPacket.class, new SoundModificationWithPathPacket(), NetworkDirection.PLAY_TO_CLIENT);


    }

    public static SimpleChannel getPlayChannel() {
        return MOD_CHANNEL;
    }

    private static <T> void registerPacket(Class<T> clazz, IPacketBase<T> packet, @Nullable  NetworkDirection direction) {
        MOD_CHANNEL.registerMessage(currentId++, clazz, packet::encode, packet::decode, packet::handle, Optional.of(direction));
    }

    public static void sendToClient(Object o, ServerPlayer player) {
        NetworkManager.MOD_CHANNEL.sendTo(o, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
