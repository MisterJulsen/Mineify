package de.mrjulsen.mineify.network.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SetCooldownPacket implements IPacketBase<SetCooldownPacket> {
    public ItemStack stack;
    public int ticks;

    public SetCooldownPacket() { }

    public SetCooldownPacket(ItemStack stack, int ticks) {
        this.stack = stack;
        this.ticks = ticks;
    }

    @Override
    public void encode(SetCooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeItemStack(packet.stack, true);
        buffer.writeInt(packet.ticks);
    }

    @Override
    public SetCooldownPacket decode(FriendlyByteBuf buffer) {
        ItemStack stack = buffer.readItem();
        int ticks = buffer.readInt();

        SetCooldownPacket instance = new SetCooldownPacket(stack, ticks);
        return instance;
    }

    @Override
    public void handle(SetCooldownPacket packet, Supplier<NetworkEvent.Context> context) {        
        context.get().enqueueWork(() ->
        {
            context.get().getSender().getCooldowns().addCooldown(packet.stack.getItem(), packet.ticks);
        });
        
        context.get().setPacketHandled(true);
    }
}
