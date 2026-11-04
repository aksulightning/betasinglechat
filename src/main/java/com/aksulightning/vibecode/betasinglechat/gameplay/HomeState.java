package com.aksulightning.vibecode.betasinglechat.gameplay;

import java.util.Map;
import java.util.TreeMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public final class HomeState extends PersistentState {
    public static final int MAX_HOMES = 32;
    private static final String ID = "betasinglechat_homes";
    private final Map<String, TeleportPosition> homes = new TreeMap<>();

    // Vanilla's SavedDataStorage reconstructs states using this constructor.
    public HomeState(String id) {
        super(id);
    }

    public static HomeState get(World world) {
        HomeState state = (HomeState) world.getOrCreateState(HomeState.class, ID);
        if (state == null) {
            state = new HomeState(ID);
            world.setState(ID, state);
        }
        return state;
    }

    public Map<String, TeleportPosition> homes() {
        return Map.copyOf(homes);
    }

    public TeleportPosition home(String name) {
        return homes.get(name);
    }

    public void put(String name, TeleportPosition position) {
        homes.put(name, position);
        markDirty();
    }

    public boolean remove(String name) {
        if (homes.remove(name) == null) {
            return false;
        }
        markDirty();
        return true;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        homes.clear();
        NbtList entries = nbt.getList("Homes");
        for (int index = 0; index < entries.size() && homes.size() < MAX_HOMES; index++) {
            if (entries.get(index) instanceof NbtCompound entry) {
                String name = entry.getString("Name");
                TeleportPosition position = TeleportPosition.fromNbt(entry);
                if (name.matches("[a-z0-9_-]{1,32}") && position.isValid()) {
                    homes.put(name, position);
                }
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        NbtList entries = new NbtList();
        homes.forEach((name, position) -> {
            NbtCompound entry = position.toNbt();
            entry.putString("Name", name);
            entries.add(entry);
        });
        nbt.put("Homes", entries);
    }
}
