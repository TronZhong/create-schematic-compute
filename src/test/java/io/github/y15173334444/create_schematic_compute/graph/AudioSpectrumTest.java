package io.github.y15173334444.create_schematic_compute.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AudioSpectrum 纯逻辑测试：分桶单调性、<b>预播目标刻契约</b>（这是全条链最容易错的地方——
 * 按展开刻点亮会让柱子比声音早最多 2 秒）、起落包络、停止清空、字节打包去重。
 * AudioSpectrum tests: bucket monotonicity, the <b>target-tick contract</b> for the pre-roll
 * (the easiest thing to get wrong — lighting at the expansion tick runs the bars up to 2 s
 * ahead of the sound), attack/decay, stop clearing and byte packing.
 */
class AudioSpectrumTest {

    @Test
    @DisplayName("分桶单调且覆盖 NBS 全音域 / bucketing is monotonic and spans the NBS key range")
    void bucketsAreMonotonic() {
        assertEquals(0, AudioSpectrum.barFor(AudioSpectrum.frequencyHz(0, 0)), "key 0 → 最低柱");
        assertEquals(AudioSpectrum.BARS - 1, AudioSpectrum.barFor(AudioSpectrum.frequencyHz(87, 0)), "key 87 → 最高柱");
        int prev = -1;
        for (int key = 0; key <= 87; key++) {
            int bar = AudioSpectrum.barFor(AudioSpectrum.frequencyHz(key, 0));
            assertTrue(bar >= prev, "键升高柱子不得回落：key=" + key);
            assertTrue(bar >= 0 && bar < AudioSpectrum.BARS, "越界：key=" + key + " bar=" + bar);
            prev = bar;
        }
        // 越界钳制与 NaN 安全 / out-of-range clamps and NaN safety
        assertEquals(0, AudioSpectrum.barFor(1f));
        assertEquals(AudioSpectrum.BARS - 1, AudioSpectrum.barFor(20000f));
        assertEquals(0, AudioSpectrum.barFor(Float.NaN));
    }

    @Test
    @DisplayName("音高倍率与绝对频率同式（key 45 = F#4 = 369.994 Hz）")
    void frequencyMatchesPitchMultiplier() {
        assertEquals(369.994f, AudioSpectrum.frequencyHz(45, 0), 0.01f);
        // NoteEvent 的倍率 × 基准频率 = 本类的绝对频率（同一条公式的两种写法）
        // NoteEvent's multiplier times the base frequency equals this class's absolute frequency.
        var e = new NoteEvent(0, 57, 100, 0, 0, 1f);
        assertEquals(369.994f * e.pitchMultiplier(), AudioSpectrum.frequencyHz(57, 0), 0.05f);
        // 一个八度 = 频率翻倍 / one octave doubles the frequency
        assertEquals(2f * AudioSpectrum.frequencyHz(45, 0), AudioSpectrum.frequencyHz(57, 0), 0.05f);
    }

    @Test
    @DisplayName("预播契约：排在未来刻的音符，到点才点亮 / a note scheduled ahead lights only at its target tick")
    void scheduledNoteLightsAtTargetTickOnly() {
        AudioSpectrum s = new AudioSpectrum();
        int bar = AudioSpectrum.barFor(AudioSpectrum.frequencyHz(57, 0));
        long now = 1000L;
        s.schedule(now + 40, 57, 0, 100, 1f);      // 2 s 预播：目标在 40 刻之后
        for (long t = now; t < now + 40; t++) {
            s.advance(t);
            assertEquals(0f, s.level(bar), "第 " + t + " 刻不该提前点亮（预播未到目标时刻）");
        }
        s.advance(now + 40);
        assertTrue(s.level(bar) > 0.9f, "到目标刻应点亮，实际 " + s.level(bar));
    }

    @Test
    @DisplayName("起落包络：点亮即时、随后单调回落并归零 / instant attack, monotonic decay to zero")
    void attackThenDecay() {
        AudioSpectrum s = new AudioSpectrum();
        int bar = AudioSpectrum.barFor(AudioSpectrum.frequencyHz(45, 0));
        s.schedule(5, 45, 0, 100, 1f);
        s.advance(5);
        float peak = s.level(bar);
        assertTrue(peak > 0.9f, "起音即时，实际 " + peak);
        float prev = peak;
        for (int t = 6; t < 200; t++) {
            s.advance(t);
            assertTrue(s.level(bar) <= prev + 1e-6f, "回落不得反弹：tick=" + t);
            prev = s.level(bar);
        }
        assertEquals(0f, s.level(bar), "最终应归零");
    }

    @Test
    @DisplayName("停顿跳刻不丢音符 / notes survive a skipped tick range (server stall)")
    void skippedTicksStillLight() {
        AudioSpectrum s = new AudioSpectrum();
        int bar = AudioSpectrum.barFor(AudioSpectrum.frequencyHz(60, 0));
        s.schedule(1008, 60, 0, 80, 1f);
        s.advance(1000);
        s.advance(1010);                            // 一跳跨过目标刻
        assertTrue(s.level(bar) > 0.7f, "跨刻推进也要点亮，实际 " + s.level(bar));
    }

    @Test
    @DisplayName("停止沿清空排程与电平 / stop clears both the schedule and the levels")
    void clearResetsEverything() {
        AudioSpectrum s = new AudioSpectrum();
        s.schedule(10, 45, 0, 100, 1f);
        s.schedule(30, 60, 0, 100, 1f);
        s.advance(10);
        assertTrue(s.level(AudioSpectrum.barFor(AudioSpectrum.frequencyHz(45, 0))) > 0f);
        s.clear();
        for (int i = 0; i < AudioSpectrum.BARS; i++) assertEquals(0f, s.level(i));
        s.advance(30);
        for (int i = 0; i < AudioSpectrum.BARS; i++) assertEquals(0f, s.level(i), "排程也应被清空");
    }

    @Test
    @DisplayName("字节打包：变化才报表、静默后稳定 / packing reports change then settles")
    void bytePacking() {
        AudioSpectrum s = new AudioSpectrum();
        byte[] out = new byte[AudioSpectrum.BARS];
        s.advance(0);
        assertFalse(s.writeBytes(out), "静默时不该报变化");
        s.schedule(1, 45, 0, 100, 1f);
        s.advance(1);
        assertTrue(s.writeBytes(out), "点亮应报变化");
        assertEquals((byte) Math.round(s.level(AudioSpectrum.barFor(AudioSpectrum.frequencyHz(45, 0))) * 255f),
            out[AudioSpectrum.barFor(AudioSpectrum.frequencyHz(45, 0))]);
        assertFalse(s.writeBytes(out), "同一刻重复打包不该再报变化");
    }
}
