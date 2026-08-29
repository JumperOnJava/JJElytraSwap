package io.github.jumperonjava.jjelytraswap.mixin;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
//? if >=26 {
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
//?} else {
/*import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


//? if >=26 {
@Mixin(LocalPlayer.class)
public class SwapCheckMixin {

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z", shift = At.Shift.AFTER))
    public void swapToElytra(CallbackInfo callbackInfo) {
        if (!JJElytraSwapInit.enabled)
            return;
        var target = ((LocalPlayer) (Object) this);
        if (!target.onGround() &&
                !target.isFallFlying()
                && !target.isInWater() && !target.hasEffect(MobEffects.LEVITATION)) {
            JJElytraSwapInit.tryWearElytra();
        }
    }
}
//?} else {
/*@Mixin(ClientPlayerEntity.class)
public class SwapCheckMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;checkGliding()Z", shift = At.Shift.AFTER))
    public void swapToElytra(CallbackInfo callbackInfo) {
        if (!JJElytraSwapInit.enabled)
            return;
        var target = ((ClientPlayerEntity) (Object) this);
        if (!target.isOnGround() &&
                !target.isGliding()
                && !target.isTouchingWater() && !target.hasStatusEffect(StatusEffects.LEVITATION)) {
            JJElytraSwapInit.tryWearElytra();
        }
    }
}
*///?}
