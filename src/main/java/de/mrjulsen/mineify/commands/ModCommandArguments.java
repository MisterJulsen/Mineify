package de.mrjulsen.mineify.commands;

import de.mrjulsen.mineify.ModMain;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCommandArguments {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENTS = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ModMain.MOD_ID);

    
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> SOUNDS = COMMAND_ARGUMENTS.register("sounds", () -> ArgumentTypeInfos.registerByClass(SoundsArgument.class, new SoundArgumentInfo()));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> SOUND_VISIBILITY = COMMAND_ARGUMENTS.register("sound_visibility", () -> ArgumentTypeInfos.registerByClass(SoundVisibilityArgument.class, SingletonArgumentInfo.contextFree(SoundVisibilityArgument::visibilityArg)));
    
    

    public static void register(IEventBus event) {
        COMMAND_ARGUMENTS.register(event);
    }
}
