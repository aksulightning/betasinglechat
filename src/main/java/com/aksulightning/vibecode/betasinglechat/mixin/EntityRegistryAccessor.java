package com.aksulightning.vibecode.betasinglechat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(EntityRegistry.class)
public interface EntityRegistryAccessor {
    @Accessor("idToClass")
    static Map<String, Class<? extends Entity>> betasinglechat$getIdToClass() {
        throw new AssertionError("Entity registry accessor was not applied.");
    }
}
