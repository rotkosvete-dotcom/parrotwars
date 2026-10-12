package com.parrotwars.country;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;

/** Хранит страны (игроков и вражескую республику): чанки, дома, силу армии. Сохраняется с миром. */
public class CountryManager extends WorldSavedData {
    public static final String NAME = "parrotwars_countries";
    public static final UUID ENEMY_ID = new UUID(0x50494745L, 0x4F4E5F31L);
    public static final int MAX_CHUNKS = 60;
    private static final int[] PALETTE = {0x1E88E5, 0x43A047, 0xFDD835, 0x8E24AA, 0xFB8C00, 0x00ACC1, 0xD81B60, 0x6D4C41};

    public static class Country {
        public String name;
        public int color;
        public long capital;
        public boolean npc = false;
        public int strength = 0;
        public long nextRaid = 0L;
        public final Set<Long> chunks = new HashSet<>();
        public final Set<Long> houses = new HashSet<>();
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

    /** Создать вражескую республику (один раз) далеко от первой страны игрока. */
    public void ensureEnemy(int nearCx, int nearCz, Random rand, long time) {
        if (countries.containsKey(ENEMY_ID)) {
            return;
        }
        double angle = rand.nextDouble() * Math.PI * 2.0D;
        int dist = 18 + rand.nextInt(8);
        int cx = nearCx + (int) Math.round(Math.cos(angle) * dist);
        int cz = nearCz + (int) Math.round(Math.sin(angle) * dist);
        Country c = new Country();
        c.name = "Голубиная Республика";
        c.color = 0xD32F2F;
        c.npc = true;
        c.strength = 10;
        c.nextRaid = time + 6000L;
        c.capital = ChunkPos.asLong(cx, cz);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                long ch = ChunkPos.asLong(cx + dx, cz + dz);
                if (ownerOf(ch) == null) c.chunks.add(ch);
            }
        }
        countries.put(ENEMY_ID, c);
        setDirty();
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

    public void addHouse(Country c, BlockPos center) {
        c.houses.add(center.asLong());
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
            c.npc = t.getBoolean("Npc");
            c.strength = t.getInt("Strength");
            c.nextRaid = t.getLong("NextRaid");
            for (long ch : t.getLongArray("Chunks")) c.chunks.add(ch);
            for (long h : t.getLongArray("Houses")) c.houses.add(h);
            countries.put(t.getUUID("Owner"), c);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (Map.Entry<UUID, Country> e : countries.entrySet()) {
            Country c = e.getValue();
            CompoundNBT t = new CompoundNBT();
            t.putUUID("Owner", e.getKey());
            t.putString("Name", c.name);
            t.putInt("Color", c.color);
            t.putLong("Capital", c.capital);
            t.putBoolean("Npc", c.npc);
            t.putInt("Strength", c.strength);
            t.putLong("NextRaid", c.nextRaid);
            t.putLongArray("Chunks", toArray(c.chunks));
            t.putLongArray("Houses", toArray(c.houses));
            list.add(t);
        }
        tag.put("Countries", list);
        return tag;
    }

    public static long[] toArray(Set<Long> set) {
        long[] arr = new long[set.size()];
        int i = 0;
        for (long v : set) arr[i++] = v;
        return arr;
    }
}
