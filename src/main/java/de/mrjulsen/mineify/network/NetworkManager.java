package de.mrjulsen.mineify.network;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.packets.DefaultServerResponsePacket;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
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
import net.minecraftforge.network.simple.SimpleChannel.MessageBuilder;

public class NetworkManager {
    public static final String PROTOCOL_VERSION = String.valueOf(1);
    private static int currentId = 0;

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModMain.MOD_ID, "mineify_channel")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();
    
    public static void registerNetworkPackets() {
        register(DownloadSoundPacket.class).encoder(DownloadSoundPacket::encode).decoder(DownloadSoundPacket::decode).consumer(DownloadSoundPacket::handle).add();
        register(UploadSoundPacket.class).encoder(UploadSoundPacket::encode).decoder(UploadSoundPacket::decode).consumer(UploadSoundPacket::handle).add();
        register(UploadSoundCompletionPacket.class).encoder(UploadSoundCompletionPacket::encode).decoder(UploadSoundCompletionPacket::decode).consumer(UploadSoundCompletionPacket::handle).add();
        register(SoundDeleteRequestPacket.class).encoder(SoundDeleteRequestPacket::encode).decoder(SoundDeleteRequestPacket::decode).consumer(SoundDeleteRequestPacket::handle).add();
        register(SoundListRequestPacket.class).encoder(SoundListRequestPacket::encode).decoder(SoundListRequestPacket::decode).consumer(SoundListRequestPacket::handle).add();
        register(SoundListResponsePacket.class).encoder(SoundListResponsePacket::encode).decoder(SoundListResponsePacket::decode).consumer(SoundListResponsePacket::handle).add();
        register(SoundPlayerBlockEntityPacket.class).encoder(SoundPlayerBlockEntityPacket::encode).decoder(SoundPlayerBlockEntityPacket::decode).consumer(SoundPlayerBlockEntityPacket::handle).add();
        register(StopSoundPacket.class).encoder(StopSoundPacket::encode).decoder(StopSoundPacket::decode).consumer(StopSoundPacket::handle).add();
        register(ErrorMessagePacket.class).encoder(ErrorMessagePacket::encode).decoder(ErrorMessagePacket::decode).consumer(ErrorMessagePacket::handle).add();
        register(NextSoundDataRequestPacket.class).encoder(NextSoundDataRequestPacket::encode).decoder(NextSoundDataRequestPacket::decode).consumer(NextSoundDataRequestPacket::handle).add();
        register(NextSoundDataResponsePacket.class).encoder(NextSoundDataResponsePacket::encode).decoder(NextSoundDataResponsePacket::decode).consumer(NextSoundDataResponsePacket::handle).add();
        register(PlaySoundPacket.class).encoder(PlaySoundPacket::encode).decoder(PlaySoundPacket::decode).consumer(PlaySoundPacket::handle).add();
        register(DefaultServerResponsePacket.class).encoder(DefaultServerResponsePacket::encode).decoder(DefaultServerResponsePacket::decode).consumer(DefaultServerResponsePacket::handle).add();
        register(SoundFilesCountRequestPacket.class).encoder(SoundFilesCountRequestPacket::encode).decoder(SoundFilesCountRequestPacket::decode).consumer(SoundFilesCountRequestPacket::handle).add();
        register(SoundFilesCountResponsePacket.class).encoder(SoundFilesCountResponsePacket::encode).decoder(SoundFilesCountResponsePacket::decode).consumer(SoundFilesCountResponsePacket::handle).add();
        register(SoundFilesSizeRequestPacket.class).encoder(SoundFilesSizeRequestPacket::encode).decoder(SoundFilesSizeRequestPacket::decode).consumer(SoundFilesSizeRequestPacket::handle).add();
        register(StopSoundWithPathPacket.class).encoder(StopSoundWithPathPacket::encode).decoder(StopSoundWithPathPacket::decode).consumer(StopSoundWithPathPacket::handle).add();
        register(SoundModificationPacket.class).encoder(SoundModificationPacket::encode).decoder(SoundModificationPacket::decode).consumer(SoundModificationPacket::handle).add();
        register(SoundModificationWithPathPacket.class).encoder(SoundModificationWithPathPacket::encode).decoder(SoundModificationWithPathPacket::decode).consumer(SoundModificationWithPathPacket::handle).add();
        register(PlaySoundRequestPacket.class).encoder(PlaySoundRequestPacket::encode).decoder(PlaySoundRequestPacket::decode).consumer(PlaySoundRequestPacket::handle).add();
        register(SetCooldownPacket.class).encoder(SetCooldownPacket::encode).decoder(SetCooldownPacket::decode).consumer(SetCooldownPacket::handle).add();
        
    }

    public static SimpleChannel getPlayChannel() {
        return MOD_CHANNEL;
    }

    private static <T> MessageBuilder<T> register(Class<T> clazz) {
        MessageBuilder<T> mb = MOD_CHANNEL.messageBuilder(clazz, currentId);
        currentId++;
        return mb;
    }

    public static void sendToClient(Object o, ServerPlayer player) {
        NetworkManager.MOD_CHANNEL.sendTo(o, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }
}
