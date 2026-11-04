package com.aksulightning.vibecode.betasinglechat.gameplay;

import com.aksulightning.vibecode.betasinglechat.command.CommandContext;
import com.aksulightning.vibecode.betasinglechat.command.CommandException;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public final class Teleportation {
    private static final Map<PlayerEntity, TeleportPosition> PREVIOUS = new WeakHashMap<>();

    private Teleportation() {
    }

    public static void teleport(CommandContext context, TeleportPosition destination) throws CommandException {
        PlayerEntity player = context.player();
        if (player.vehicle != null || player.passenger != null) {
            throw new CommandException("Dismount and remove passengers before teleporting.");
        }
        if (destination.dimension() != context.world().dimension.id) {
            throw new CommandException("Destination is in dimension " + destination.dimension()
                    + ". Enter that dimension before teleporting.");
        }
        if (!destination.isValid() || destination.y() + player.height > 128) {
            throw new CommandException("Destination must be within X/Z +/-30000000 and fit below Y 128.");
        }
        loadDestination(context.world(), destination.x(), destination.z());
        if (!isSafe(context.world(), player, destination)) {
            throw new CommandException("Destination is unsafe: choose open space above solid ground.");
        }
        TeleportPosition previous = TeleportPosition.capture(player);
        // This Beta method takes feet Y and adds the player's standing eye offset itself.
        player.cameraOffset = 0;
        player.setPositionAndAnglesKeepPrevAngles(destination.x(), destination.y(), destination.z(),
                destination.yaw(), destination.pitch());
        player.prevYaw = player.yaw;
        player.prevPitch = player.pitch;
        player.velocityX = 0;
        player.velocityY = 0;
        player.velocityZ = 0;
        player.onGround = false;
        ((FlightState) player).betasinglechat$resetFallDistance();
        context.world().updateEntity(player, false);
        PREVIOUS.put(player, previous);
    }

    public static void back(CommandContext context) throws CommandException {
        TeleportPosition previous = PREVIOUS.get(context.player());
        if (previous == null) {
            throw new CommandException("No previous teleport in this player session.");
        }
        teleport(context, previous);
    }

    public static TeleportPosition spawn(CommandContext context) throws CommandException {
        Vec3i spawn = context.world().getSpawnPos();
        PlayerEntity player = context.player();
        for (int radius = 0; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    double x = spawn.x + dx + 0.5;
                    double z = spawn.z + dz + 0.5;
                    loadDestination(context.world(), x, z);
                    int surface = context.world().getTopSolidBlockY(spawn.x + dx, spawn.z + dz);
                    for (int y : new int[] {spawn.y, surface}) {
                        TeleportPosition candidate = new TeleportPosition(x, y, z, player.yaw, player.pitch, 0);
                        if (candidate.isValid() && isSafe(context.world(), player, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        throw new CommandException("No safe place found within four blocks of world spawn.");
    }

    private static void loadDestination(World world, double x, double z) {
        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getChunk(chunkX + dx, chunkZ + dz);
            }
        }
    }

    private static boolean isSafe(World world, PlayerEntity player, TeleportPosition position) {
        double halfWidth = player.width / 2.0;
        Box body = Box.create(position.x() - halfWidth, position.y(), position.z() - halfWidth,
                position.x() + halfWidth, position.y() + player.height, position.z() + halfWidth);
        if (body.minY < 0 || body.maxY > 128 || !world.getEntityCollisions(player, body).isEmpty()) {
            return false;
        }
        for (Object other : world.getEntities(player, body)) {
            if (other instanceof Entity entity && entity.isCollidable() && !entity.dead
                    && entity.boundingBox.intersects(body)) {
                return false;
            }
        }
        // Include the surface underfoot to reject cactus, fire, fluids, and portals.
        for (int x = (int) Math.floor(body.minX); x <= (int) Math.floor(body.maxX); x++) {
            for (int y = Math.max(0, (int) Math.floor(body.minY - 0.05)); y < Math.ceil(body.maxY); y++) {
                for (int z = (int) Math.floor(body.minZ); z <= (int) Math.floor(body.maxZ); z++) {
                    Material material = world.getMaterial(x, y, z);
                    if (material.isFluid() || material == Material.FIRE || material == Material.CACTUS
                            || material == Material.NETHER_PORTAL) {
                        return false;
                    }
                }
            }
        }
        return ((FlightState) player).betasinglechat$isFlying()
                || !world.getEntityCollisions(player, body.offset(0, -0.1, 0)).isEmpty();
    }
}
