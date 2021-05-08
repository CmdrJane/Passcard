package ru.aiefu.passcard.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.passcard.IPlayerPass;

@Mixin(PlayerEntity.class)
public class PlayerMixins {
    @Inject(method = "dropSelectedItem", at =@At("HEAD"), cancellable = true)
    private void preventDropAuthCheck(boolean dropEntireStack, CallbackInfoReturnable<Boolean> cir){
        if(!((IPlayerPass)this).getAuthState()){
            cir.setReturnValue(false);
        }
    }
}
