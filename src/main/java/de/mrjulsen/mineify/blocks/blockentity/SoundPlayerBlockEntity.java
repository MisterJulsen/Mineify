package de.mrjulsen.mineify.blocks.blockentity;

import java.util.UUID;

import javax.annotation.Nullable;

import de.mrjulsen.mineify.client.ETrigger;
import de.mrjulsen.mineify.sound.Playlist;
import de.mrjulsen.mineify.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SoundPlayerBlockEntity extends BlockEntity {

    // Properties
    private Playlist playlist = Playlist.DEFAULT;
    private UUID owner = null;
    private ETrigger trigger = ETrigger.NONE;
    private boolean powered = false;

    protected SoundPlayerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SoundPlayerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUND_PLAYER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        
        if (compound.contains("owner")) {
            try {
                this.owner = compound.getUUID("owner");
            } catch (Exception e) {
                this.owner = null;
            }
        }
        this.trigger = ETrigger.getTriggerByIndex(compound.getByte("trigger"));
        this.powered = compound.getBoolean("powered");
        this.playlist = Playlist.fromNbt(compound);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {   
        if (this.getOwnerUUID() != null) {
            tag.putUUID("owner", this.getOwnerUUID());
        } else {
            tag.putBoolean("owner", false);
        }
        tag.putByte("trigger", this.getTrigger().getIndex());
        tag.putBoolean("powered", this.isPowered());
        this.playlist.toNbt(tag);

        super.saveAdditional(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        this.load(pkt.getTag());
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 3, 512);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
       this.playlist.tick(level, pos);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoundPlayerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(level, pos, state);
        }
    }

    private void clientSync() {
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public UUID getOwnerUUID() {
        return this.owner;
    }

    public boolean isLocked() {
        return this.getOwnerUUID() != null;
    }

    public boolean canVisit(UUID uuid) {
        return !this.isLocked() || this.getOwnerUUID().equals(uuid);
    }

    public ETrigger getTrigger() {
        return this.trigger;
    }

    public void lock(UUID uuid) {
        this.owner = uuid;
        this.clientSync();
    }

    public void unlock() {
        this.owner = null;
        this.clientSync();
    }

    public void setTrigger(ETrigger trigger) {
        this.trigger = trigger;
        this.clientSync();
    }

    public void setPowered(boolean b) {
        this.powered = b;
        this.clientSync();        

        if (b && (this.getTrigger() == ETrigger.CONTINUOUS_REDSTONE || this.getTrigger() == ETrigger.REDSTONE_IMPULSE)) {
            this.playlist.play(this.getLevel(), this.getBlockPos());            
        } else if (!b && this.getTrigger() == ETrigger.CONTINUOUS_REDSTONE) {
            this.playlist.stop(this.getLevel());
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }
}
