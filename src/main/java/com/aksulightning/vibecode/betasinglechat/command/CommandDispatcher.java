package com.aksulightning.vibecode.betasinglechat.command;

import com.aksulightning.vibecode.betasinglechat.command.commands.GiveCommand;
import com.aksulightning.vibecode.betasinglechat.command.commands.GodCommand;
import com.aksulightning.vibecode.betasinglechat.command.commands.HelpCommand;
import com.aksulightning.vibecode.betasinglechat.command.commands.PlayerCommands;
import com.aksulightning.vibecode.betasinglechat.command.commands.SpawnMobCommand;
import com.aksulightning.vibecode.betasinglechat.command.commands.TravelCommands;
import com.aksulightning.vibecode.betasinglechat.command.commands.WorldCommands;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommandDispatcher {
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public static CommandDispatcher createDefault() {
        CommandDispatcher dispatcher = new CommandDispatcher();
        dispatcher.register(new HelpCommand(dispatcher));
        dispatcher.register(new GiveCommand());
        dispatcher.register(new SpawnMobCommand());
        dispatcher.register(new GodCommand());
        WorldCommands.register(dispatcher);
        TravelCommands.register(dispatcher);
        PlayerCommands.register(dispatcher);
        return dispatcher;
    }

    public List<Command> commands() {
        return List.copyOf(commands.values());
    }

    public Command find(String name) {
        return commands.get(name.toLowerCase(Locale.ROOT));
    }

    public void register(Command command) {
        String name = command.name().toLowerCase(Locale.ROOT);
        if (commands.putIfAbsent(name, command) != null) {
            throw new IllegalArgumentException("Command already registered: " + name);
        }
    }

    public void execute(CommandContext context, String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            trimmed = "help";
        }
        String[] tokens = trimmed.split("\\s+");
        Command command = commands.get(tokens[0].toLowerCase(Locale.ROOT));
        if (command == null) {
            context.error("Unknown command: /" + tokens[0] + ". Use /help for available commands.");
            return;
        }
        try {
            command.execute(context, Arrays.copyOfRange(tokens, 1, tokens.length));
        } catch (CommandException exception) {
            context.error(exception.getMessage());
        }
    }
}
