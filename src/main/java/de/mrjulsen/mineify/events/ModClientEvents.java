package de.mrjulsen.mineify.events;

import java.util.Set;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.items.ModItems;
import de.mrjulsen.mineify.keys.ModKeys;
import de.mrjulsen.mineify.sound.ModifiedSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {
    
    @SubscribeEvent
    public static void clientTick(ClientTickEvent event) {
        if (ModKeys.soundBoardKeyMapping.isDown() && Minecraft.getInstance().player.getInventory().hasAnyOf(Set.of(ModItems.SOUND_BOARD.get())) && !Minecraft.getInstance().player.getCooldowns().isOnCooldown(ModItems.SOUND_BOARD.get())) {
            ClientWrapper.showSoundBoardScreen();
        }
    }

    @SubscribeEvent
    public static void onReceiveSoundPlayEvent(PlayStreamingSourceEvent event) {
        if (event.getSound() instanceof ModifiedSoundInstance instance) {
            instance.setSoundChannel(event.getChannel());
        }
    }

}
