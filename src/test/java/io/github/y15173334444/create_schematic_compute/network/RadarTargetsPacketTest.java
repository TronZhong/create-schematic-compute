package io.github.y15173334444.create_schematic_compute.network;

import io.github.y15173334444.create_schematic_compute.graph.TargetRecord;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雷达轻量目标包编解码往返回归：blip 20Hz 数据面的保真性——目标逐字段、参考系
 * （扫描中心+姿态）、锁定/分配集。参考系曾搭全量 NBT 的车按语义频率慢刷（姿态换算/
 * 扫描中心移动卡顿），必须随轻量包逐 tick 保真。
 * Radar light-targets packet codec round-trip: fidelity of the 20Hz blip data plane —
 * per-field targets, the reference frame (scan center + attitude) and the lock/assignment
 * sets. The frame used to ride the full NBT at semantic frequency (slow attitude
 * conversion / scan-center motion) — it must survive the light channel tick-for-tick.
 */
class RadarTargetsPacketTest {

    @Test
    @DisplayName("编解码往返：目标逐字段 + 参考系 + 锁定/分配集保真")
    void codecRoundTripPreservesEverything() {
        var frame = new RadarTargetsPacket.Frame(
            100.5f, 64.25f, -30.75f,
            33f, -12f, 78f,
            0f, 0.7071f, 0f, 0.7071f);
        var targets = List.of(
            new TargetRecord(1.25, 2.5, -3.75, 7, 12.5f, TargetRecord.TYPE_PLAYER, "Steve"),
            new TargetRecord(10, 20, 30, -11, 4.5f, TargetRecord.TYPE_SABLE, "Sable Structure"));
        var pkt = new RadarTargetsPacket(new BlockPos(1, 2, 3), frame, targets,
            new int[]{7, -11}, new int[]{7});

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        RadarTargetsPacket.CODEC.encode(buf, pkt);
        var out = RadarTargetsPacket.CODEC.decode(buf);

        assertEquals(pkt.pos(), out.pos());
        assertEquals(frame, out.frame(), "reference frame must survive (attitude/center ride the light channel)");
        assertEquals(targets, out.targets(), "targets must round-trip field-for-field");
        assertArrayEquals(pkt.locked(), out.locked());
        assertArrayEquals(pkt.active(), out.active());
    }

    @Test
    @DisplayName("参考系 record 的 NaN 语义：不在 Sable 上（NaN）不构成变化")
    void frameNaNComparesEqual() {
        var offCraft1 = new RadarTargetsPacket.Frame(Float.NaN, Float.NaN, Float.NaN,
            Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        var offCraft2 = new RadarTargetsPacket.Frame(Float.NaN, Float.NaN, Float.NaN,
            Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        assertEquals(offCraft1, offCraft2,
            "record equality must treat NaN frames as equal — no per-tick change while off-craft");
        var onCraft = new RadarTargetsPacket.Frame(1, 2, 3, 4, 5, 6, 0, 0, 0, 1);
        assertNotEquals(offCraft1, onCraft);
    }
}
