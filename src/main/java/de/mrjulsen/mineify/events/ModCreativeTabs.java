package de.mrjulsen.mineify.events;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class ModCreativeTabs {

    protected static Multimap<ResourceKey<CreativeModeTab>, ItemLike> itemsPerCreativeTab = HashMultimap.create();

    public static void register(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();
        if (itemsPerCreativeTab.containsKey(tab))
            for (ItemLike il : itemsPerCreativeTab.get(tab)) {
                Item item = il.asItem();
                event.accept(item);
            }
    }

    public static void setCreativeTab(ItemLike itemlike, ResourceKey<CreativeModeTab> group) {
		ModCreativeTabs.itemsPerCreativeTab.put(group, itemlike);
	}
}
