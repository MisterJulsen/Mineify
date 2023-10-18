package de.mrjulsen.mineify.events;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.mrjulsen.mineify.ModMain;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabs {

    protected static Multimap<CreativeModeTab, ItemLike> itemsPerCreativeTab = HashMultimap.create();

    @SubscribeEvent
    public static void buildContents(CreativeModeTabEvent.BuildContents event) {
        CreativeModeTab tab = event.getTab();
        if (itemsPerCreativeTab.containsKey(tab))
            for (ItemLike il : itemsPerCreativeTab.get(tab)) {
                Item item = il.asItem();
                event.accept(item);
            }
    }

    public static void setCreativeTab(ItemLike itemlike, CreativeModeTab group) {
		ModCreativeTabs.itemsPerCreativeTab.put(group, itemlike);
	}

}
