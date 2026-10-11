package com.parrotwars.network;

import com.parrotwars.entity.ArmyUnit;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.network.NetworkEvent;

/** Приказ войскам с карты (клиент -> сервер). */
public class OrderPacket {
    public static final int MOVE = 0;
    public static final int FOLLOW = 1;
    public static final int HOLD = 2;
    public static final int ATTACK = 3;

    private final int mode;
    private final int[] ids;
    private final int x;
    private final int z;
    private final int target;

    public OrderPacket(int mode, int[] ids, int x, int z, int target) {
        this.mode = mode;
        this.ids = ids;
        this.x = x;
        this.z = z;
        this.target = target;
    }

    public static void encode(OrderPacket m, PacketBuffer buf) {
        buf.writeInt(m.mode);
        buf.writeVarIntArray(m.ids);
        buf.writeInt(m.x);
        buf.writeInt(m.z);
        buf.writeInt(m.target);
    }

    public static OrderPacket decode(PacketBuffer buf) {
        int mode = buf.readInt();
        int[] ids = buf.readVarIntArray();
        int x = buf.readInt();
        int z = buf.readInt();
        int target = buf.readInt();
        return new OrderPacket(mode, ids, x, z, target);
    }

    public static void handle(OrderPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            Entity targetEntity = msg.mode == ATTACK ? player.level.getEntity(msg.target) : null;
            int count = Math.min(msg.ids.length, 300);
            for (int i = 0; i < count; i++) {
                Entity e = player.level.getEntity(msg.ids[i]);
                if (!(e instanceof ArmyUnit) || !((ArmyUnit) e).isOwnedByPlayer(player)) continue;
                ArmyUnit unit = (ArmyUnit) e;
                switch (msg.mode) {
                    case MOVE: {
                        BlockPos probe = new BlockPos(msg.x, 64, msg.z);
                        int y = player.level.hasChunkAt(probe)
                                ? player.level.getHeight(Heightmap.Type.WORLD_SURFACE, msg.x, msg.z)
                                : (int) player.getY();
                        unit.orderMove(new BlockPos(msg.x, y, msg.z));
                        break;
                    }
                    case FOLLOW:
                        unit.orderFollow();
                        break;
                    case HOLD:
                        unit.orderHold();
                        break;
                    case ATTACK:
                        if (targetEntity instanceof LivingEntity) {
                            unit.orderAttack((LivingEntity) targetEntity);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
