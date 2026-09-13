package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.chat.SingleplayerChat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;isWorldRemote()Z"))
    private void betasinglechat$openChat(CallbackInfo ci) {
        SingleplayerChat.openForCurrentKey((Minecraft) (Object) this);
    }
}
