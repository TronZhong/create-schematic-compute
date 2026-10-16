package io.github.y15173334444.create_schematic_compute.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.y15173334444.create_schematic_compute.blocks.AmplifierComputerBlock;
import io.github.y15173334444.create_schematic_compute.blocks.AmplifierComputerBlockEntity;
import io.github.y15173334444.create_schematic_compute.blocks.AmplifierComputerStates;
import io.github.y15173334444.create_schematic_compute.graph.AudioSpectrum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * 功放电脑屏幕渲染器：在盖子内侧的屏幕面上画 {@link AudioSpectrum#BARS} 根频谱柱，
 * 柱顶垫一颗<b>峰顶指示器</b>（被柱子顶起、松开后慢落）——取代旧的槽底托底（与柱面同深、
 * z-fighting）。
 * <p><b>只在开盖（{@code lit=true}）时画</b>：合盖时屏幕面朝下压在键盘上，画了也看不见；
 * 电平数据由服务端 20 Hz 下发（{@link io.github.y15173334444.create_schematic_compute.network.SpectrumSyncPacket}），
 * 这里只按 {@code partialTick} 在上一包/本包之间插值。</p>
 * <p><b>屏幕面几何</b>：锚点/法线/上向全部来自 {@link AmplifierComputerStates#PANEL_OPEN}
 * （与模型 PM 件共面），朝向按 blockstate 的 {@code facing} 复刻（顺时针对应 JOML 负角，
 * 与动力仪表/雷达同款）。绘制用的 RenderType 与全息显示器同一条（{@code SCREEN_PIXEL}：
 * POSITION_COLOR、不采样光照/贴图 → 自发光），因此颜色直接写在顶点上，不需要贴图。</p>
 * <p>Amplifier screen renderer: draws the {@link AudioSpectrum#BARS} spectrum bars on the lid's
 * inner screen face, each topped by a <b>peak indicator</b> (pushed up by the bar, sinking after
 * it lets go) — replacing the old baseline stubs (same-plane z-fighting with the bars).
 * <b>Open lid only</b> (closed, the screen faces down onto the keyboard and
 * nothing would be visible). Levels arrive from the server at 20 Hz and are interpolated between
 * the previous/current packet by {@code partialTick}. Geometry (centre/normal/up) comes from
 * {@link AmplifierComputerStates#PANEL_OPEN}, coplanar with the model's {@code PM} element; the
 * blockstate {@code facing} is replayed on the pose stack (vanilla clockwise = negative JOML,
 * same as the kinetic gauge and the radar). The render type is the holographic monitor's
 * {@code SCREEN_PIXEL} (POSITION_COLOR, no lightmap/texture → self-illuminated), so the bar
 * colours are plain vertex colours and no texture is involved.</p>
 */
public class AmplifierComputerRenderer implements BlockEntityRenderer<AmplifierComputerBlockEntity> {

    /** 屏幕内缩边距（模型单位）：柱子不贴到屏幕边框。 / inset so bars miss the bezel */
    private static final float MARGIN_X = 1.1f, MARGIN_Y = 1.1f;
    /** 柱子占槽宽的比例（其余是缝）。 / bar width fraction of its slot */
    private static final float BAR_FILL = 0.62f;
    /** 沿法线朝观察者抬起的量（模型单位）：压住屏幕面防 z-fighting。 / anti z-fight lift */
    private static final float Z_OUT = -0.02f;
    /** 峰顶指示器：再抬出一档压住柱面（同面 z-fighting 的根治）与厚度。
     *  Peak indicator: lifted one more step past the bars (kills same-plane z-fighting)
     *  and its thickness. */
    private static final float Z_PEAK = Z_OUT - 0.02f;
    private static final float PEAK_H = 0.45f;
    /** 峰顶指示器颜色（软白，压在电平渐变上不抢色）。 / peak indicator colour (soft white). */
    private static final int COL_PEAK = 0xEAF2EE;
    /** 电平渐变：低 → 中 → 高。 / level gradient */
    private static final int COL_LOW = 0x3FE0C0, COL_MID = 0xFFC24B, COL_HIGH = 0xFF5A4B;

    public AmplifierComputerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(AmplifierComputerBlockEntity be, float partialTick, PoseStack ps,
                       MultiBufferSource buffer, int light, int overlay) {
        if (be.getLevel() == null) return;
        BlockState st = be.getBlockState();
        if (!st.hasProperty(AmplifierComputerBlock.LIT) || !st.getValue(AmplifierComputerBlock.LIT))
            return;                                   // 合盖不画 / closed lid draws nothing

        // 复刻 blockstate 的变体 y 旋转：**必须用 modelYaw（北=0）**，不能用 Direction#toYRot()
        // 或 get2DDataValue()（两者都是「南=0」的实体约定，北向会得到 180°，即机器另一侧）。
        // Replay the blockstate's variant y: use modelYaw (north = 0), never Direction#toYRot() /
        // get2DDataValue() — both are the entity convention ("south = 0"), giving 180° for north.
        float yRot = AmplifierComputerStates.modelYaw(st.getValue(AmplifierComputerBlock.FACING));
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-yRot));   // 复刻 blockstate y 旋转 / replay facing
        ps.scale(1f / 16f, 1f / 16f, 1f / 16f);       // 进模型单位 / enter model units
        Matrix4f pose = ps.last().pose();
        Matrix4f panelMat = new Matrix4f(pose)
            .mul(AmplifierComputerStates.panelBasis(AmplifierComputerStates.PANEL_OPEN));
        drawBars(be, buffer, panelMat, partialTick);
        ps.popPose();
    }

    private void drawBars(AmplifierComputerBlockEntity be, MultiBufferSource buffer,
                          Matrix4f panelMat, float partialTick) {
        var panel = AmplifierComputerStates.PANEL_OPEN;
        float halfW = panel.halfRight() - MARGIN_X;
        float halfH = panel.halfUp() - MARGIN_Y;
        int n = AudioSpectrum.BARS;
        float slot = (halfW * 2f) / n;
        float barW = slot * BAR_FILL;
        VertexConsumer vc = buffer.getBuffer(MonitorRenderTypes.SCREEN_PIXEL);
        be.tickSpectrumPeaks(System.nanoTime(), partialTick);
        for (int i = 0; i < n; i++) {
            float x0 = -halfW + slot * i + (slot - barW) * 0.5f;
            float x1 = x0 + barW;
            float level = be.spectrumLevel(i, partialTick);
            float h = level * (halfH * 2f);
            if (h > 0.06f) quad(vc, panelMat, x0, x1, -halfH, -halfH + h, Z_OUT, levelColor(level), 1f);
            // 峰顶指示器：垫在柱顶上（被柱子顶起）、柱子回落后慢慢沉下来；**沉底不消失**——
            // 静音时 16 颗停在槽底当槽位标记。
            // Peak indicator: rests on the bar top (pushed up by it), sinking after the bar falls;
            // **never hidden** — in silence all sixteen rest on the floor as slot markers.
            float peak = be.spectrumPeak(i);
            float yB = -halfH + peak * (halfH * 2f);
            if (yB + PEAK_H > halfH) yB = halfH - PEAK_H;
            quad(vc, panelMat, x0, x1, yB, yB + PEAK_H, Z_PEAK, COL_PEAK, 1f);
        }
    }

    /** 面板平面四边形（坐标为「上为正」模型单位；zOut 负值朝观察者凸出；
     *  {@code SCREEN_PIXEL} 无背面剔除，绕序无关）——与动力仪表同款。
     *  Panel-plane quad (up-positive model units; negative zOut protrudes toward the viewer;
     *  SCREEN_PIXEL has no culling so winding is irrelevant) — same as the kinetic gauge. */
    private static void quad(VertexConsumer vc, Matrix4f panelMat,
                             float x0, float x1, float yBottom, float yTop, float zOut,
                             int argb, float alpha) {
        float r = ((argb >> 16) & 0xFF) / 255f, g = ((argb >> 8) & 0xFF) / 255f, b = (argb & 0xFF) / 255f;
        // 面板矩阵的 y 轴是「向下」→ 取 −y 把「上为正」翻过去。
        vc.addVertex(panelMat, x0, -yTop, zOut).setColor(r, g, b, alpha);
        vc.addVertex(panelMat, x1, -yTop, zOut).setColor(r, g, b, alpha);
        vc.addVertex(panelMat, x1, -yBottom, zOut).setColor(r, g, b, alpha);
        vc.addVertex(panelMat, x0, -yBottom, zOut).setColor(r, g, b, alpha);
    }

    /** 电平配色：0.6 以下是「低→中」渐变，以上「中→高」。 / level colour gradient. */
    private static int levelColor(float level) {
        if (level < 0.6f) return lerpColor(COL_LOW, COL_MID, level / 0.6f);
        return lerpColor(COL_MID, COL_HIGH, (level - 0.6f) / 0.4f);
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
