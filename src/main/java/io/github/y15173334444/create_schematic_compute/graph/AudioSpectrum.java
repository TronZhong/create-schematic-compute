package io.github.y15173334444.create_schematic_compute.graph;

import java.util.Arrays;

/**
 * 频谱取数（服务端，纯逻辑、可单测）：把 MUSIC 节点每刻展开的音符事件累加进 {@link #BARS} 根柱子。
 * <p>Spectrum accumulator (server-side, pure and unit-testable): folds the note events a MUSIC
 * node expands each tick into {@link #BARS} bars.</p>
 *
 * <p><b>为什么是「乐谱频谱」而不是采样 FFT</b>：音符事件在服务端就有全曲（MUSIC 节点是曲目宿主），
 * 而音频只在听得到音响的客户端渲染 —— 站在屏幕前但离音响远的玩家拿不到采样，屏幕会变成死的。
 * 服务端算、按刻同步，所有人看到同一份。</p>
 * <p><b>横轴口径</b>：柱子横轴是音符的<b>基频</b>（key 45 = F#4 = 369.994 Hz，与
 * {@link NoteEvent#pitchMultiplier()} 同式），16 根柱子等分 NBS 全音域（key 0…87 =
 * 27.5 Hz…4.19 kHz）—— 所以它是乐谱频谱，不是谐波 FFT：一个音符点亮它基频所在的那根柱子，
 * 不含乐器采样里的泛音。</p>
 * <p><b>预播提前量</b>：事件带 {@code delaySeconds}（目标时刻最多在 2 s 之后），所以音符按
 * <b>目标刻</b>排进环形缓冲、到点才点亮 —— 直接按「展开刻」累加会让柱子比声音早最多 2 秒。
 * 停止/跳转沿（{@code stopSignal}）清空排程与电平。</p>
 * <p><b>Why a score spectrum, not a sampled FFT</b>: the note events exist server-side for the
 * whole song (the MUSIC node hosts it), while audio is only rendered on clients that can hear a
 * speaker — a player standing at the screen but far from the speakers has no samples and the
 * display would sit dead. Computed on the server and synced per tick, everyone sees the same
 * bars. The horizontal axis is the note's <b>fundamental</b> (key 45 = F#4 = 369.994 Hz, the same
 * formula as {@link NoteEvent#pitchMultiplier()}); 16 bars split the NBS key range
 * (0…87 = 27.5 Hz…4.19 kHz) evenly — a score spectrum, not a harmonics FFT: one note lights the
 * bar of its fundamental, without the instrument sample's overtones.</p>
 * <p><b>Pre-roll</b>: events carry {@code delaySeconds} (their target sits up to 2 s in the
 * future), so notes are scheduled at their <b>target tick</b> in a ring buffer and only light up
 * when that tick arrives — accumulating at the expansion tick would run the bars up to 2 s ahead
 * of the sound. A stop/seek edge ({@code stopSignal}) clears the schedule and the levels.</p>
 */
public final class AudioSpectrum {

    /** 柱子数。 / bar count. */
    public static final int BARS = 16;
    /** 每刻电平回落系数（0.82 ≈ −1.7 dB/刻；20 刻后剩 2%）。 / per-tick decay */
    private static final float DECAY = 0.82f;
    /** 低于此值直接归零（省掉无尽的指数尾巴）。 / snap-to-zero floor */
    private static final float FLOOR = 0.004f;
    /** 排程环容量（刻）：≥ 预播窗 2 s = 40 刻，留一倍余量。 / ring size in ticks */
    private static final int RING = 64;
    /** NBS 音域两端（key 0 / key 87）——柱子等分这条区间。 / NBS key range endpoints. */
    private static final double KEY_LO = 0.0, KEY_HI = 87.0;

    /** 柱子上界频率（BARS+1 条边界）。 / bar edge frequencies. */
    private static final float[] EDGE = new float[BARS + 1];
    static {
        for (int i = 0; i <= BARS; i++)
            EDGE[i] = frequencyHz(KEY_LO + (KEY_HI - KEY_LO) * i / BARS, 0.0);
    }

