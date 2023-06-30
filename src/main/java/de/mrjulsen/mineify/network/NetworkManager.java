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

public class NetworkManager {
    public static final String PROTOCOL_VERSION = String.valueOf(1);

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModMain.MOD_ID, "mineify_channel")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();
    
    public static void registerNetworkPackets() {
        MOD_CHANNEL.messageBuilder(DownloadSoundPacket.class, 0).encoder(DownloadSoundPacket::encode).decoder(DownloadSoundPacket::decode).consumer(DownloadSoundPacket::handle).add();
        MOD_CHANNEL.messageBuilder(UploadSoundPacket.class, 1).encoder(UploadSoundPacket::encode).decoder(UploadSoundPacket::decode).consumer(UploadSoundPacket::handle).add();
        MOD_CHANNEL.messageBuilder(UploadSoundCompletionPacket.class, 2).encoder(UploadSoundCompletionPacket::encode).decoder(UploadSoundCompletionPacket::decode).consumer(UploadSoundCompletionPacket::handle).add();
        MOD_CHANNEL.messageBuilder(SoundDeleteRequestPacket.class, 3).encoder(SoundDeleteRequestPacket::encode).decoder(SoundDeleteRequestPacket::decode).consumer(SoundDeleteRequestPacket::handle).add();
        MOD_CHANNEL.messageBuilder(SoundListRequestPacket.class, 4).encoder(SoundListRequestPacket::encode).decoder(SoundListRequestPacket::decode).consumer(SoundListRequestPacket::handle).add();
        MOD_CHANNEL.messageBuilder(SoundListResponsePacket.class, 5).encoder(SoundListResponsePacket::encode).decoder(SoundListResponsePacket::decode).consumer(SoundListResponsePacket::handle).add();
        MOD_CHANNEL.messageBuilder(SoundPlayerBlockEntityPacket.class, 6).encoder(SoundPlayerBlockEntityPacket::encode).decoder(SoundPlayerBlockEntityPacket::decode).consumer(SoundPlayerBlockEntityPacket::handle).add();
        MOD_CHANNEL.messageBuilder(StopSoundPacket.class, 7).encoder(StopSoundPacket::encode).decoder(StopSoundPacket::decode).consumer(StopSoundPacket::handle).add();
        MOD_CHANNEL.messageBuilder(ErrorMessagePacket.class, 8).encoder(ErrorMessagePacket::encode).decoder(ErrorMessagePacket::decode).consumer(ErrorMessagePacket::handle).add();
        MOD_CHANNEL.messageBuilder(RefreshSoundListPacket.class, 9).encoder(RefreshSoundListPacket::encode).decoder(RefreshSoundListPacket::decode).consumer(RefreshSoundListPacket::handle).add();
        MOD_CHANNEL.messageBuilder(NextSoundDataRequestPacket.class, 10).encoder(NextSoundDataRequestPacket::encode).decoder(NextSoundDataRequestPacket::decode).consumer(NextSoundDataRequestPacket::handle).add();
        MOD_CHANNEL.messageBuilder(NextSoundDataResponsePacket.class, 11).encoder(NextSoundDataResponsePacket::encode).decoder(NextSoundDataResponsePacket::decode).consumer(NextSoundDataResponsePacket::handle).add();
        MOD_CHANNEL.messageBuilder(PlaySoundPacket.class, 12).encoder(PlaySoundPacket::encode).decoder(PlaySoundPacket::decode).consumer(PlaySoundPacket::handle).add();
        
    }

    public static SimpleChannel getPlayChannel() {
        return MOD_CHANNEL;
    }
}
