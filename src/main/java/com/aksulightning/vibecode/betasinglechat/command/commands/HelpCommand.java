package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.Command;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandDispatcher;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import java.util.List;

public final class HelpCommand implements Command {
    private static final int PAGE_SIZE = 6;
    private final CommandDispatcher dispatcher;

    public HelpCommand(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String usage() {
        return "/help [command or page]";
    }

    @Override
    public String description() {
        return "List commands by page, or show a command's usage and description.";
    }

    @Override
    public void execute(CommandContext context, String[] arguments) throws CommandException {
        Arguments.count(arguments, 0, 1, usage());
        List<Command> commands = dispatcher.commands();
        int pages = (commands.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int page = 1;
        if (arguments.length == 1) {
            String query = arguments[0].startsWith("/") ? arguments[0].substring(1) : arguments[0];
            Command command = dispatcher.find(query);
            if (command != null) {
                context.reply(command.usage());
                context.reply(command.description());
                return;
            }
            if (!query.matches("[0-9]+")) {
                throw new CommandException("Unknown command: " + query + ". Use /help for the list.");
            }
            page = Arguments.integer(query, 1, pages, "Help page");
        }
        context.reply("Commands (" + page + "/" + pages + "). Use /help <command> for details.");
        for (int index = (page - 1) * PAGE_SIZE; index < Math.min(page * PAGE_SIZE, commands.size()); index++) {
            context.reply(commands.get(index).usage());
        }
    }
}
