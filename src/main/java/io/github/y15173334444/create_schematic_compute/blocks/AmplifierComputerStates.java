package io.github.y15173334444.create_schematic_compute.blocks;

import net.minecraft.core.Direction;
import org.joml.Matrix4f;

/**
 * 功放电脑的屏幕几何（纯数据 + 纯函数，可单测）：把模型里 {@code PM} 件（盖子）的屏幕面姿态
 * 翻成渲染用的<b>面板基</b>——中心 / 法线 / 右 / 上 / 半宽高，全部以<b>模型单位</b>（16 单位 = 1 格）给出。
 * <p>纯类：只依赖 {@link Direction} 与 JOML，不引用任何客户端类型，因此服务端路径与单元测试都能用（与
 * {@link KineticGaugeStates} 同款）。</p>
 * <p>Screen geometry for the amplifier computer (pure data + pure functions, unit-testable):
 * the model's {@code PM} element (the lid) screen face turned into a render-side <b>panel basis</b>
 * — centre / normal / right / up / half extents, all in <b>model units</b> (16 units = 1 block).
 * Depends on JOML only, so server paths and unit tests can use it (same as
 * {@link KineticGaugeStates}).</p>
 *
 * <p><b>数字出处</b>（改模型就要同步这里）：铰链 = 键盘面后上棱 {@code (Y4, Z12)}；开盖角
 * {@code +22.5°}；盖子（{@code PM}）本地盒 {@code [1,4,12]–[15,14,13]}，屏幕面 = 其 north 面
 * （本地 z 最小那面，开盖时朝上朝前对着玩家）。旋转用与模型烘焙同一套约定（原版
 * {@code FaceBakery.applyElementRotation} → JOML 右手系绕 +X：{@code y' = y·cos−z·sin,
 * z' = y·sin+z·cos}），所以本类算出来的锚点与屏幕里画出来的模型<b>共面</b>。</p>
 * <p><b>Where the numbers come from</b> (keep in sync when the model changes): the hinge is the
 * keyboard deck's back-top edge {@code (Y4, Z12)}, the open angle is {@code +22.5°}, and the lid
 * ({@code PM}) is the local box {@code [1,4,12]–[15,14,13]} whose screen face is its north face
 * (the smallest-local-z one, facing up-and-forward at the player when open). The rotation uses
 * the same convention as the model bake (vanilla {@code FaceBakery.applyElementRotation} → JOML
 * right-handed about +X: {@code y' = y·cos − z·sin, z' = y·sin + z·cos}), so the anchors computed
 * here are exactly coplanar with the rendered model.</p>
 */
public final class AmplifierComputerStates {

    private AmplifierComputerStates() {}

    // ── 模型参数（与 assets-src/amplifier_computer/gf.json 的 PM 件一致）──
    /** 铰链：键盘面后上棱（模型单位）。 / hinge: the deck's back-top edge. */
    private static final float HINGE_Y = 4f, HINGE_Z = 12f;
    /** 开盖角（度，绕 +X 右手系；正 = 盖子向后仰）。 / open angle, right-handed about +X. */
    private static final float OPEN_DEG = 22.5f;
    /** 盖子（PM）本地盒。 / the lid's local box. */
    private static final float LID_X0 = 1f, LID_X1 = 15f, LID_Y0 = 4f, LID_Y1 = 14f, LID_Z0 = 12f;

    /**
     * 面板基（模型单位，上为正）：{@code (cx,cy,cz)} 中心、{@code n} 外法线、{@code r} 右、{@code u} 上，
     * {@code halfRight}/{@code halfUp} 半宽高。面板空间约定与动力仪表一致：x 沿 {@code r}、y 沿 {@code −u}、
     * z 沿 {@code −n}（负 z 朝观察者凸出）。
     * <p>Panel basis in model units: centre, outward normal, right, up and half extents. The panel
     * space matches the kinetic gauge's: x along {@code r}, y along {@code −u}, z along {@code −n}
     * (negative z protrudes toward the viewer).</p>
     */
    public record Panel(float cx, float cy, float cz,
                        float nx, float ny, float nz,
                        float rx, float ry, float rz,
                        float ux, float uy, float uz,
                        float halfRight, float halfUp) {}

    /** 开盖姿态的屏幕面（{@code lit=true} 时画在这里；合盖不画）。 / the open-pose screen face. */
    public static final Panel PANEL_OPEN = buildOpenPanel();

