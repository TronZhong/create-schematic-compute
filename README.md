<h1 align="center"> Create: Schematic Compute </h1>

<p align="center">
  <b> 12 Programmable Blocks · 101 Node Types · Formula Syntax Highlighting & Autocomplete · Multiplayer Collaboration</b><br>
  <b>十二种可编程方块 · 101种节点 · 公式语法高亮与自动补全 · 多人实时协作</b><br>
  <i>Drag, connect, and build logic — just like Unreal Engine Blueprints!</i><br>
  <i>拖拽连接，构建逻辑 — 像虚幻引擎蓝图一样直观！</i><br>
  <i>Created by <b>StarryNight_Luo</b> (y15173334444)</i>
</p>

<p align="center">
  <a href="https://github.com/y15173334444/create-schematic-compute"><img src="https://img.shields.io/badge/GitHub-y15173334444/create--schematic--compute-blue?style=flat-square&logo=github" alt="GitHub"></a>
  <a href="https://github.com/y15173334444/create-schematic-compute/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License"></a>
  <a href="https://github.com/y15173334444/create-schematic-compute/releases"><img src="https://img.shields.io/badge/Version-1.2.5.2-blue?style=flat-square" alt="Version"></a>
  <a href="https://neoforged.net/"><img src="https://img.shields.io/badge/NeoForge-21.1.233-orange?style=flat-square" alt="NeoForge"></a>
  <a href="https://modrinth.com/mod/create"><img src="https://img.shields.io/badge/Create-6.0.10-brightgreen?style=flat-square" alt="Create"></a>
  <a href="https://www.minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-1.21.1-8B4513?style=flat-square" alt="MC"></a>
</p>

---

##  Overview / 简介

**🇬🇧** Create: Schematic Compute is a **Create mod addon** that introduces **12 programmable blocks + 1 portable terminal** with a **visual node-based programming system**. Instead of writing complex redstone circuits, simply drag and connect nodes to build logic — just like Unreal Engine Blueprints or Blender Geometry Nodes. Each computer runs at **20Hz (every game tick)** for real-time control. **All 12 blocks support real-time multiplayer collaborative editing** with live cursor tracking and node lock protection. The **FORMULA script editor** features syntax highlighting (9 token colours), intelligent autocomplete (functions, variables, `@output`), real-time validation with error badges, and named constants `(PI)`/`(E)`.

**🇨🇳** **机械动力：蓝图计算机** 是一个机械动力附属模组，添加了**十二种可编程方块和一个便携终端**，采用**可视化节点图编程系统**。无需搭建复杂红石电路，只需拖拽连接节点即可构建逻辑——就像虚幻引擎的蓝图系统或 Blender 的几何节点一样直观。每台设备拥有独立的节点图，以 **20Hz（每游戏刻）** 的频率运行，适合实时控制应用。**全部 12 种方块支持多人实时协作编辑**，带实时光标追踪和节点锁定保护。**FORMULA 公式脚本编辑器** 支持语法高亮（9 种词法颜色）、智能自动补全（函数、变量、`@output`）、实时校验与错误徽章、以及命名常量 `(PI)`/`(E)`。

---

## Mod Detail / 模组详情

- For a detailed explanation, see **[`mod-detail.md`](/docs/mod-detail.md)**. / 详细讲解见 **[`mod-detail.md`](/docs/mod-detail.md)**。



---

##  FAQ / 常见问题


<summary><b>Node editor laggy? / 节点编辑器卡顿？</b></summary>

Editor drawing is viewport-culled (v1.2.5.2): only on-screen nodes cost render time. 1024 nodes on screen runs fine — node count and PID count are no longer practical limits. / v1.2.5.2 起编辑器绘制做了视口裁剪（只画屏内节点）：同屏 1024 个节点也无压力，节点数量与 PID 数量不再是实际限制。


<summary><b>Speed Proxy not working? / 转速代理不工作？</b></summary>

Place directly adjacent to a Speed Controller. / 放置在转速控制器相邻面。


<summary><b>State lost after schematic? / 蓝图放置后状态丢失？</b></summary>

Use Create 6.0.10+. Full NBT interfaces registered. / 确保使用Create 6.0.10+。


<summary><b>Can computers communicate? / 计算机可以通信吗？</b></summary>

Yes — Private Signal I/O (named channels) or BUS_IN/BUS_OUT (banded). / 可以——私有信号I/O或BUS总线。


---

##  Installation / 安装

| Dependency / 依赖 | Version / 版本 |
|------------------|---------------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233+ |
| Create | 6.0.10+ |

*Sable is optional / Sable 为可选*

1. Install NeoForge + Create / 安装 NeoForge + Create
2. Place `.jar` in `mods` folder / 将 `.jar` 放入 `mods`
3. Launch / 启动

---

##  Changelog / 更新日志

The full changelog now lives in **[`CHANGELOG.md`](/CHANGELOG.md)** — one `<details>` block per release. / 
完整变更日志已拆分至 **[`CHANGELOG.md`](/CHANGELOG.md)** —— 每个版本一个 `<details>` 块。

---

##  Links / 链接

- **GitHub**: [github.com/y15173334444/create-schematic-compute](https://github.com/y15173334444/create-schematic-compute)
- **Modrinth**: [modrinth.com/mod/create-schematic-compute](https://modrinth.com/mod/create-schematic-compute)
- **License / 许可证**: MIT © 2026 StarryNight_Luo

---

<p align="center">
  <b> If you enjoy this mod, star us on GitHub! / 喜欢请在GitHub点！</b><br>
  <i>Unleash Create's potential with visual programming! / 用可视化编程释放机械动力的潜力！</i>
</p>
