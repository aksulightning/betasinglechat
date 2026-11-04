package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.Command;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public final class ClearCommand implements Command {
    private static final long CONFIRM_TIMEOUT = 30_000_000_000L;
    private final Map<PlayerEntity, Pending> pending = new WeakHashMap<>();

    private record Pending(ItemStack[] stacks, long createdAt) {
    }

    @Override
    public String name() {
        return "clear";
    }

    @Override
    public String usage() {
        return "/clear [confirm]";
    }

    @Override
    public String description() {
        return "Clear main inventory, armor, and cursor stack after /clear confirm within 30 seconds.";
    }

    @Override
    public void execute(CommandContext context, String[] args) throws CommandException {
        Arguments.count(args, 0, 1, usage());
        if (args.length == 1 && !args[0].equalsIgnoreCase("confirm")) {
            throw new CommandException("Usage: " + usage());
        }
        PlayerInventory inventory = context.player().inventory;
        ItemStack[] current = snapshot(inventory);
        if (args.length == 0) {
            int items = 0;
            for (ItemStack stack : current) {
                if (stack != null) {
                    items += stack.count;
                }
            }
            if (items == 0) {
                pending.remove(context.player());
                context.reply("Your inventory is already empty.");
                return;
            }
            pending.put(context.player(), new Pending(current, System.nanoTime()));
            context.reply("Delete " + items + " items including armor? Use /clear confirm within 30 seconds.");
            return;
        }
        Pending confirmation = pending.remove(context.player());
        if (confirmation == null || System.nanoTime() - confirmation.createdAt() > CONFIRM_TIMEOUT) {
            throw new CommandException("No active confirmation. Use /clear first.");
        }
        if (current.length != confirmation.stacks().length) {
            throw new CommandException("Inventory changed. Use /clear again.");
        }
        for (int index = 0; index < current.length; index++) {
            if (!ItemStack.areEqual(current[index], confirmation.stacks()[index])) {
                throw new CommandException("Inventory changed. Use /clear again.");
            }
        }
        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.setStack(slot, null);
        }
        inventory.setCursorStack(null);
        inventory.markDirty();
        context.reply("Inventory cleared.");
    }

    private static ItemStack[] snapshot(PlayerInventory inventory) {
        ItemStack[] stacks = new ItemStack[inventory.size() + 1];
        for (int slot = 0; slot < inventory.size(); slot++) {
            stacks[slot] = ItemStack.clone(inventory.getStack(slot));
        }
        stacks[stacks.length - 1] = ItemStack.clone(inventory.getCursorStack());
        return stacks;
    }
}
