package de.mrjulsen.mineify.network;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.packets.DownloadSoundPacket;
import de.mrjulsen.mineify.network.packets.ErrorMessagePacket;
import de.mrjulsen.mineify.network.packets.NextSoundDataRequestPacket;
import de.mrjulsen.mineify.network.packets.NextSoundDataResponsePacket;
import de.mrjulsen.mineify.network.packets.PlaySoundPacket;
import de.mrjulsen.mineify.network.packets.RefreshSoundListPacket;
import de.mrjulsen.mineify.network.packets.SoundDeleteRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundListRequestPacket;
import de.mrjulsen.mineify.network.packets.SoundListResponsePacket;
import de.mrjulsen.mineify.network.packets.SoundPlayerBlockEntityPacket;
import de.mrjulsen.mineify.network.packets.StopSoundPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundCompletionPacket;
import de.mrjulsen.mineify.network.packets.UploadSoundPacket;
import net.minecraft.resources.ResourceLocation;
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
        register(RefreshSoundListPacket.class).encoder(RefreshSoundListPacket::encode).decoder(RefreshSoundListPacket::decode).consumer(RefreshSoundListPacket::handle).add();
        register(NextSoundDataRequestPacket.class).encoder(NextSoundDataRequestPacket::encode).decoder(NextSoundDataRequestPacket::decode).consumer(NextSoundDataRequestPacket::handle).add();
        register(NextSoundDataResponsePacket.class).encoder(NextSoundDataResponsePacket::encode).decoder(NextSoundDataResponsePacket::decode).consumer(NextSoundDataResponsePacket::handle).add();
        register(PlaySoundPacket.class).encoder(PlaySoundPacket::encode).decoder(PlaySoundPacket::decode).consumer(PlaySoundPacket::handle).add();
        
    }

    public static SimpleChannel getPlayChannel() {
        return MOD_CHANNEL;
    }

    private static <T> MessageBuilder<T> register(Class<T> clazz) {
        MessageBuilder<T> mb = MOD_CHANNEL.messageBuilder(clazz, currentId);
        currentId++;
        return mb;
    }
}
