package ru.aiefu.passcard.compat;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import ru.aiefu.passcard.PacketsIDs;

public class OriginOrbItem extends Item {
    public OriginOrbItem(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            for (OriginLayer layer : OriginLayers.getLayers()) {
                if(layer.isEnabled()) {
                    component.setOrigin(layer, Origin.EMPTY);
                }
            }
            component.checkAutoChoosingLayers(user, false);
            component.sync();
            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeBoolean(false);
            ServerPlayNetworking.send((ServerPlayerEntity) user, PacketsIDs.PASSCARD_ORIGINS_SCREEN, data);
        }
        ItemStack stack = user.getStackInHand(hand);
        if(!user.isCreative()) {
            stack.decrement(1);
        }
        return TypedActionResult.consume(stack);
    }
}