    private static Panel buildOpenPanel() {
        double a = Math.toRadians(OPEN_DEG);
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        float cx = (LID_X0 + LID_X1) / 2f;
        // 屏幕面中心（本地）→ 绕铰链旋转（与模型烘焙同式）
        float dy = (LID_Y0 + LID_Y1) / 2f - HINGE_Y, dz = LID_Z0 - HINGE_Z;
        float cy = HINGE_Y + dy * c - dz * s;
        float cz = HINGE_Z + dy * s + dz * c;
        // 本地法线 (0,0,−1) / 面内轴 A (1,0,0) / 面内轴 B (0,1,0) 同式旋转。
        // 面基的右/上按**右手系纪律**取（与 tools/gen_gauge_assets.py 的 derive_screen_plane 同一套）：
        // 更水平的面内轴当 right、up 朝上，然后强制 right×up = normal；不满足就把 right 取反。
        // 北面在旋转后 A=(1,0,0) 与 up=(0,c,s) 的叉积 = −normal → right 必须取 **(−1,0,0)**。
        // 写成 +X 会让面板基变成左手系（det = −1）→ 画出来的内容是镜像的（2026-10-11 实机复现：
        // 频谱左右翻、看着像转了 180°）。
        // Face basis right/up follow the right-handed discipline (same as the generator's
        // derive_screen_plane): the more horizontal in-plane axis becomes `right`, `up` points up,
        // then right×up must equal the normal — otherwise `right` is negated. For the north face
        // (1,0,0) × (0,c,s) = −normal, so right must be (−1,0,0); using +X makes the basis
        // left-handed (det = −1) and the drawn content comes out mirrored.
        return new Panel(cx, cy, cz,
            0f, s, -c,
            -1f, 0f, 0f,
            0f, c, s,
            (LID_X1 - LID_X0) / 2f, (LID_Y1 - LID_Y0) / 2f);
    }

    /**
     * 方块模型的**变体 y 角**：与 {@code blockstates/amplifier_computer.json} 的 y 表逐项一致
     * （north 0 / east 90 / south 180 / west 270）。
     * <p><b>为什么不用 {@code Direction#toYRot()} / {@code get2DDataValue()}</b>：原版这两者是
     * <b>「南 = 0」的实体朝向</b>约定（{@code toYRot()} 的定义就是 {@code (data2d & 3) * 90}，
     * 而各方向 data2d 为 north=2 / south=0 / west=1 / east=3），所以北向会得到 180°。方块模型的
     * 变体旋转是 <b>「北 = 0」</b>，两者恒定差 180°——2026-10-11 实机症状就是「屏幕 0 度、频谱
     * 180 度」（频谱被画到机器另一侧）。本方法由 {@code AmplifierComputerStatesTest} 对着
     * blockstates JSON 钉死。</p>
     * <p>Block model variant yaw, matching the blockstate table item by item (north 0 / east 90 /
     * south 180 / west 270). <b>Do not use {@code Direction#toYRot()} or {@code get2DDataValue()}</b>:
     * both follow the entity-yaw convention ("south = 0"; {@code toYRot()} is literally
     * {@code (data2d & 3) * 90} with north=2 / south=0 / west=1 / east=3), so north yields 180° —
     * a constant 180° offset from the model convention ("north = 0") that showed in-game as the
     * spectrum sitting on the machine's far side. Pinned by {@code AmplifierComputerStatesTest}
     * against the blockstate JSON.</p>
     */
    public static float modelYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 0f;
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
    }

    /**
     * 面板基矩阵：把面板空间（x 右 / y 下 / z 朝观察者）映到<b>以方块中心为原点</b>的模型空间
     * （见渲染器：先 translate(0.5,0.5,0.5)、再复刻朝向旋转、再 ×1/16 进模型单位）。
     * JOML 的 16 参数 {@code set} 是<b>列主序</b>，写错会整体转置（平移被塞进透视行 → 画面被拉成面条）。
     * 与 {@link KineticGaugeStates#panelBasis} 同一构造。
     * <p>Panel basis matrix mapping panel space (x right / y down / z toward the viewer) into
     * model space centred on the block. JOML's 16-arg {@code set} is <b>column-major</b>; getting
     * it wrong transposes the matrix and drags the translation into the perspective row.
     * Identical construction to {@link KineticGaugeStates#panelBasis}.</p>
     */
    public static Matrix4f panelBasis(Panel p) {
        return new Matrix4f().set(
            p.rx(), p.ry(), p.rz(), 0f,
            -p.ux(), -p.uy(), -p.uz(), 0f,
            -p.nx(), -p.ny(), -p.nz(), 0f,
            p.cx() - 8f, p.cy() - 8f, p.cz() - 8f, 1f);
    }
}
