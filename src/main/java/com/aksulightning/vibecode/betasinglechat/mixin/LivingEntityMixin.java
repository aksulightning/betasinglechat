package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.gameplay.RunningState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // The third call is normal ground/air travel, after the water and lava branches.
    @ModifyArg(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;moveNonSolid(FFF)V", ordinal = 2), index = 2)
    private float betasinglechat$sprintAcceleration(float acceleration) {
        if ((Object) this instanceof RunningState state && state.betasinglechat$isSprinting()) {
            return acceleration * 1.3F;
        }
        return acceleration;
    }
}
