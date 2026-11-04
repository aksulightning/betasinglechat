package com.aksulightning.vibecode.betasinglechat.command.commands;

import com.aksulightning.vibecode.betasinglechat.command.Arguments;
import com.aksulightning.vibecode.betasinglechat.command.Command;
import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import com.aksulightning.vibecode.betasinglechat.mixin.EntityRegistryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.entity.LivingEntity;
import java.lang.reflect.Modifier;
import java.util.Map;

public final class SpawnMobCommand implements Command {
    private static final int MAX_AMOUNT = 32;
    @Override
    public String name() {
        return "spawnmob";
    }

    @Override
    public String usage() {
        return "/spawnmob <mob name or 49> [amount]";
    }

    @Override
    public String description() {
        return "Spawn registered mobs nearby; amount defaults to 1 (maximum 32).";
    }

    @Override
    public void execute(CommandContext context, String[] arguments) throws CommandException {
        Arguments.count(arguments, 1, 2, usage());
        int amount = arguments.length == 2
                ? Arguments.integer(arguments[1], 1, MAX_AMOUNT, "Amount") : 1;
        // Beta registers raw entity ID 49 as "Monster".
        String requestedMob = arguments[0].equals("49") ? "Monster" : arguments[0];
        String id = null;
        for (Map.Entry<String, Class<? extends Entity>> entry
                : EntityRegistryAccessor.betasinglechat$getIdToClass().entrySet()) {
            Class<? extends Entity> type = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(requestedMob)
                    && LivingEntity.class.isAssignableFrom(type)
                    && type != LivingEntity.class
                    && !Modifier.isAbstract(type.getModifiers())) {
                id = entry.getKey();
                break;
            }
        }
        if (id == null) {
            throw new CommandException("Unknown mob: " + arguments[0]);
        }
        int spawned = 0;
        while (spawned < amount && spawnOne(context, id)) {
            spawned++;
        }
        if (spawned == 0) {
            throw new CommandException("No room to spawn " + id + " near you.");
        }
        context.reply("Spawned " + spawned + " " + id + (spawned < amount
                ? " (requested " + amount + "; no more room nearby)." : "."));
    }

    private boolean spawnOne(CommandContext context, String id) throws CommandException {
        Entity entity = EntityRegistry.create(id, context.world());
        if (!(entity instanceof LivingEntity)) {
            throw new CommandException("Could not create mob: " + id);
        }
        // Try nearby positions at foot height, including small changes in terrain.
        double feetY = context.player().boundingBox.minY;
        int[] heightOffsets = {0, 1, -1, 2, -2};
        for (int heightOffset : heightOffsets) {
            for (int direction = 0; direction < 8; direction++) {
                double angle = Math.toRadians(context.player().yaw + direction * 45.0);
                entity.setPositionAndAngles(context.player().x - Math.sin(angle) * 3.0,
                        feetY + heightOffset, context.player().z + Math.cos(angle) * 3.0,
                        context.player().yaw, 0.0F);
                if (entity.boundingBox.minY < 0.0 || entity.boundingBox.maxY > 128.0
                        || !context.world().getEntityCollisions(entity, entity.boundingBox).isEmpty()
                        || !context.world().canSpawnEntity(entity.boundingBox)) {
                    continue;
                }
                if (context.world().spawnEntity(entity)) {
                    return true;
                }
            }
        }
        return false;
    }
}
