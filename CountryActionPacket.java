package com.parrotwars.network;

import com.parrotwars.country.CountryManager;
import java.util.function.Supplier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

/** Действия со страной (клиент -> сервер): основать, занять чанк, отпустить чанк. */
public class CountryActionPacket {
    public static final int FOUND = 0;
    public static final int CLAIM = 1;
    public static final int UNCLAIM = 2;

    private final int action;
    private final String name;
    private final int cx;
    private final int cz;

    public CountryActionPacket(int action, String name, int cx, int cz) {
        this.action = action;
        this.name = name;
        this.cx = cx;
        this.cz = cz;
    }

    public static void encode(CountryActionPacket m, PacketBuffer buf) {
        buf.writeInt(m.action);
        buf.writeUtf(m.name);
        buf.writeInt(m.cx);
        buf.writeInt(m.cz);
    }

    public static CountryActionPacket decode(PacketBuffer buf) {
        int action = buf.readInt();
        String name = buf.readUtf(64);
        int cx = buf.readInt();
        int cz = buf.readInt();
        return new CountryActionPacket(action, name, cx, cz);
    }

    private static void msg(ServerPlayerEntity p, String key, Object... args) {
        p.displayClientMessage(new TranslationTextComponent(key, args), true);
    }

    public static void handle(CountryActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            CountryManager manager = CountryManager.get(player.getServer());
            CountryManager.Country mine = manager.getCountry(player.getUUID());

            if (msg.action == FOUND) {
                if (mine != null) {
                    msg(player, "message.parrotwars.country_has", mine.name);
                } else {
                    ChunkPos here = new ChunkPos(player.blockPosition());
                    if (manager.ownerOf(ChunkPos.asLong(here.x, here.z)) != null) {
                        msg(player, "message.parrotwars.country_taken");
                    } else {
                        String name = msg.name == null ? "" : msg.name.trim();
                        if (name.isEmpty()) name = "Parrotland";
                        if (name.length() > 24) name = name.substring(0, 24);
                        manager.found(player.getUUID(), name, here.x, here.z);
                        msg(player, "message.parrotwars.country_founded", name);
                    }
                }
            } else if (mine == null) {
                msg(player, "message.parrotwars.country_none");
            } else if (msg.action == CLAIM) {
                long ch = ChunkPos.asLong(msg.cx, msg.cz);
                if (mine.chunks.contains(ch)) {
                    // уже наше
                } else if (manager.ownerOf(ch) != null) {
                    msg(player, "message.parrotwars.country_taken");
                } else if (!manager.isAdjacent(mine, msg.cx, msg.cz)) {
                    msg(player, "message.parrotwars.country_far");
                } else if (mine.chunks.size() >= CountryManager.MAX_CHUNKS) {
                    msg(player, "message.parrotwars.country_max", CountryManager.MAX_CHUNKS);
                } else {
                    manager.claim(mine, msg.cx, msg.cz);
                    msg(player, "message.parrotwars.country_claimed", mine.chunks.size());
                }
            } else if (msg.action == UNCLAIM) {
                long ch = ChunkPos.asLong(msg.cx, msg.cz);
                if (ch == mine.capital) {
                    msg(player, "message.parrotwars.country_capital");
                } else if (mine.chunks.contains(ch)) {
                    manager.unclaim(mine, msg.cx, msg.cz);
                    msg(player, "message.parrotwars.country_unclaimed", mine.chunks.size());
                }
            }
            CountrySyncPacket.send(player, false);
        });
        ctx.get().setPacketHandled(true);
    }
}
