package de.mrjulsen.mineify.events;

import java.util.Set;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.items.ModItems;
import de.mrjulsen.mineify.sound.ModifiedSoundInstance;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {
    
    @SubscribeEvent
    public static void clientTick(ClientTickEvent event) {
        if (ModClientEvents.soundBoardKeyMapping != null && ModClientEvents.soundBoardKeyMapping.isDown() && Minecraft.getInstance().player.getInventory().hasAnyOf(Set.of(ModItems.SOUND_BOARD.get())) && !Minecraft.getInstance().player.getCooldowns().isOnCooldown(ModItems.SOUND_BOARD.get())) {
            ClientWrapper.showSoundBoardScreen();
        }
    }

    @SubscribeEvent
    public static void onReceiveSoundPlayEvent(PlayStreamingSourceEvent event) {
        if (event.getSound() instanceof ModifiedSoundInstance instance) {
            instance.setSoundChannel(event.getChannel());
        }
    }

    private static KeyMapping registerKey(String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + ModMain.MOD_ID + "." + name, keycode, "key." + ModMain.MOD_ID + ".category." + category);
        return key;
    }

    public static KeyMapping soundBoardKeyMapping;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        soundBoardKeyMapping = registerKey("sound_board", "mineify", -1);
        event.register(soundBoardKeyMapping);
    }

}
