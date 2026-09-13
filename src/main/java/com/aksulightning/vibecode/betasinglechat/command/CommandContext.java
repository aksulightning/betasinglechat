package com.aksulightning.vibecode.betasinglechat.command;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;

public final class CommandContext {
    private final Minecraft minecraft;

    public CommandContext(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public ClientPlayerEntity player() {
        return minecraft.player;
    }

    public World world() {
        return minecraft.world;
    }

    public void reply(String message) {
        minecraft.inGameHud.addChatMessage(message);
    }

    public void error(String message) {
        reply("\u00a7c" + message);
    }
}
