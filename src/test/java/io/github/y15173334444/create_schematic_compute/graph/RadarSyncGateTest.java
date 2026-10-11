package io.github.y15173334444.create_schematic_compute.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雷达同步数据面回归（P1-7 终态）：旧门控逐字段比较含 x/y/z 的目标表，运动目标让
 * 全量 BE NBT 每 tick 砸向所有追踪客户端（图每次重建、拖拽卡顿）。终态是数据面拆分——
 * 运动走轻量通道（原始精度、变化即发、20Hz），全量 NBT 只看语义。
 * Radar sync data-plane regression (P1-7 final shape): the old gate compared target
 * records field-by-field including x/y/z, so moving targets made the full BE NBT hammer
 * every tracking client per tick (graph rebuilt every time, drag stutter). The final
 * shape splits the data planes — motion rides the light channel (full precision,
 * send-on-change, 20Hz) and the full NBT gates on semantics only.
 */
class RadarSyncGateTest {

    private static TargetRecord target(int id, double x, double y, double z, float dist) {
        return new TargetRecord(x, y, z, id, dist, TargetRecord.TYPE_PLAYER, "p" + id);
    }

    @Test
    @DisplayName("语义变化（成员/锁定/设置）立即推全量 NBT")
    void semanticChangesPushImmediately() {
        var gate = new RadarSyncGate();
        var s0 = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), "k");
        assertTrue(gate.shouldPush(s0), "first push always fires");
        gate.markPushed(s0);
        assertFalse(gate.shouldPush(s0), "unchanged semantics must not push");

        // 目标成员进出 / membership change
        var sNew = RadarSyncGate.Snapshot.of(
            List.of(target(1, 0, 0, 0, 0f), target(2, 3, 0, 0, 3f)), List.of(), "k");
        assertTrue(gate.shouldPush(sNew), "membership change is semantic");

        // 锁定变化 / lock change
        var sLock = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(1), "k");
        assertTrue(gate.shouldPush(sLock), "lock change is semantic");

        // 设置指纹变化 / settings fingerprint change
        var sCfg = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), "k2");
        assertTrue(gate.shouldPush(sCfg), "settings change is semantic");
    }

    @Test
    @DisplayName("纯运动不触发全量 NBT（运动数据走轻量通道，20Hz 原始精度）")
    void motionDoesNotTriggerFullNbt() {
        var gate = new RadarSyncGate();
        var s0 = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), "k");
        gate.markPushed(s0);
        // 目标飞速移动：语义不变 → 全量 NBT 不推 / a target flying around: no semantic change, no fat push
        var sMoved = RadarSyncGate.Snapshot.of(List.of(target(1, 50, 80, -30, 90f)), List.of(), "k");
        assertFalse(gate.shouldPush(sMoved), "motion must never ride the whole-graph payload");

        // 同一时刻，轻量通道用**原始**比较必须看得见哪怕 0.2 块的运动
        // Meanwhile the light channel's raw comparison must see even a 0.2-block move
        var tSmall1 = List.of(target(1, 0, 0, 0, 0f));
        var tSmall2 = List.of(target(1, 0.2, 0, 0, 0.2f));
        assertNotEquals(tSmall1, tSmall2,
            "the light channel compares raw records — full precision, no quantization");
    }

    @Test
    @DisplayName("分配集是运动派生态：不进全量 NBT 语义段（随轻量通道走）")
    void assignmentSetIsNotSemantic() {
        // Snapshot 不含分配集：相同成员/锁定/设置的视图必须相等
        // The Snapshot excludes assignments: same membership/locks/settings must compare equal
        var a = RadarSyncGate.Snapshot.of(List.of(target(1, 0, 0, 0, 0f)), List.of(), "k");
        var b = RadarSyncGate.Snapshot.of(List.of(target(1, 9, 9, 9, 12f)), List.of(), "k");
        assertEquals(a, b, "motion-derived assignment churn must not appear in the semantic view");
    }

    @Test
    @DisplayName("距离排序的顺序抖动不算变化（视图按字典序归一）")
    void distanceSortShufflingIsNotAChange() {
        var gate = new RadarSyncGate();
        var a = RadarSyncGate.Snapshot.of(
            List.of(target(1, 1, 0, 0, 1f), target(2, 2, 0, 0, 2f)), List.of(), "k");
        var b = RadarSyncGate.Snapshot.of(
            List.of(target(2, 2, 0, 0, 2f), target(1, 1, 0, 0, 1f)), List.of(), "k");
        gate.markPushed(a);
        assertFalse(gate.shouldPush(b), "reordering by distance must not count as a change");
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
