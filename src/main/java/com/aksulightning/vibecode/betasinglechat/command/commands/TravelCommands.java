package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandDispatcher;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import com.aksulightning.vibecode.betasinglechat.command.SimpleCommand;
import com.aksulightning.vibecode.betasinglechat.gameplay.HomeState;
import com.aksulightning.vibecode.betasinglechat.gameplay.Teleportation;
import com.aksulightning.vibecode.betasinglechat.gameplay.TeleportPosition;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.util.math.Vec3i;

public final class TravelCommands {
    private TravelCommands() {
    }

    public static void register(CommandDispatcher dispatcher) {
        dispatcher.register(new SimpleCommand("tp", "/tp <x> <y> <z>",
                "Teleport safely; Y is feet height and ~ offsets use your current position.", TravelCommands::teleport));
        dispatcher.register(new SimpleCommand("spawn", "/spawn", "Visit a safe spot near Overworld world spawn.",
                (context, args) -> {
                    Arguments.count(args, 0, 0, "/spawn");
                    requireOverworld(context);
                    Teleportation.teleport(context, Teleportation.spawn(context));
                    context.reply("Teleported to world spawn.");
                }));
        dispatcher.register(new SimpleCommand("setspawn", "/setspawn", "Set Overworld world spawn at your feet.",
                (context, args) -> {
                    Arguments.count(args, 0, 0, "/setspawn");
                    requireOverworld(context);
                    TeleportPosition position = TeleportPosition.capture(context.player());
                    if (!position.isValid()) {
                        throw new CommandException("Your position is outside the supported world bounds.");
                    }
                    context.world().setSpawnPos(new Vec3i((int) Math.floor(position.x()),
                            (int) Math.floor(position.y()), (int) Math.floor(position.z())));
                    context.reply("World spawn set. Existing bed respawn points still apply.");
                }));
        dispatcher.register(new SimpleCommand("sethome", "/sethome [name]",
                "Save or replace a home (default: home); maximum 32 per world.", TravelCommands::setHome));
        dispatcher.register(new SimpleCommand("home", "/home [name]",
                "Visit a saved home in your current dimension (default: home).", TravelCommands::home));
        dispatcher.register(new SimpleCommand("homes", "/homes [page]", "List this world's homes and dimensions by page.",
                TravelCommands::homes));
        dispatcher.register(new SimpleCommand("delhome", "/delhome <name>", "Delete a saved home.", (context, args) -> {
            Arguments.count(args, 1, 1, "/delhome <name>");
            String name = homeName(args[0]);
            if (!HomeState.get(context.world()).remove(name)) {
                throw new CommandException("Unknown home: " + name);
            }
            context.reply("Deleted home " + name + ".");
        }));
        dispatcher.register(new SimpleCommand("back", "/back",
                "Return to your last successful command teleport; resets with the player.", (context, args) -> {
                    Arguments.count(args, 0, 0, "/back");
                    Teleportation.back(context);
                    context.reply("Returned to your previous teleport position.");
                }));
    }

    private static void teleport(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 3, 3, "/tp <x> <y> <z>");
        TeleportPosition current = TeleportPosition.capture(context.player());
        TeleportPosition destination = new TeleportPosition(
                Arguments.coordinate(args[0], current.x(), "X"),
                Arguments.coordinate(args[1], current.y(), "Y"),
                Arguments.coordinate(args[2], current.z(), "Z"),
                current.yaw(), current.pitch(), current.dimension());
        Teleportation.teleport(context, destination);
        context.reply(String.format(Locale.ROOT, "Teleported to %.2f %.2f %.2f.",
                destination.x(), destination.y(), destination.z()));
    }

    private static void setHome(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 0, 1, "/sethome [name]");
        String name = args.length == 0 ? "home" : homeName(args[0]);
        HomeState state = HomeState.get(context.world());
        if (state.home(name) == null && state.homes().size() >= HomeState.MAX_HOMES) {
            throw new CommandException("Home limit reached (32). Delete a home first.");
        }
        TeleportPosition position = TeleportPosition.capture(context.player());
        if (!position.isValid()) {
            throw new CommandException("Your position is outside the supported world bounds.");
        }
        state.put(name, position);
        context.reply("Saved home " + name + " in dimension " + position.dimension() + ".");
    }

    private static void home(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 0, 1, "/home [name]");
        String name = args.length == 0 ? "home" : homeName(args[0]);
        TeleportPosition position = HomeState.get(context.world()).home(name);
        if (position == null) {
            throw new CommandException("Unknown home: " + name + ". Save it with /sethome " + name + ".");
        }
        Teleportation.teleport(context, position);
        context.reply("Teleported to home " + name + ".");
    }

    private static void homes(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 0, 1, "/homes [page]");
        Map<String, TeleportPosition> homes = new TreeMap<>(HomeState.get(context.world()).homes());
        int pages = Math.max(1, (homes.size() + 5) / 6);
        int page = args.length == 0 ? 1 : Arguments.integer(args[0], 1, pages, "Homes page");
        if (homes.isEmpty()) {
            context.reply("No homes saved. Use /sethome [name].");
            return;
        }
        context.reply("Homes " + page + "/" + pages + " (" + homes.size() + "/32). Use /homes <page>.");
        List<String> names = List.copyOf(homes.keySet());
        for (int index = (page - 1) * 6; index < Math.min(page * 6, names.size()); index++) {
            String name = names.get(index);
            context.reply(name + " (dimension " + homes.get(name).dimension() + ")");
        }
    }

    private static String homeName(String input) throws CommandException {
        String name = input.toLowerCase(Locale.ROOT);
        if (!name.matches("[a-z0-9_-]{1,32}")) {
            throw new CommandException("Home names need 1-32 letters, digits, underscores, or hyphens.");
        }
        return name;
    }

    private static void requireOverworld(CommandContext context) throws CommandException {
        if (context.world().dimension.id != 0) {
            throw new CommandException("World spawn commands are only available in the Overworld.");
        }
    }
}
