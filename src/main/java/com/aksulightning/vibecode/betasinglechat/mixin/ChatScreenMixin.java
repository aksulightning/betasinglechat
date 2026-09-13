package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.chat.SingleplayerChat;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow protected String text;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void betasinglechat$submit(char character, int key, CallbackInfo ci) {
        if ((key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER)
                && SingleplayerChat.isSingleplayer(minecraft)) {
            SingleplayerChat.submit(minecraft, text);
            minecraft.setScreen(null);
            ci.cancel();
        }
    }
}
