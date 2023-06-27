package de.mrjulsen.mineify.blocks.blockentity;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ModMain.MOD_ID);


    public static final RegistryObject<BlockEntityType<SoundPlayerBlockEntity>> SOUND_PLAYER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sound_player_block_entity", () -> BlockEntityType.Builder.of(SoundPlayerBlockEntity::new, ModBlocks.SOUND_PLAYER.get()).build(null));
    

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
