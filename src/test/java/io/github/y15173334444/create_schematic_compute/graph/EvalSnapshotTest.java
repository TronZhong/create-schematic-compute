package io.github.y15173334444.create_schematic_compute.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EvalSnapshot 内容比较测试：「变化才发」门控的判据必须是<b>逐元素</b>深度比较——
 * record 的 equals 对 float[] 是引用相等，拿它当门控每刻都会判「变了」（门控失效 =
 * 暂停时总线数据面反复刷新的来源）。
 * EvalSnapshot content comparison: the send-on-change gate needs <b>element-wise</b> deep
 * comparison — the record's equals compares float[]s by reference and would read as "changed"
 * every tick (a defeated gate is exactly what kept the bus data plane "refreshing" while
 * playback was paused).
 */
class EvalSnapshotTest {

    private static EvalSnapshot snap(Map<Integer, float[]> outputs,
                                     Map<Integer, Float> debugTimes,
                                     Map<Integer, Map<Integer, float[]>> subOutputs,
                                     Map<Integer, Map<Integer, Float>> subDebugTimes,
                                     Map<Integer, Float> spreads) {
        return EvalSnapshot.capture(outputs, debugTimes, subOutputs, subDebugTimes, spreads);
    }

    private static Map<Integer, float[]> outs(float... perNode) {
        var m = new HashMap<Integer, float[]>();
        for (int i = 0; i < perNode.length; i++) m.put(i, new float[]{perNode[i]});
        return m;
    }

    @Test
    @DisplayName("逐元素相等才算同内容（record equals 对 float[] 失效）/ element-wise equality; record equals is broken for float[]")
    void identicalContentIsSame() {
        var a = snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(), Map.of(), Map.of(3, 1f));
        var b = snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(), Map.of(), Map.of(3, 1f));
        assertTrue(a.sameContent(b), "逐元素相同必须判同内容");
        // 回归锚：拿 record equals 当门控会每刻判「变了」（数组按引用比）。
        // Regression anchor: the record's own equals reads as "changed" (arrays compare by identity).
        assertNotEquals(a, b, "record equals 对不同数组实例判不等——门控不能用它");
    }

    @Test
    @DisplayName("任一映射变化即判变化 / any map difference counts as changed")
    void differencesAreDetected() {
        var base = snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(1, outs(3f)), Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f));
        assertTrue(base.sameContent(base), "自身恒等");
        assertFalse(base.sameContent(null), "null = 未发过，判变化");

        assertFalse(base.sameContent(snap(outs(1f, 2.5f), Map.of(7, 0.5f), Map.of(1, outs(3f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f))), "outputs 元素差异");
        assertFalse(base.sameContent(snap(outs(1f), Map.of(7, 0.5f), Map.of(1, outs(3f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f))), "outputs 条目数差异");
        assertFalse(base.sameContent(snap(outs(1f, 2f), Map.of(7, 0.75f), Map.of(1, outs(3f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f))), "debugTimes 差异");
        assertFalse(base.sameContent(snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(1, outs(4f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f))), "subOutputs 内层差异");
        assertFalse(base.sameContent(snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(2, outs(3f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 1f))), "subOutputs 键差异");
        assertFalse(base.sameContent(snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(1, outs(3f)),
            Map.of(1, Map.of(2, 0.5f)), Map.of(3, 1f))), "subDebugTimes 差异");
        assertFalse(base.sameContent(snap(outs(1f, 2f), Map.of(7, 0.5f), Map.of(1, outs(3f)),
            Map.of(1, Map.of(2, 0.25f)), Map.of(3, 0f))), "formulaSpreads 差异");
    }

    @Test
    @DisplayName("capture 防御性拷贝：底稿变异不影响基线 / capture copies defensively, so a mutating source never flips the gate")
    void captureIsDefensive() {
        var mutable = outs(1f);
        var a = snap(mutable, Map.of(), Map.of(), Map.of(), Map.of());
        mutable.get(0)[0] = 9f;                       // 底稿随后被求值器复用改写
        var b = snap(outs(1f), Map.of(), Map.of(), Map.of(), Map.of());
        assertTrue(a.sameContent(b), "capture 拷贝后底稿变异不得判变化");
    }
}
