package de.mrjulsen.mineify.events;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.commands.SoundCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SoundCommand(event.getDispatcher());
    }
}
