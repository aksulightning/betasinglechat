package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.gameplay.GodModeState;
import com.aksulightning.vibecode.betasinglechat.gameplay.FlightState;
import com.aksulightning.vibecode.betasinglechat.gameplay.RunningState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements GodModeState, FlightState, RunningState {
    @Unique private boolean betasinglechat$godModeEnabled;
    @Unique private boolean betasinglechat$flying;
    @Unique private double betasinglechat$flightSpeed = 1.0;
    @Unique private boolean betasinglechat$runningEnabled;

    protected PlayerEntityMixin(World world) {
        super(world);
    }

    @Override
    public boolean betasinglechat$isRunningEnabled() {
        return betasinglechat$runningEnabled && world != null && !world.isRemote;
    }

    @Override
    public void betasinglechat$setRunningEnabled(boolean enabled) {
        betasinglechat$runningEnabled = enabled;
    }

    @Override
    public boolean betasinglechat$isSprinting() {
        return betasinglechat$isRunningEnabled()
                && (Object) this instanceof ClientPlayerEntity player
                && health > 0 && !dead && vehicle == null && !isSleeping()
                && !betasinglechat$isFlying() && player.input != null && !player.isSneaking()
                && player.input.movementForward >= 0.8F
                && !horizontalCollision && !isSubmergedInWater() && !isTouchingLava() && !isOnLadder();
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void betasinglechat$sprintJump(CallbackInfo ci) {
        if (betasinglechat$isSprinting()) {
            double radians = Math.toRadians(yaw);
            velocityX -= Math.sin(radians) * 0.2;
            velocityZ += Math.cos(radians) * 0.2;
        }
    }

    @Override
    public boolean betasinglechat$isFlying() {
        return betasinglechat$flying && world != null && !world.isRemote;
    }

    @Override
    public void betasinglechat$setFlying(boolean enabled) {
        betasinglechat$flying = enabled;
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        fallDistance = 0;
    }

    @Override
    public double betasinglechat$getFlightSpeed() {
        return betasinglechat$flightSpeed;
    }

    @Override
    public void betasinglechat$setFlightSpeed(double speed) {
        betasinglechat$flightSpeed = speed;
    }

    @Override
    public void betasinglechat$resetFallDistance() {
        fallDistance = 0;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void betasinglechat$fly(float sideways, float forward, CallbackInfo ci) {
        if (!betasinglechat$isFlying() || !((Object) this instanceof ClientPlayerEntity player)
                || health <= 0 || vehicle != null) {
            return;
        }
        double speed = 0.35 * betasinglechat$flightSpeed;
        double strafe = player.input.movementSideways;
        double advance = player.input.movementForward;
        double length = Math.max(1, Math.sqrt(strafe * strafe + advance * advance));
        double radians = Math.toRadians(yaw);
        double dx = (strafe * Math.cos(radians) - advance * Math.sin(radians)) / length * speed;
        double dz = (advance * Math.cos(radians) + strafe * Math.sin(radians)) / length * speed;
        double dy = ((player.input.jumping ? 1 : 0) - (player.input.sneaking ? 1 : 0)) * speed;
        dy = Math.max(-boundingBox.minY, Math.min(128 - boundingBox.maxY, dy));
        fallDistance = 0;
        onGround = false;
        move(dx, dy, dz);
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        fallDistance = 0;
        ci.cancel();
    }

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
