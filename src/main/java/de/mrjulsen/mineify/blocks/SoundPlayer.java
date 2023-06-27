package de.mrjulsen.mineify.blocks;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.blocks.blockentity.ModBlockEntities;
import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.ClientWrapper;
import de.mrjulsen.mineify.client.ETrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class SoundPlayer extends BaseEntityBlock {
    
    public SoundPlayer() {
        super(BlockBehaviour.Properties.of(Material.WOOD)
            .strength(5f)
            .sound(SoundType.WOOD)
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof SoundPlayerBlockEntity blockEntity) { 
            blockEntity.stopPlayingSound();
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            if (pLevel.getBlockEntity(pPos) instanceof SoundPlayerBlockEntity blockEntity) {
                if (pLevel.hasNeighborSignal(pPos)) {
                    if (!blockEntity.isPowered()) {
                        blockEntity.setPowered(true);
                    }
                } else {
                    if (blockEntity.isPowered()) {
                        blockEntity.setPowered(false);
                    }
                }                    
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if (level.getBlockEntity(pos) instanceof SoundPlayerBlockEntity blockEntity) { 
            
            if (blockEntity.getTrigger() == ETrigger.NONE && player.isShiftKeyDown()) {
                if (blockEntity.isPlaying()) {
                    blockEntity.stop();
                } else {
                    blockEntity.start();
                }
                return InteractionResult.SUCCESS;
            }


            if (blockEntity.canVisit(player.getUUID())) {
                if (level.isClientSide) {
                    ClientWrapper.showSoundSelectionScreen(blockEntity);
                } else {

                }
            } else {
                if (level.isClientSide) {
                    
                } else {
                    player.displayClientMessage(new TranslatableComponent("block.mineify.sound_player.locked"), true);
                }
                
            }                
        }
       
        return InteractionResult.SUCCESS; 
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SoundPlayerBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.SOUND_PLAYER_BLOCK_ENTITY.get(), SoundPlayerBlockEntity::tick);
    }  
}
