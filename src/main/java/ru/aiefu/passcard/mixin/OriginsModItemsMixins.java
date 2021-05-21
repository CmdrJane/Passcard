package ru.aiefu.passcard.mixin;

import io.github.apace100.origins.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.passcard.compat.OriginOrbItem;

@Pseudo
@Mixin(ModItems.class)
public class OriginsModItemsMixins {
    @Mutable
    @Shadow @Final public static Item ORB_OF_ORIGIN;

    @Inject(method = "register", remap = false, at =@At("HEAD"))
    private static void rerouteSelPacket(CallbackInfo ci){
        ORB_OF_ORIGIN = new OriginOrbItem(new Item.Settings().maxCount(1).group(ItemGroup.MISC).rarity(Rarity.RARE));
    }
}
