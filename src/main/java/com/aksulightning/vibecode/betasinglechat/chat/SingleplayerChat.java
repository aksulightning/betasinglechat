package com.aksulightning.vibecode.betasinglechat.chat;

import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.input.Keyboard;

public final class SingleplayerChat {
    private static final CommandDispatcher COMMANDS = CommandDispatcher.createDefault();
    private static final ChatHistory HISTORY = new ChatHistory();

    private SingleplayerChat() {
    }

    public static ChatHistory history() {
        return HISTORY;
    }

    public static boolean isSingleplayer(Minecraft minecraft) {
        return minecraft.world != null && !minecraft.world.isRemote
                && minecraft.player != null && minecraft.player.world == minecraft.world;
    }

    // Called inside vanilla's keyboard event loop, after inventory/drop handling.
    public static void openForCurrentKey(Minecraft minecraft) {
        if (isSingleplayer(minecraft) && minecraft.currentScreen == null
                && minecraft.player.health > 0 && !minecraft.player.isSleeping()
                && Keyboard.getEventKeyState()
                && Keyboard.getEventKey() == minecraft.options.chatKey.code) {
            minecraft.setScreen(new ChatScreen());
        }
    }

    public static void submit(Minecraft minecraft, String input) {
        if (!isSingleplayer(minecraft)) {
            return;
        }
        String message = input.trim();
        if (message.isEmpty()) {
            return;
        }
        HISTORY.add(message);
        CommandContext context = new CommandContext(minecraft);
        if (message.startsWith("/")) {
            COMMANDS.execute(context, message.substring(1));
        } else {
            context.reply("<" + context.player().name + "> " + message);
        }
    }
}
