package com.parrotwars.network;

import com.parrotwars.client.ClientHooks;
import com.parrotwars.country.CountryManager;
import com.parrotwars.entity.CitizenEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

/** Данные о странах для карты (сервер -> клиент). open=true: ещё и открыть карту. */
public class CountrySyncPacket {

    public static class View {
        public String name;
        public int color;
        public boolean mine;
        public boolean enemy;
        public long capital;
        public int citizens;
        public int approval;
        public long[] chunks;
        public long[] houses;
    }

    public final boolean open;
    public final List<View> views;

    public CountrySyncPacket(boolean open, List<View> views) {
        this.open = open;
        this.views = views;
    }

    /** Собрать данные и отправить игроку. */
    public static void send(ServerPlayerEntity player, boolean open) {
        CountryManager manager = CountryManager.get(player.getServer());

        Map<UUID, int[]> stats = new HashMap<>();
        for (Entity e : player.getServer().overworld().getAllEntities()) {
            if (e instanceof CitizenEntity) {
                CitizenEntity c = (CitizenEntity) e;
                if (c.getRuler() == null) continue;
                int[] s = stats.computeIfAbsent(c.getRuler(), k -> new int[2]);
                s[0]++;
                s[1] += c.getApproval();
            }
        }

        List<View> views = new ArrayList<>();
        for (Map.Entry<UUID, CountryManager.Country> e : manager.all().entrySet()) {
            CountryManager.Country c = e.getValue();
            View v = new View();
            v.name = c.name;
            v.color = c.color;
            v.mine = e.getKey().equals(player.getUUID());
            v.enemy = c.npc;
            v.capital = c.capital;
            int[] s = stats.get(e.getKey());
            v.citizens = s == null ? 0 : s[0];
            v.approval = s == null || s[0] == 0 ? -1 : s[1] / s[0];
            v.chunks = CountryManager.toArray(c.chunks);
            v.houses = CountryManager.toArray(c.houses);
            views.add(v);
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CountrySyncPacket(open, views));
    }

    public static void encode(CountrySyncPacket m, PacketBuffer buf) {
        buf.writeBoolean(m.open);
        buf.writeVarInt(m.views.size());
        for (View v : m.views) {
            buf.writeUtf(v.name);
            buf.writeInt(v.color);
            buf.writeBoolean(v.mine);
            buf.writeBoolean(v.enemy);
            buf.writeLong(v.capital);
            buf.writeInt(v.citizens);
            buf.writeInt(v.approval);
            buf.writeLongArray(v.chunks);
            buf.writeLongArray(v.houses);
        }
    }

    public static CountrySyncPacket decode(PacketBuffer buf) {
        boolean open = buf.readBoolean();
        int n = buf.readVarInt();
        List<View> views = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            View v = new View();
            v.name = buf.readUtf(64);
            v.color = buf.readInt();
            v.mine = buf.readBoolean();
            v.enemy = buf.readBoolean();
            v.capital = buf.readLong();
            v.citizens = buf.readInt();
            v.approval = buf.readInt();
            v.chunks = buf.readLongArray(null);
            v.houses = buf.readLongArray(null);
            views.add(v);
        }
        return new CountrySyncPacket(open, views);
    }

    public static void handle(CountrySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onSync(msg)));
        ctx.get().setPacketHandled(true);
    }
}
