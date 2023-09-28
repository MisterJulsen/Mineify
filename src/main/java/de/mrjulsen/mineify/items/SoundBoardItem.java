package de.mrjulsen.mineify.items;

import de.mrjulsen.mineify.client.ClientWrapper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoundBoardItem extends Item {

    public SoundBoardItem() {
        super(new Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide) {
            ClientWrapper.showSoundBoardScreen();
        }        
        return super.use(pLevel, pPlayer, pUsedHand);
    }
    
}
