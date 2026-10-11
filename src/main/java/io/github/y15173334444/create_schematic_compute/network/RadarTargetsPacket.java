package io.github.y15173334444.create_schematic_compute.network;

import io.github.y15173334444.create_schematic_compute.SchematicCompute;
import io.github.y15173334444.create_schematic_compute.graph.TargetRecord;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/** 服务端→客户端：雷达目标**轻量通道**（20Hz 精度）。
 *  目标/锁定/分配的原始变化走本包全量率推送——blip 位置恢复 20Hz 刷新；全量 BE NBT
 *  （整图）只在语义变化（设置/成员/名称）时推。运动数据永远不搭整图的车（最小数据面）。
 *  Server→client: the radar's **light targets channel** (20Hz precision). Raw changes to
 *  targets/locks/assignments ride this packet at full rate — blip positions refresh at
 *  20Hz again — while the full BE NBT (whole graph) pushes only on semantic change
 *  (settings/membership/names). Motion never rides the whole-graph payload (minimal
 *  data plane). */
public record RadarTargetsPacket(BlockPos pos, List<TargetRecord> targets, int[] locked, int[] active)
        implements CustomPacketPayload {

    public static final Type<RadarTargetsPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(SchematicCompute.MOD_ID, "radar_targets"));

    public static final StreamCodec<ByteBuf, RadarTargetsPacket> CODEC = new StreamCodec<>() {
        @Override public RadarTargetsPacket decode(ByteBuf buf) {
            var b = new FriendlyByteBuf(buf);
            BlockPos p = b.readBlockPos();
            int count = b.readVarInt();
            var list = new ArrayList<TargetRecord>(count);
            for (int i = 0; i < count; i++) {
                double x = b.readDouble(), y = b.readDouble(), z = b.readDouble();
                int id = b.readVarInt();
                float dist = b.readFloat();
                String type = b.readUtf();
                String name = b.readUtf();
                list.add(new TargetRecord(x, y, z, id, dist, type, name));
            }
            int[] locked = b.readVarIntArray();
            int[] active = b.readVarIntArray();
            return new RadarTargetsPacket(p, list, locked, active);
        }
        @Override public void encode(ByteBuf buf, RadarTargetsPacket pkt) {
            var b = new FriendlyByteBuf(buf);
            b.writeBlockPos(pkt.pos);
            var targets = pkt.targets;
            b.writeVarInt(targets != null ? targets.size() : 0);
            if (targets != null) {
                for (var t : targets) {
                    b.writeDouble(t.x()); b.writeDouble(t.y()); b.writeDouble(t.z());
                    b.writeVarInt(t.entityId());
                    b.writeFloat(t.distance());
                    b.writeUtf(t.entityType() != null ? t.entityType() : "");
                    b.writeUtf(t.name() != null ? t.name() : "");
                }
            }
            b.writeVarIntArray(pkt.locked != null ? pkt.locked : new int[0]);
            b.writeVarIntArray(pkt.active != null ? pkt.active : new int[0]);
        }
    };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var level = ctx.player().level();
            if (level == null) return;
            if (level.getBlockEntity(pos)
                instanceof io.github.y15173334444.create_schematic_compute.blocks.RadarBlockEntity radar) {
                radar.applyTargetsSync(targets, locked, active);
            }
        });
    }
}
