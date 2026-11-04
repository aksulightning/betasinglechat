package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandDispatcher;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import com.aksulightning.vibecode.betasinglechat.command.SimpleCommand;
import java.util.Locale;
import net.minecraft.world.WorldProperties;

public final class WorldCommands {
    private WorldCommands() {
    }

    public static void register(CommandDispatcher dispatcher) {
        dispatcher.register(new SimpleCommand("heal", "/heal", "Restore all ten hearts.", (context, args) -> {
            Arguments.count(args, 0, 0, "/heal");
            context.player().heal(20);
            context.reply("Health restored.");
        }));
        dispatcher.register(new SimpleCommand("time", "/time <day|night|ticks>",
                "Set time within the current day; ticks must be 0-23999.", WorldCommands::time));
        dispatcher.register(new SimpleCommand("weather", "/weather <clear|rain|thunder>",
                "Set Overworld weather for five minutes.", WorldCommands::weather));
        dispatcher.register(new SimpleCommand("pos", "/pos", "Show feet coordinates and dimension.", (context, args) -> {
            Arguments.count(args, 0, 0, "/pos");
            context.reply(String.format(Locale.ROOT, "Position: %.2f %.2f %.2f (dimension %d)",
                    context.player().x, context.player().boundingBox.minY, context.player().z,
                    context.world().dimension.id));
        }));
        dispatcher.register(new SimpleCommand("seed", "/seed", "Show the world seed.", (context, args) -> {
            Arguments.count(args, 0, 0, "/seed");
            context.reply("Seed: " + context.world().getSeed());
        }));
    }

    private static void time(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 1, 1, "/time <day|night|ticks>");
        int ticks = switch (args[0].toLowerCase(Locale.ROOT)) {
            case "day" -> 1000;
            case "night" -> 13000;
            default -> Arguments.integer(args[0], 0, 23999, "Time");
        };
        long current = context.world().getTime();
        long target;
        try {
            long startOfDay = Math.subtractExact(current, Math.floorMod(current, 24000L));
            target = Math.addExact(startOfDay, ticks);
        } catch (ArithmeticException exception) {
            throw new CommandException("World time is outside the supported range.");
        }
        // Keep scheduled block updates at the same relative delay when changing the clock.
        context.world().synchronizeTimeAndUpdates(target);
        context.world().updateSkyBrightness();
        context.reply("Time set to " + ticks + ".");
    }

    private static void weather(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 1, 1, "/weather <clear|rain|thunder>");
        String weather = args[0].toLowerCase(Locale.ROOT);
        if (!weather.equals("clear") && !weather.equals("rain") && !weather.equals("thunder")) {
            throw new CommandException("Usage: /weather <clear|rain|thunder>");
        }
        if (context.world().dimension.id != 0) {
            throw new CommandException("Weather can only be changed in the Overworld.");
        }
        WorldProperties properties = context.world().getProperties();
        properties.setRaining(!weather.equals("clear"));
        properties.setThundering(weather.equals("thunder"));
        properties.setRainTime(6000);
        properties.setThunderTime(6000);
        context.reply("Weather set to " + weather + " for five minutes.");
    }
}