    private final float[] level = new float[BARS];
    private final float[][] ring = new float[RING][BARS];
    private long lastApplied = Long.MIN_VALUE;

    /** key（+音分）→ 频率（Hz）：key 45 = F#4 = 369.994 Hz，与 {@link NoteEvent#pitchMultiplier()}
     *  同式（该处给倍率、这里给绝对频率）。
     *  key (+cents) → frequency in Hz; key 45 = F#4 = 369.994 Hz, the same formula as
     *  {@link NoteEvent#pitchMultiplier()} (multiplier there, absolute frequency here). */
    public static float frequencyHz(double key, double cents) {
        return (float) (369.994 * Math.pow(2.0, (key - 45.0) / 12.0 + cents / 1200.0));
    }

    /** 频率 → 柱子下标（对数等分 NBS 音域；越界钳到两端）。 / frequency → bar index, clamped. */
    public static int barFor(float hz) {
        if (!(hz > EDGE[0])) return 0;                     // 含 NaN
        for (int i = 0; i < BARS; i++) if (hz < EDGE[i + 1]) return i;
        return BARS - 1;
    }

    /** 把一个音符排到它的<b>目标刻</b>（换算在调用方：目标刻 = 本刻 + delaySeconds/0.05）。
     *  力度 0 无声；同刻同柱取最大值（不做叠加饱和）。
     *  Schedule one note at its <b>target tick</b> (the caller converts delaySeconds/0.05);
     *  zero velocity is silent and same-tick same-bar hits take the max. */
    public void schedule(long targetTick, int key, int cents, int velocity, float gain) {
        float v = velocity / 100f * gain;
        if (v <= 0f) return;
        if (v > 1f) v = 1f;
        int bar = barFor(frequencyHz(key, cents));
        float[] slot = ring[(int) Math.floorMod(targetTick, RING)];
        if (slot[bar] < v) slot[bar] = v;
    }

    /** 停止/跳转沿：清空排程与电平（音乐停了柱子不该继续抖）。 / stop/seek edge: clear all. */
    public void clear() {
        for (float[] slot : ring) Arrays.fill(slot, 0f);
        Arrays.fill(level, 0f);
        lastApplied = Long.MIN_VALUE;
    }

    /** 推进到 {@code nowTick}：先回落、再点亮本刻（含停顿期间跳过的刻）排下的音符。
     *  <b>次序要紧</b>：先点亮后回落会让满力度音符只显示到 82%（起音不实），所以命中值在本刻
     *  按满值显示、从下一刻才开始衰减。
     *  Advance to {@code nowTick}: decay first, then light this tick's scheduled notes (including
     *  ticks skipped during a server stall). <b>Order matters</b>: lighting before decaying leaves a
     *  full-velocity hit showing at only 82% — hits display at full value this tick and start
     *  decaying on the next. */
    public void advance(long nowTick) {
        for (int i = 0; i < BARS; i++) {
            float l = level[i] * DECAY;
            level[i] = l < FLOOR ? 0f : l;
        }
        if (lastApplied == Long.MIN_VALUE) lastApplied = nowTick - 1;
        long from = Math.max(lastApplied + 1, nowTick - RING + 1);
        for (long t = from; t <= nowTick; t++) {
            float[] slot = ring[(int) Math.floorMod(t, RING)];
            for (int i = 0; i < BARS; i++) {
                if (slot[i] > 0f) {
                    if (slot[i] > level[i]) level[i] = slot[i];
                    slot[i] = 0f;
                }
            }
        }
        lastApplied = nowTick;
    }

    /** 某根柱子的当前电平（0..1）。 / current level of one bar. */
    public float level(int bar) {
        return level[bar];
    }

    /** 打包成字节（0..255）写入 {@code out}；返回是否与上次不同（同步去重用）。
     *  Pack the levels as bytes into {@code out}; true when they differ from the previous pack. */
    public boolean writeBytes(byte[] out) {
        boolean changed = false;
        for (int i = 0; i < BARS; i++) {
            byte b = (byte) Math.round(level[i] * 255f);
            if (out[i] != b) {
                out[i] = b;
                changed = true;
            }
        }
        return changed;
    }
}
