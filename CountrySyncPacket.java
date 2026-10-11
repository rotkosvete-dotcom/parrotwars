package com.parrotwars.network;

import com.parrotwars.client.ClientHooks;
import com.parrotwars.country.CountryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
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
        public long[] chunks;
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
        List<View> views = new ArrayList<>();
        for (Map.Entry<UUID, CountryManager.Country> e : manager.all().entrySet()) {
            View v = new View();
            v.name = e.getValue().name;
            v.color = e.getValue().color;
            v.mine = e.getKey().equals(player.getUUID());
            long[] arr = new long[e.getValue().chunks.size()];
            int i = 0;
            for (long ch : e.getValue().chunks) arr[i++] = ch;
            v.chunks = arr;
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
            buf.writeLongArray(v.chunks);
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
            v.chunks = buf.readLongArray(null);
            views.add(v);
        }
        return new CountrySyncPacket(open, views);
    }

    public static void handle(CountrySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onSync(msg)));
        ctx.get().setPacketHandled(true);
    }
}
