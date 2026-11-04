package com.aksulightning.vibecode.betasinglechat.command;

public interface Command {
    String name();

    String usage();

    String description();

    void execute(CommandContext context, String[] arguments) throws CommandException;
}
