package com.aksulightning.vibecode.betasinglechat.command;

public interface Command {
    String name();

    String usage();

    void execute(CommandContext context, String[] arguments) throws CommandException;
}
