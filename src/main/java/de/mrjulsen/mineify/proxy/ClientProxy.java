package de.mrjulsen.mineify.proxy;

import de.mrjulsen.mineify.keys.ModKeys;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements IProxy {

    @Override
    public void setup(FMLCommonSetupEvent event) {  
        ModKeys.init();
    }
}
