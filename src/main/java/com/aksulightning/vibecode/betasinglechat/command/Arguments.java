package com.aksulightning.vibecode.betasinglechat.command;

public final class Arguments {
    private Arguments() {
    }

    public static void count(String[] arguments, int min, int max, String usage)
            throws CommandException {
        if (arguments.length < min || arguments.length > max) {
            throw new CommandException("Usage: " + usage);
        }
    }

    public static int integer(String input, int min, int max, String label) throws CommandException {
        try {
            int value = Integer.parseInt(input);
            if (value >= min && value <= max) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new CommandException(label + " must be a whole number from " + min + " to " + max + ".");
    }

    public static double number(String input, String label) throws CommandException {
        try {
            double value = Double.parseDouble(input);
            if (Double.isFinite(value)) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new CommandException(label + " must be a finite number.");
    }

    public static double coordinate(String input, double origin, String label) throws CommandException {
        double value;
        if (input.startsWith("~")) {
            value = origin + (input.length() == 1 ? 0 : number(input.substring(1), label));
        } else {
            value = number(input, label);
        }
        if (!Double.isFinite(value)) {
            throw new CommandException(label + " must resolve to a finite coordinate.");
        }
        return value;
    }
}
