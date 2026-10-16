package io.github.y15173334444.create_schematic_compute.client.audio;

import io.github.y15173334444.create_schematic_compute.graph.MusicTransport;
import io.github.y15173334444.create_schematic_compute.graph.NbsSong;
import io.github.y15173334444.create_schematic_compute.graph.NoteEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 起播落点时间轴测试（传输展开 → delaySeconds → 落点帧，全纯逻辑）：
 * <b>不变量 = 全曲共享一条时间轴</b>——首窗（delaySeconds &lt; 预播）与稳态（≥ 预播）两分支
 * 落下的音符，必须与各自的乐理时刻成<b>同一个</b>常数偏移。分支一旦错位，起播后一个预播窗的
 * 音乐会压在开头上（同一拍响两下 = 「开始播放时一段音频重音」）。
 * <p>Start-placement time-axis test (transport expansion → delaySeconds → placement frames, all
 * pure): <b>the invariant is one shared time axis</b> — notes placed by the on-time branch
 * (delaySeconds &lt; pre-roll) and the steady branch (≥ pre-roll) must share the same constant
 * offset from their musical times. Split the axis and the next window of music piles onto the
 * opening (two hits per beat — "a doubled opening at playback start").</p>
 */
class MusicPlacementTest {

    /** BE tick 同款步长；tempo=1000 → 10 NBS tick/s。 / the BE's step; tempo=1000 → 10 ticks/s. */
    private static final float DT = 0.05f;
    private static final float TPS = 10f;

    /** 每 NBS tick 一个音，key = tick（事件回读 key 即还原乐理时刻）。 */
    private static NbsSong denseSong(int ticks) {
        NbsSong s = new NbsSong();
        s.tempo = 1000;
        for (int t = 0; t < ticks; t++) s.putNote(t, 0, 2, t, 100, 100, 0);
        return s;
    }

    @Test
    @DisplayName("起播全曲单一时间轴：首窗与稳态批次同轴 / one time axis across the start window and steady batches")
    void startWindowSharesTimeAxisWithSteady() {
        NbsSong song = denseSong(80);
        MusicTransport tr = new MusicTransport();
        tr.play(0);
        double rate = AudioMixer.SAMPLE_RATE;

        List<double[]> landed = new ArrayList<>();   // {乐理秒, 落点帧}
        for (int step = 0; step < 60; step++) {
            // 到达墙钟 ≈ 下发墙钟（传输时延≈0）：第 step 批在 step*DT 秒到达
            long arrivalFrames = Math.round(step * DT * rate);
            List<NoteEvent> evs = tr.advance(song, DT, MusicTransport.PREROLL_SECONDS);
            for (NoteEvent e : evs) {
                long frame = arrivalFrames + AudioMixer.placementFrames(
                    0L, 0L, e.delaySeconds(), 0f);
                landed.add(new double[]{e.key() / TPS, frame});
            }
        }
        assertTrue(landed.size() >= 50, "应展开出整条时间轴，实际 " + landed.size());

        double c0 = landed.get(0)[1] - landed.get(0)[0] * rate;
        List<Double> deviants = new ArrayList<>();
        for (double[] l : landed) {
            double off = l[1] - l[0] * rate;
            if (Math.abs(off - c0) > 2.0) deviants.add(off);   // ±2 帧容差（双重取整 ~1 帧）
        }
        assertTrue(deviants.isEmpty(),
            "落点偏离统一时间轴（首窗/稳态分支错位）: 首个偏移=" + c0 / rate + "s, 偏移样本="
                + deviants.stream().limit(5).map(o -> (o / rate) + "s").toList()
                + "（共 " + deviants.size() + " 个音错位）");
    }
}
