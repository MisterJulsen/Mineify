package de.mrjulsen.mineify.blocks;

import java.util.function.Supplier;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.events.ModCreativeTabs;
import de.mrjulsen.mineify.items.ModItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModMain.MOD_ID);
    
    
    public static final RegistryObject<Block> SOUND_PLAYER = registerBlock("sound_player", () -> new SoundPlayer(), CreativeModeTabs.REDSTONE_BLOCKS);
    

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block, ResourceKey<CreativeModeTab> tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, ResourceKey<CreativeModeTab> tab) {
        return ModItems.ITEMS.register(name, () -> {
            BlockItem item = new BlockItem(block.get(), new Item.Properties());
            ModCreativeTabs.setCreativeTab(item, tab);
            return item;
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
