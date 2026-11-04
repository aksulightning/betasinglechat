package com.aksulightning.vibecode.betasinglechat.gameplay;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public record TeleportPosition(double x, double y, double z, float yaw, float pitch, int dimension) {
    public static TeleportPosition capture(PlayerEntity player) {
        return new TeleportPosition(player.x, player.boundingBox.minY, player.z,
                player.yaw, player.pitch, player.world.dimension.id);
    }

    public boolean isValid() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
                && Float.isFinite(yaw) && Float.isFinite(pitch)
                && Math.abs(x) <= 30000000 && Math.abs(z) <= 30000000 && y >= 0 && y < 128;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("X", x);
        nbt.putDouble("Y", y);
        nbt.putDouble("Z", z);
        nbt.putFloat("Yaw", yaw);
        nbt.putFloat("Pitch", pitch);
        nbt.putInt("Dimension", dimension);
        return nbt;
    }

    public static TeleportPosition fromNbt(NbtCompound nbt) {
        return new TeleportPosition(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"),
                nbt.getFloat("Yaw"), nbt.getFloat("Pitch"), nbt.getInt("Dimension"));
    }
}
