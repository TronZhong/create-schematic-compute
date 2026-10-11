package io.github.y15173334444.create_schematic_compute.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雷达目标同步门控回归（P1-7）：旧门控逐字段比较含 x/y/z 的目标表，运动目标让门控
 * 每 tick 全开 → 每 tick 全量 BE NBT 砸向所有追踪客户端（「图每次重建」、拖拽卡顿）。
 * 量化口径：纯运动要越过量化格/半径档才算变化且受节流；语义（成员/锁定/设置）立即推；
 * 距离排序的顺序抖动不算变化。
 * Radar target-sync gate regression (P1-7): the old gate compared target records
 * field-by-field including x/y/z, so moving targets held it open every tick and the full
 * BE NBT hammered every tracking client ("the graph rebuilds every time", drag stutter).
 * Quantized semantics: pure motion must cross a quantization cell / radius bucket to
 * count as changed (and is rate-limited); semantic changes (membership/locks/settings)
 * push immediately; distance-sort shuffling is not a change.
 */
class RadarSyncGateTest {

    private static TargetRecord target(int id, double x, double y, double z, float dist) {
        return new TargetRecord(x, y, z, id, dist, TargetRecord.TYPE_PLAYER, "p" + id);
    }

    @Test
    @DisplayName("量化格内运动不推；跨格受节流；语义变化立即推")
    void quantizedMotionThrottled_semanticsImmediate() {
        var gate = new RadarSyncGate();
        var t0 = List.of(target(1, 0, 0, 0, 0f));
        var s0 = RadarSyncGate.Snapshot.of(t0, List.of(), List.of(), "k");
        assertTrue(gate.shouldPush(s0, 0), "first push always fires");
        gate.markPushed(s0, 0);
        // 调用方契约：只有真推送才 markPushed——被跳过的推送不记账，节流时钟不被污染
        // Caller contract: markPushed only after a real push — skipped pushes are not
        // recorded, so the throttle clock stays clean

        // 量化格内抖动（<0.5 块）：不推 / sub-quantum jitter: no push
        var t1 = List.of(target(1, 0.2, 0, 0, 0.2f));
        var s1 = RadarSyncGate.Snapshot.of(t1, List.of(), List.of(), "k");
        assertFalse(gate.shouldPush(s1, 1), "sub-quantum motion must not push");

        // 跨量化格但节流间隔未到（上次推送在 tick 0）：不推
        // Crossed a cell inside the throttle window (last push at tick 0): no push
        var t2 = List.of(target(1, 5, 0, 0, 5f));
        var s2 = RadarSyncGate.Snapshot.of(t2, List.of(), List.of(), "k");
        assertFalse(gate.shouldPush(s2, 2), "motion-only change is throttled");

        // 节流间隔到：推 / throttle window elapsed: push
        assertTrue(gate.shouldPush(s2, 0 + RadarSyncGate.MOTION_MIN_INTERVAL_TICKS),
            "motion-only change pushes once the interval elapses");
        gate.markPushed(s2, RadarSyncGate.MOTION_MIN_INTERVAL_TICKS);

        // 语义变化（锁定）：无视节流立即推 / lock change: immediate regardless of throttle
        var s3 = RadarSyncGate.Snapshot.of(t2, List.of(1), List.of(), "k");
        assertTrue(gate.shouldPush(s3, RadarSyncGate.MOTION_MIN_INTERVAL_TICKS + 1),
            "semantic change must push immediately");
    }

    @Test
    @DisplayName("成员进出与设置变化都是语义变化，立即推")
    void membershipAndSettingsAreSemantic() {
        var gate = new RadarSyncGate();
        var s0 = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), List.of(), "k");
        gate.markPushed(s0, 0);

        // 进新目标 / new target appears
        var sNew = RadarSyncGate.Snapshot.of(
            List.of(target(1, 0, 0, 0, 0f), target(2, 3, 0, 0, 3f)), List.of(), List.of(), "k");
        assertTrue(gate.shouldPush(sNew, 1), "membership change is semantic");

        // 设置指纹变化 / settings fingerprint change
        var sCfg = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), List.of(), "k2");
        assertTrue(gate.shouldPush(sCfg, 1), "settings change is semantic");

        // 分配集变化 / assignment change
        var sAct = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), List.of(1), "k");
        assertTrue(gate.shouldPush(sAct, 1), "assignment change is semantic");
    }

    @Test
    @DisplayName("距离排序的顺序抖动不算变化（视图按 id 归一）")
    void distanceSortShufflingIsNotAChange() {
        var gate = new RadarSyncGate();
        // 两个目标换位（距离序颠倒），内容相同 / two targets swap order (distance order), same content
        var a = RadarSyncGate.Snapshot.of(
            List.of(target(1, 1, 0, 0, 1f), target(2, 2, 0, 0, 2f)), List.of(), List.of(), "k");
        var b = RadarSyncGate.Snapshot.of(
            List.of(target(2, 2, 0, 0, 2f), target(1, 1, 0, 0, 1f)), List.of(), List.of(), "k");
        gate.markPushed(a, 0);
        assertFalse(gate.shouldPush(b, 100), "reordering by distance must not count as a change");
    }

    @Test
    @DisplayName("半径档跨档算运动变化（半径档口径）")
    void distanceBucketCrossingCountsAsMotion() {
        var gate = new RadarSyncGate();
        var s0 = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 3.9f)), List.of(), List.of(), "k");
        gate.markPushed(s0, 0);
        // 位置不动、距离跨 2 块档 / same position, distance crosses a 2-block bucket
        var s1 = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 6.1f)), List.of(), List.of(), "k");
        assertTrue(gate.shouldPush(s1, 100), "crossing a radius bucket is a motion change");
    }

    @Test
    @DisplayName("Sable id 走结构身份、不走坐标：同身份移动 id 不变，恒为负")
    void sableIdIsIdentityStableNotPositionDerived() {
        // 同一身份，结构从 A 移到 B：id 必须一致（否则锁定被「不在范围」清理剪掉）
        // Same identity, structure moved from A to B: the id must hold (else the lock prune cuts it)
        var atA = TargetRecord.fromSableStructure(0, 0, 0, 10, 64, 10, 12345, "Sable Structure");
        var atB = TargetRecord.fromSableStructure(0, 0, 0, -30, 70, 90, 12345, "Sable Structure");
        assertEquals(atA.entityId(), atB.entityId(), "id must be movement-invariant");
        assertTrue(atA.entityId() < 0, "sable ids stay negative (clear of entity ids)");
        // 不同身份 → 不同 id / different identity → different id
        var other = TargetRecord.fromSableStructure(0, 0, 0, 10, 64, 10, 54321, "Sable Structure");
        assertNotEquals(atA.entityId(), other.entityId());
        // Math.abs 溢出防护：MIN_VALUE 身份也必须落在合法负 id / overflow guard
        assertTrue(TargetRecord.sableId(Integer.MIN_VALUE) < 0);
    }
}
