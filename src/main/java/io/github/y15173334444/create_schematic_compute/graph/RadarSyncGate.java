package io.github.y15173334444.create_schematic_compute.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 雷达 BE 目标同步的**量化门控**（tech-debt P1-7）。
 * <p>
 * 旧门控是 {@code targets.equals(lastPushedTargets)}：{@link TargetRecord} 含 x/y/z
 * 逐字段比较，目标几乎每 tick 都在动 → 门控形同虚设 → 每 tick {@code sendBlockUpdated}
 * 全量 BE NBT（整图+设置+目标表）砸向所有追踪客户端。本类把比较口径换成「同步视图」：
 * <ul>
 *   <li><b>语义段</b>（目标身份 id/类型/名称 + 锁定集 + 分配集 + 设置指纹）：变化立即推送
 *       ——锁定点击、目标进出、设置调整不再被运动噪声淹没或吞掉；</li>
 *   <li><b>运动段</b>（位置量化 {@link #POS_QUANTUM} + 距离半径档 {@link #DIST_BUCKET}）：
 *       只有越过量化格/档位才算变化，且两次推送至少隔 {@link #MOTION_MIN_INTERVAL_TICKS} tick。</li>
 * </ul>
 * 目标列表按距离排序——排序抖动不是语义变化，视图一律按 id/字典序归一后再比较。
 * <p>
 * Quantized gate for the radar BE's target sync (tech-debt P1-7). The old gate compared
 * full TargetRecords including x/y/z, so moving targets kept it open forever and every
 * tick shipped the full BE NBT to all tracking clients. The comparison now rides a
 * "sync view": a semantic section (identity/type/name + lock set + assignment set +
 * settings fingerprint) that pushes immediately when changed, and a motion section
 * (positions quantized to {@link #POS_QUANTUM}, distances bucketed to
 * {@link #DIST_BUCKET}) that counts as changed only across a cell/bucket boundary and
 * is rate-limited to one push per {@link #MOTION_MIN_INTERVAL_TICKS} ticks.
 */
public final class RadarSyncGate {

    /** 位置量化格（块）：blip 精度步进 / position quantum in blocks — blip precision step */
    public static final double POS_QUANTUM = 0.5;
    /** 距离半径档宽（块）/ radius bucket width in blocks */
    public static final double DIST_BUCKET = 2.0;
    /** 纯运动变化的最小推送间隔（tick）/ minimum interval between motion-only pushes (ticks) */
    public static final int MOTION_MIN_INTERVAL_TICKS = 5;

    /** 量化取整：v 落入哪个格/档 / quantize: which cell/bucket v falls into */
    public static long quantize(double v, double quantum) {
        return Math.round(v / quantum);
    }

    /** 同步视图：语义段 + 运动段（record equals 即逐段比较）。
     *  Sync view: semantic + motion sections (record equality compares both). */
    public record Snapshot(List<String> semantic, List<Long> motion) {

        /** 构建当前同步视图。目标段一律归一排序（距离排序的顺序抖动不算变化）。
         *  Build the current sync view; target entries are normalized to a stable order
         *  (shuffling from the distance sort is not a change). */
        public static Snapshot of(List<TargetRecord> targets, Collection<Integer> locked,
                                  Collection<Integer> active, String settingsKey) {
            List<String> sem = new ArrayList<>();
            List<Long> mot = new ArrayList<>();
            // 目标段按 (id, type, name) 字典序归一；运动段按 id 序拼接
            // Target entries normalized by (id, type, name); motion entries flattened in id order
            List<TargetRecord> byId = new ArrayList<>(targets);
            byId.sort((a, b) -> Integer.compare(a.entityId(), b.entityId()));
            for (var t : byId) {
                sem.add(t.entityId() + "|" + t.entityType() + "|" + t.name());
                mot.add(quantize(t.x(), POS_QUANTUM));
                mot.add(quantize(t.y(), POS_QUANTUM));
                mot.add(quantize(t.z(), POS_QUANTUM));
                mot.add(quantize(t.distance(), DIST_BUCKET));
            }
            sem.add("locked=" + sorted(locked));
            sem.add("active=" + sorted(active));
            sem.add("settings=" + settingsKey);
            return new Snapshot(sem, mot);
        }

        private static List<Integer> sorted(Collection<Integer> ids) {
            List<Integer> out = new ArrayList<>(ids);
            Collections.sort(out);
            return out;
        }
    }

    /** 上次推送的视图与 tick；null = 从未推送 */
    private Snapshot lastPushed;
    private long lastPushTick = Long.MIN_VALUE / 2;

    /** 现在是否该推送。调用方推送成功后必须 {@link #markPushed}。
     *  Whether to push now; the caller must {@link #markPushed} after pushing. */
    public boolean shouldPush(Snapshot now, long tick) {
        if (lastPushed == null) return true;
        // 语义变化（成员/名称/锁定/分配/设置）：立即推送，不受节流
        // Semantic change (membership / names / locks / assignments / settings): immediate
        if (!now.semantic().equals(lastPushed.semantic())) return true;
        // 纯运动变化：量化视图变了才推，且受最小间隔约束
        // Motion only: push when the quantized view moved across a cell/bucket, rate-limited
        return !now.motion().equals(lastPushed.motion())
            && tick - lastPushTick >= MOTION_MIN_INTERVAL_TICKS;
    }

    /** 记录一次已发生的推送 / record a push that just happened */
    public void markPushed(Snapshot now, long tick) {
        lastPushed = now;
        lastPushTick = tick;
    }
}
