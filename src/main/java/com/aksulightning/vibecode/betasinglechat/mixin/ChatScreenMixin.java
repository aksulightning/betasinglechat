package com.aksulightning.vibecode.betasinglechat.mixin;

import com.aksulightning.vibecode.betasinglechat.chat.ChatHistory;
import com.aksulightning.vibecode.betasinglechat.chat.SingleplayerChat;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow protected String text;

    @Unique private int betasinglechat$historyIndex = -1;
    @Unique private String betasinglechat$draft = "";

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void betasinglechat$handleInput(char character, int key, CallbackInfo ci) {
        if (!SingleplayerChat.isSingleplayer(minecraft)) {
            return;
        }
        if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
            betasinglechat$navigateHistory(key == Keyboard.KEY_UP);
            ci.cancel();
        } else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            SingleplayerChat.submit(minecraft, text);
            minecraft.setScreen(null);
            ci.cancel();
        }
    }

    @Unique
    private void betasinglechat$navigateHistory(boolean older) {
        ChatHistory history = SingleplayerChat.history();
        if (betasinglechat$historyIndex < 0) {
            betasinglechat$historyIndex = history.size();
        }
        if (older && betasinglechat$historyIndex > 0) {
            if (betasinglechat$historyIndex == history.size()) {
                betasinglechat$draft = text;
            }
            text = history.get(--betasinglechat$historyIndex);
        } else if (!older && betasinglechat$historyIndex < history.size()) {
            betasinglechat$historyIndex++;
            text = betasinglechat$historyIndex == history.size()
                    ? betasinglechat$draft : history.get(betasinglechat$historyIndex);
        }
    }
}
