package io.github.y15173334444.create_schematic_compute.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 雷达 BE 全量 NBT 推送的**语义门控**（tech-debt P1-7，终态）。
 * <p>
 * 旧门控是 {@code targets.equals(lastPushedTargets)}：{@link TargetRecord} 含 x/y/z
 * 逐字段比较，目标几乎每 tick 都在动 → 门控形同虚设 → 每 tick {@code sendBlockUpdated}
 * 全量 BE NBT（整图+设置+目标表）砸向所有追踪客户端。修复经历两个阶段：
 * <ul>
 *   <li>量化节流（0.5 块量化格 + 2 块半径档 + 5 tick 节流）止了血，但 blip 位置掉到
 *       4Hz/0.5 块步进；</li>
 *   <li>终态是**数据面拆分**：目标/锁定/分配的运动数据改走轻量通道
 *       （{@code RadarTargetsPacket}，原始精度、变化即发、20Hz），全量 NBT 只剩
 *       语义内容需要推送——本类只比较语义段（成员/名称/锁定/设置指纹），运动不再
 *       触发整图推送。分配集是运动派生态，随轻量通道走，不进语义段。</li>
 * </ul>
 * 目标列表按距离排序——排序抖动不是语义变化，视图按 id/字典序归一后再比较。
 * <p>
 * Semantic gate for the radar BE's full-NBT pushes (tech-debt P1-7, final shape). The old
 * gate compared full TargetRecords including x/y/z, so moving targets held it open every
 * tick and the full BE NBT hammered every tracking client. The fix went through two
 * stages: quantized throttling staunched the bleed but dropped blips to 4Hz/0.5-block
 * steps; the final shape is a **data-plane split** — target/lock/assignment motion rides
 * the light channel ({@code RadarTargetsPacket}, full precision, send-on-change, 20Hz)
 * and the full NBT only carries semantic content, so this class compares the semantic
 * section alone (membership/names/locks/settings fingerprint). The assignment set is
 * motion-derived and travels on the light channel, not here.
 */
public final class RadarSyncGate {

    /** 同步视图 = 语义段（record equals 即比较）/ sync view = the semantic section */
    public record Snapshot(List<String> semantic) {

        /** 构建当前语义视图。目标段一律归一排序（距离排序的顺序抖动不算变化）。
         *  Build the current semantic view; target entries are normalized to a stable
         *  order (shuffling from the distance sort is not a change). */
        public static Snapshot of(List<TargetRecord> targets, Collection<Integer> locked,
                                  String settingsKey) {
            List<String> sem = new ArrayList<>();
            for (var t : targets) {
                sem.add(t.entityId() + "|" + t.entityType() + "|" + t.name());
            }
            Collections.sort(sem);
            List<Integer> lockOrder = new ArrayList<>(locked);
            Collections.sort(lockOrder);
            sem.add("locked=" + lockOrder);
            sem.add("settings=" + settingsKey);
            return new Snapshot(sem);
        }
    }

    /** 上次推送的视图；null = 从未推送 / the last pushed view; null = never pushed */
    private Snapshot lastPushed;

    /** 现在是否该推送全量 NBT。调用方推送成功后必须 {@link #markPushed}。
     *  Whether to push the full NBT now; the caller must {@link #markPushed} after. */
    public boolean shouldPush(Snapshot now) {
        return lastPushed == null || !now.semantic().equals(lastPushed.semantic());
    }

    /** 记录一次已发生的推送 / record a push that just happened */
    public void markPushed(Snapshot now) {
        lastPushed = now;
    }
}
