package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Command;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class GiveCommand implements Command {
    // One inventory's worth of 64-item stacks; bounds work and dropped entities.
    private static final int MAX_AMOUNT = 36 * 64;

    @Override
    public String name() {
        return "give";
    }

    @Override
    public String usage() {
        return "/give <item ID> [amount]";
    }

    @Override
    public void execute(CommandContext context, String[] arguments) throws CommandException {
        if (arguments.length < 1 || arguments.length > 2) {
            throw new CommandException("Usage: " + usage());
        }
        int id;
        try {
            id = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException exception) {
            throw new CommandException("Item ID must be a whole number.");
        }
        if (id <= 0 || id >= Item.ITEMS.length || Item.ITEMS[id] == null) {
            throw new CommandException("Invalid item ID: " + id);
        }
        int amount = 1;
        if (arguments.length == 2) {
            try {
                amount = Integer.parseInt(arguments[1]);
            } catch (NumberFormatException exception) {
                throw new CommandException("Amount must be a whole number from 1 to " + MAX_AMOUNT + ".");
            }
        }
        if (amount < 1 || amount > MAX_AMOUNT) {
            throw new CommandException("Amount must be from 1 to " + MAX_AMOUNT + ".");
        }
        int remaining = amount;
        int dropped = 0;
        int stackLimit = Math.min(64, Item.ITEMS[id].getMaxCount());
        while (remaining > 0) {
            int count = Math.min(remaining, stackLimit);
            ItemStack stack = new ItemStack(id, count, 0);
            context.player().inventory.addStack(stack);
            // Beta's addStack may return true even when only part of a stack fits.
            if (stack.count > 0) {
                dropped += stack.count;
                context.player().dropItem(stack, false);
            }
            remaining -= count;
        }
        context.player().inventory.dirty = true;
        context.reply("Gave " + amount + " of item ID " + id + ".");
        if (dropped > 0) {
            context.reply("Inventory full: dropped " + dropped + " near you.");
        }
    }
}
