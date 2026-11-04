package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandDispatcher;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import com.aksulightning.vibecode.betasinglechat.command.SimpleCommand;
import com.aksulightning.vibecode.betasinglechat.gameplay.FlightState;
import com.aksulightning.vibecode.betasinglechat.gameplay.RunningState;
import net.minecraft.item.ItemStack;

public final class PlayerCommands {
    private PlayerCommands() {
    }

    public static void register(CommandDispatcher dispatcher) {
        dispatcher.register(new SimpleCommand("repair", "/repair", "Repair the selected held tool or armor item.",
                (context, args) -> {
                    Arguments.count(args, 0, 0, "/repair");
                    ItemStack held = context.player().inventory.getSelectedItem();
                    if (held == null || !held.isDamageable()) {
                        throw new CommandException("Hold an item with durability to repair it.");
                    }
                    if (!held.isDamaged()) {
                        context.reply("Your held item is already repaired.");
                        return;
                    }
                    held.setDamage(0);
                    context.player().inventory.markDirty();
                    context.reply("Held item repaired.");
                }));
        dispatcher.register(new ClearCommand());
        dispatcher.register(new SimpleCommand("fly", "/fly", "Toggle flight; jump rises and sneak descends.",
                PlayerCommands::fly));
        dispatcher.register(new SimpleCommand("run", "/run",
                "Toggle running: move forward for a 30% speed boost and sprint jumps.", (context, args) -> {
                    Arguments.count(args, 0, 0, "/run");
                    RunningState state = (RunningState) context.player();
                    boolean enabled = !state.betasinglechat$isRunningEnabled();
                    state.betasinglechat$setRunningEnabled(enabled);
                    context.reply(enabled ? "Running enabled. Move forward to sprint."
                            : "Running disabled.");
                }));
        dispatcher.register(new SimpleCommand("speed", "/speed <value>",
                "Set flight speed multiplier from 0.1 to 4; default is 1.", (context, args) -> {
                    Arguments.count(args, 1, 1, "/speed <value>");
                    double speed = Arguments.number(args[0], "Flight speed");
                    if (speed < 0.1 || speed > 4) {
                        throw new CommandException("Flight speed must be from 0.1 to 4.");
                    }
                    ((FlightState) context.player()).betasinglechat$setFlightSpeed(speed);
                    context.reply("Flight speed set to " + speed + "x.");
                }));
    }

    private static void fly(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 0, 0, "/fly");
        FlightState state = (FlightState) context.player();
        if (!state.betasinglechat$isFlying() && context.player().vehicle != null) {
            throw new CommandException("Dismount before enabling flight.");
        }
        boolean enabled = !state.betasinglechat$isFlying();
        state.betasinglechat$setFlying(enabled);
        context.reply(enabled ? "Flight enabled. Jump to rise; sneak to descend."
                : "Flight disabled. Normal gravity restored.");
    }
}
