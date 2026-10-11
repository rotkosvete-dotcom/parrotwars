package com.parrotwars.country;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;

/** Хранит страны игроков (чанки территории). Сохраняется вместе с миром. */
public class CountryManager extends WorldSavedData {
    public static final String NAME = "parrotwars_countries";
    public static final int MAX_CHUNKS = 60;
    private static final int[] PALETTE = {0xE53935, 0x1E88E5, 0x43A047, 0xFDD835, 0x8E24AA, 0xFB8C00, 0x00ACC1, 0xD81B60};

    public static class Country {
        public String name;
        public int color;
        public long capital;
        public final Set<Long> chunks = new HashSet<>();
    }

    private final Map<UUID, Country> countries = new HashMap<>();

    public CountryManager() {
        super(NAME);
    }

    public static CountryManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CountryManager::new, NAME);
    }

    public Map<UUID, Country> all() {
        return countries;
    }

    public Country getCountry(UUID owner) {
        return countries.get(owner);
    }

    public UUID ownerOf(long chunk) {
        for (Map.Entry<UUID, Country> e : countries.entrySet()) {
            if (e.getValue().chunks.contains(chunk)) return e.getKey();
        }
        return null;
    }

    /** Основать страну: захватывает 3x3 чанка вокруг центра (свободные). */
    public Country found(UUID owner, String name, int cx, int cz) {
        Country c = new Country();
        c.name = name;
        c.color = PALETTE[Math.abs(owner.hashCode()) % PALETTE.length];
        c.capital = ChunkPos.asLong(cx, cz);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long ch = ChunkPos.asLong(cx + dx, cz + dz);
                if (ownerOf(ch) == null) c.chunks.add(ch);
            }
        }
        countries.put(owner, c);
        setDirty();
        return c;
    }

    public boolean isAdjacent(Country c, int cx, int cz) {
        return c.chunks.contains(ChunkPos.asLong(cx + 1, cz)) || c.chunks.contains(ChunkPos.asLong(cx - 1, cz))
                || c.chunks.contains(ChunkPos.asLong(cx, cz + 1)) || c.chunks.contains(ChunkPos.asLong(cx, cz - 1));
    }

    public void claim(Country c, int cx, int cz) {
        c.chunks.add(ChunkPos.asLong(cx, cz));
        setDirty();
    }

    public void unclaim(Country c, int cx, int cz) {
        c.chunks.remove(ChunkPos.asLong(cx, cz));
        setDirty();
    }

    @Override
    public void load(CompoundNBT tag) {
        countries.clear();
        ListNBT list = tag.getList("Countries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT t = list.getCompound(i);
            Country c = new Country();
            c.name = t.getString("Name");
            c.color = t.getInt("Color");
            c.capital = t.getLong("Capital");
            for (long ch : t.getLongArray("Chunks")) c.chunks.add(ch);
            countries.put(t.getUUID("Owner"), c);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (Map.Entry<UUID, Country> e : countries.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putUUID("Owner", e.getKey());
            t.putString("Name", e.getValue().name);
            t.putInt("Color", e.getValue().color);
            t.putLong("Capital", e.getValue().capital);
            long[] arr = new long[e.getValue().chunks.size()];
            int i = 0;
            for (long ch : e.getValue().chunks) arr[i++] = ch;
            t.putLongArray("Chunks", arr);
            list.add(t);
        }
        tag.put("Countries", list);
        return tag;
    }
}
