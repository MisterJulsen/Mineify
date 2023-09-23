package de.mrjulsen.mineify.events;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.commands.SoundCommand;
import de.mrjulsen.mineify.sound.ModifiedSoundInstance;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SoundCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onReceiveSoundPlayEvent(PlayStreamingSourceEvent event) {
        if (event.getSound() instanceof ModifiedSoundInstance instance) {
            instance.setSoundChannel(event.getChannel());
        }
    }

}
