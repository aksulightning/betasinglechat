package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.gameplay.GodModeState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements GodModeState {
    @Unique private boolean betasinglechat$godModeEnabled;

    @Override
    public boolean betasinglechat$isGodModeEnabled() {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return betasinglechat$godModeEnabled && player.world != null && !player.world.isRemote;
    }

    @Override
    public void betasinglechat$setGodModeEnabled(boolean enabled) {
        betasinglechat$godModeEnabled = enabled;
    }

    @Inject(method = "damage(Lnet/minecraft/entity/Entity;I)Z", at = @At("HEAD"), cancellable = true)
    private void betasinglechat$preventDamage(Entity source, int amount,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (betasinglechat$isGodModeEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void betasinglechat$preventHealthLoss(int amount, CallbackInfo ci) {
        if (betasinglechat$isGodModeEnabled()) {
            ci.cancel();
        }
    }
}
