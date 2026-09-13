package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Command;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import com.aksulightning.vibecode.betasinglechat.gameplay.GodModeState;

public final class GodCommand implements Command {
    @Override
    public String name() {
        return "god";
    }

    @Override
    public String usage() {
        return "/god";
    }

    @Override
    public void execute(CommandContext context, String[] arguments) throws CommandException {
        if (arguments.length != 0) {
            throw new CommandException("Usage: " + usage());
        }
        GodModeState state = (GodModeState) context.player();
        boolean enabled = !state.betasinglechat$isGodModeEnabled();
        state.betasinglechat$setGodModeEnabled(enabled);
        context.reply(enabled ? "God mode enabled." : "God mode disabled.");
    }
}
