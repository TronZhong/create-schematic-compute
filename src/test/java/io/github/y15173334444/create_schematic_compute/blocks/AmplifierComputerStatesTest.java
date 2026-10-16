package io.github.y15173334444.create_schematic_compute.blocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 功放电脑屏幕锚点的**模型钉死测试**：从产物模型 {@code amplifier_computer_on.json} 的 {@code PM} 件
 * 独立反算屏幕面平面（与 {@code tools/gen_gauge_assets.py} 的 derive_screen_plane 同一套约定），
 * 断言 {@link AmplifierComputerStates#PANEL_OPEN} 与之逐项一致，并单独钉住<b>右手系纪律</b>
 * （{@code right × up = normal}）。
 * <p>右手系那条不是形式主义：基一旦变成左手系（det = −1），画上去的内容会<b>镜像</b>——
 * 2026-10-11 实机就是这个症状（频谱左右翻、看着像转了 180°），根因是 right 取了 +X 而不是 −X。</p>
 * <p>Anchor pinning test: re-derives the screen plane from the product model's {@code PM} element
 * (the same conventions as the generator's derive_screen_plane) and asserts
 * {@link AmplifierComputerStates#PANEL_OPEN} matches it item by item, with a dedicated guard for
 * the <b>right-handed rule</b> ({@code right × up = normal}). That rule is not formalism: a
 * left-handed basis (det = −1) draws mirrored content — exactly the in-game symptom on
 * 2026-10-11 (the spectrum read flipped, looking like a 180° turn), caused by taking right = +X
 * instead of −X.</p>
 */
class AmplifierComputerStatesTest {

    /** 模型里屏幕件的名字（与生成器约定同款：命名是主标识）。 / the screen element's name. */
    private static final String SCREEN_ELEMENT = "PM";
    private static final double EPS = 1e-4;

    @Test
    @DisplayName("面板锚点与模型 PM 件逐项一致 / the panel matches the model's PM element")
    void panelMatchesModel() throws Exception {
        JsonObject model = loadModel("amplifier_computer_on");
        JsonObject pm = null;
        for (var e : model.getAsJsonArray("elements")) {
            JsonObject el = e.getAsJsonObject();
            if (el.has("name") && SCREEN_ELEMENT.equals(el.get("name").getAsString())) { pm = el; break; }
        }
        assertNotNull(pm, "模型里必须有名为 " + SCREEN_ELEMENT + " 的屏幕件 / the model needs a " + SCREEN_ELEMENT + " element");

        // 元素旋转（本件：绕 X，origin 在铰链上） / element rotation (about X, origin on the hinge)
        JsonObject rot = pm.has("rotation") ? pm.getAsJsonObject("rotation") : null;
        String axis = rot != null ? rot.get("axis").getAsString() : null;
        double ang = rot != null ? rot.get("angle").getAsDouble() : 0;
        JsonArray o = rot != null ? rot.getAsJsonArray("origin") : null;
        double[] org = o != null ? new double[]{o.get(0).getAsDouble(), o.get(1).getAsDouble(), o.get(2).getAsDouble()}
                                 : new double[]{0, 0, 0};
        JsonArray f = pm.getAsJsonArray("from"), t = pm.getAsJsonArray("to");
        double[] from = {f.get(0).getAsDouble(), f.get(1).getAsDouble(), f.get(2).getAsDouble()};
        double[] to = {t.get(0).getAsDouble(), t.get(1).getAsDouble(), t.get(2).getAsDouble()};

        // north 面基（FACE_BASIS）：法线 + 面内轴 A/B / the north face basis
        double[] nL = {0, 0, -1}, aL = {1, 0, 0}, bL = {0, 1, 0};
        int dimA = 0, dimB = 1, dimN = 2;
        double[] n = rotDir(nL, axis, ang), a = rotDir(aL, axis, ang), b = rotDir(bL, axis, ang);
        double halfA = Math.abs(to[dimA] - from[dimA]) / 2, halfB = Math.abs(to[dimB] - from[dimB]) / 2;
        double halfN = Math.abs(to[dimN] - from[dimN]) / 2;
        double[] mid = {(from[0] + to[0]) / 2, (from[1] + to[1]) / 2, (from[2] + to[2]) / 2};
        double[] faceCentre = rotPt(new double[]{mid[0] + nL[0] * halfN, mid[1] + nL[1] * halfN, mid[2] + nL[2] * halfN},
            axis, ang, org);

        // 右 = 更水平的在面内轴；up 朝上；再强制右手系 / pick right, force up upward, force right-handed
        double[] right, up; double halfRight, halfUp;
        if (Math.abs(a[1]) <= Math.abs(b[1])) { right = a; up = b; halfRight = halfA; halfUp = halfB; }
        else { right = b; up = a; halfRight = halfB; halfUp = halfA; }
        if (up[1] < 0) up = neg(up);
        if (dot(cross(right, up), n) < 0) right = neg(right);

        // 右手系纪律（镜像的回归锚点）/ the right-handed rule (regression anchor for the mirror bug)
        assertEquals(1.0, dot(cross(right, up), n), 1e-6, "面板基必须右手系（否则内容镜像）");

        var panel = AmplifierComputerStates.PANEL_OPEN;
        assertEquals(faceCentre[0], panel.cx(), EPS, "面心 X");
        assertEquals(faceCentre[1], panel.cy(), EPS, "面心 Y");
        assertEquals(faceCentre[2], panel.cz(), EPS, "面心 Z");
        assertEquals(n[0], panel.nx(), EPS, "法线 X");
        assertEquals(n[1], panel.ny(), EPS, "法线 Y");
        assertEquals(n[2], panel.nz(), EPS, "法线 Z");
        assertEquals(right[0], panel.rx(), EPS, "右 X");
        assertEquals(right[1], panel.ry(), EPS, "右 Y");
        assertEquals(right[2], panel.rz(), EPS, "右 Z");
        assertEquals(up[0], panel.ux(), EPS, "上 X");
        assertEquals(up[1], panel.uy(), EPS, "上 Y");
        assertEquals(up[2], panel.uz(), EPS, "上 Z");
        assertEquals(halfRight, panel.halfRight(), EPS, "半宽");
        assertEquals(halfUp, panel.halfUp(), EPS, "半高");

        // 恒等式的另一半：我方常量自己也必须是右手系（防手改 / same guard on our constants）
        assertEquals(1.0, panel.rx() * (panel.uy() * panel.nz() - panel.uz() * panel.ny())
            + panel.ry() * (panel.uz() * panel.nx() - panel.ux() * panel.nz())
            + panel.rz() * (panel.ux() * panel.ny() - panel.uy() * panel.nx()), 1e-4,
            "PANEL_OPEN 必须右手系 / PANEL_OPEN must be right-handed");
    }

    @Test
    @DisplayName("朝向 y 角与 blockstates 的变体 y 逐项一致 / modelYaw matches the blockstate table")
    void modelYawMatchesBlockstates() throws Exception {
        JsonObject bs = JsonParser.parseString(Files.readString(Path.of("src", "main", "resources", "assets",
            "create_schematic_compute", "blockstates", "amplifier_computer.json"))).getAsJsonObject();
        JsonObject variants = bs.getAsJsonObject("variants");
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction f : facings) {
            String key = "facing=" + f.getSerializedName() + ",lit=true";
            JsonObject v = variants.getAsJsonObject(key);
            assertNotNull(v, "blockstates 缺少变体 " + key);
            float expected = v.has("y") ? v.get("y").getAsFloat() : 0f;
            assertEquals(expected, AmplifierComputerStates.modelYaw(f), 1e-4,
                f + " 的模型 y 角必须与 blockstates 一致");
        }
        // 哨兵：Direction#toYRot 是「南=0」的实体约定（北=180），与方块模型（北=0）恒定差 180°。
        // 2026-10-11 的实机症状（频谱画到机器另一侧）就是照抄它造成的——这条断言把那条路封死。
        // Sentinel: Direction#toYRot is the entity convention (north = 180°), a constant 180° off the
        // block model's (north = 0°). Copying it caused the 2026-10-11 in-game symptom (the spectrum
        // drawn on the machine's far side) — this assertion closes that door.
        assertTrue(Math.abs(Direction.NORTH.toYRot() - AmplifierComputerStates.modelYaw(Direction.NORTH)) > 1e-4,
            "北向时 toYRot()=" + Direction.NORTH.toYRot() + " ≠ modelYaw(北)，两者不可互换");
    }

    // ── 与生成器同款的向量工具 / same helpers as the generator ──

    private static JsonObject loadModel(String name) throws Exception {
        Path p = Path.of("src", "main", "resources", "assets", "create_schematic_compute",
            "models", "block", name + ".json");
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    private static double[] rotDir(double[] v, String axis, double ang) {
        if (axis == null || ang == 0) return v.clone();
        double a = Math.toRadians(ang), c = Math.cos(a), s = Math.sin(a);
        return switch (axis) {
            case "x" -> new double[]{v[0], v[1] * c - v[2] * s, v[1] * s + v[2] * c};
            case "y" -> new double[]{v[0] * c + v[2] * s, v[1], -v[0] * s + v[2] * c};
            case "z" -> new double[]{v[0] * c - v[1] * s, v[0] * s + v[1] * c, v[2]};
            default -> throw new IllegalArgumentException("axis " + axis);
        };
    }

    private static double[] rotPt(double[] p, String axis, double ang, double[] org) {
        double[] q = {p[0] - org[0], p[1] - org[1], p[2] - org[2]};
        double[] r = rotDir(q, axis, ang);
        return new double[]{r[0] + org[0], r[1] + org[1], r[2] + org[2]};
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static double dot(double[] a, double[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }

    private static double[] neg(double[] v) { return new double[]{-v[0], -v[1], -v[2]}; }
}
