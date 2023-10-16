package de.mrjulsen.mineify;

import com.mojang.logging.LogUtils;

import de.mrjulsen.mineify.blocks.ModBlocks;
import de.mrjulsen.mineify.blocks.blockentity.ModBlockEntities;
import de.mrjulsen.mineify.commands.ModCommandArguments;
import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.items.ModItems;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.UploaderUsercache;
import de.mrjulsen.mineify.proxy.ClientProxy;
import de.mrjulsen.mineify.proxy.IProxy;
import de.mrjulsen.mineify.proxy.ServerProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final String MOD_ID = "mineify";
    public final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);

        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModCommandArguments.register(eventBus);
        NetworkManager.registerNetworkPackets();
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC, MOD_ID + "-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");       

        UploaderUsercache.loadOrCreate(Constants.DEFAULT_USERCACHE_PATH);
        UploaderUsercache.INSTANCE.recacheNamesAsync();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("Welcome to the MINEIFY mod by MRJULSEN.");
        PROXY.setup(event);
    }
}
