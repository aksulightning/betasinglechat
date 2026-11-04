package com.aksulightning.vibecode.betasinglechat.gameplay;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.Test;
import static org.junit.Assert.*;

public class HomeStateTest {
    @Test
    public void roundTripRetainsFractionalFeetPositionLookAndDimension() {
        HomeState original = new HomeState("test");
        TeleportPosition overworld = new TeleportPosition(-15.25, 63.5, 22.75, 92.5F, -30, 0);
        TeleportPosition nether = new TeleportPosition(8.5, 42, -18.5, -179, 20.5F, -1);
        original.put("home", overworld);
        original.put("nether_base", nether);
        NbtCompound nbt = new NbtCompound();
        original.writeNbt(nbt);

        HomeState restored = new HomeState("test");
        restored.readNbt(nbt);
        assertEquals(overworld, restored.home("home"));
        assertEquals(nether, restored.home("nether_base"));
        assertEquals(2, restored.homes().size());
    }

    @Test
    public void removingAHomeMarksStateDirtyAndOtherWorldsStaySeparate() {
        HomeState firstWorld = new HomeState("test");
        HomeState secondWorld = new HomeState("test");
        firstWorld.put("home", new TeleportPosition(0.5, 64, 0.5, 0, 0, 0));
        assertNull(secondWorld.home("home"));
        firstWorld.setDirty(false);
        assertTrue(firstWorld.remove("home"));
        assertTrue(firstWorld.isDirty());
        assertNull(firstWorld.home("home"));
        assertFalse(firstWorld.remove("missing"));
    }

    @Test
    public void ignoresInvalidSavedHomesAndClearsOldEntriesWhenLoading() {
        NbtList entries = new NbtList();
        entries.add(entry("bad/name", new TeleportPosition(0, 64, 0, 0, 0, 0)));
        entries.add(entry("nan", new TeleportPosition(Double.NaN, 64, 0, 0, 0, 0)));
        entries.add(entry("too_high", new TeleportPosition(0, 128, 0, 0, 0, 0)));
        entries.add(entry("valid", new TeleportPosition(0.5, 64, 0.5, 0, 0, 0)));
        NbtCompound nbt = new NbtCompound();
        nbt.put("Homes", entries);
        HomeState restored = new HomeState("test");
        restored.put("old", new TeleportPosition(1, 64, 1, 0, 0, 0));
        restored.readNbt(nbt);
        assertEquals(1, restored.homes().size());
        assertNotNull(restored.home("valid"));
        assertNull(restored.home("old"));
    }

    @Test
    public void limitsLoadedHomeCountAndDoesNotExposeMutableStorage() {
        NbtList entries = new NbtList();
        for (int index = 0; index < HomeState.MAX_HOMES + 5; index++) {
            entries.add(entry("home_" + index, new TeleportPosition(0.5, 64, 0.5, 0, 0, 0)));
        }
        NbtCompound nbt = new NbtCompound();
        nbt.put("Homes", entries);
        HomeState restored = new HomeState("test");
        restored.readNbt(nbt);
        assertEquals(HomeState.MAX_HOMES, restored.homes().size());
        assertThrows(UnsupportedOperationException.class, () -> restored.homes().clear());
    }

    @Test
    public void validatesWorldBoundsAndFiniteLookAngles() {
        assertTrue(new TeleportPosition(-30000000, 0, 30000000, 180, 90, -1).isValid());
        assertFalse(new TeleportPosition(30000001, 64, 0, 0, 0, 0).isValid());
        assertFalse(new TeleportPosition(0, -1, 0, 0, 0, 0).isValid());
        assertFalse(new TeleportPosition(0, 64, Double.POSITIVE_INFINITY, 0, 0, 0).isValid());
        assertFalse(new TeleportPosition(0, 64, 0, Float.NaN, 0, 0).isValid());
    }

    private static NbtCompound entry(String name, TeleportPosition position) {
        NbtCompound nbt = position.toNbt();
        nbt.putString("Name", name);
        return nbt;
    }
}
