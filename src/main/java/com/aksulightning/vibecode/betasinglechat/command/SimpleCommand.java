package com.aksulightning.vibecode.betasinglechat.command;

public record SimpleCommand(String name, String usage, String description,
                            Action action) implements Command {
    @FunctionalInterface
    public interface Action {
        void execute(CommandContext context, String[] arguments) throws CommandException;
    }

    @Override
    public void execute(CommandContext context, String[] arguments) throws CommandException {
        action.execute(context, arguments);
    }
}
