# Create: Schematic Compute

<p align="center">
  <b>🎮 12 Programmable Blocks · 101 Node Types · Formula Syntax Highlighting & Autocomplete · Multiplayer Collaboration</b><br>
  <b>十二种可编程方块 · 101种节点 · 公式语法高亮与自动补全 · 多人实时协作</b><br>
  <i>Drag, connect, and build logic — just like Unreal Engine Blueprints!</i><br>
  <i>拖拽连接，构建逻辑 — 像虚幻引擎蓝图一样直观！</i><br>
  <i>Created by <b>StarryNight_Luo</b> (y15173334444)</i>
</p>

<p align="center">
  <a href="https://github.com/y15173334444/create-schematic-compute"><img src="https://img.shields.io/badge/GitHub-y15173334444/create--schematic--compute-blue?style=flat-square&logo=github" alt="[...]">
  <a href="https://github.com/y15173334444/create-schematic-compute/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License"></a>
  <a href="https://github.com/y15173334444/create-schematic-compute/releases"><img src="https://img.shields.io/badge/Version-1.2.5.2-blue?style=flat-square" alt="Version"></a>
  <a href="https://neoforged.net/"><img src="https://img.shields.io/badge/NeoForge-21.1.233-orange?style=flat-square" alt="NeoForge"></a>
  <a href="https://modrinth.com/mod/create"><img src="https://img.shields.io/badge/Create-6.0.10-brightgreen?style=flat-square" alt="Create"></a>
  <a href="https://www.minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-1.21.1-8B4513?style=flat-square" alt="MC"></a>
</p>

---

## Table of Contents / 目录

- [Overview / 简介](#overview--简介)
- [Blocks / 方块](#blocks--方块)
- [Multiplayer Collaboration / 多人协作](#multiplayer-collaboration--多人协作v124)
- [Formula Script Node / 公式脚本节点](#formula-script-node--公式脚本节点)
- [BUS System / BUS 总线系统](#bus-system--bus-总线系统)
- [Node Reference / 节点参考](#node-reference--节点参考101-
)
- [Controls / 操作指南](#controls--操作指南)
- [Sable Physics Integration / Sable 物理集成](#sable-physics-integration--sable-物理集成)
- [Schematic Support / 蓝图兼容](#schematic-support--蓝图兼容)
- [Recipes / 合成配方](#recipes--合成配方)
- [Block Properties / 方块属性](#block-properties--方块属性)
- [Technical Highlights / 技术亮点](#technical-highlights--技术亮点)
- [Changelog / 更新日志](#changelog--更新日志)
- [FAQ / 常见问题](#faq--常见问题)
- [Installation / 安装](#installation--安装)
- [Links / 链接](#links--链接)

---

<a id="overview--简介"></a>
## 📖 Overview / 简介

**🇬🇧** Create: Schematic Compute is a **Create mod addon** that introduces **12 programmable blocks + 1 portable terminal** with a **visual node-based programming system**. Instead of writin[...]

**🇨🇳** **机械动力：蓝图计算机** 是一个机械动力附属模组，添加了**十二种可编程方块和一个便携终端**，采用**可视化节点图编程系统**。无需��[...]

---

<a id="blocks--方块"></a>
## 🖥️ Blocks / 方块

### 🖥️ Holographic Monitor / 全息显示器
**3D floating display / 3D 悬浮显示方块**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🖼️ Display Nodes / 显示节点 | TEXT, DATA, IMAGE, IMAGE_SEQUENCE / 文本、数值、图片、动画 |
| 🎨 16×16 Pixel Editor / 16×16像素编辑器 | Multi-frame animation + undo/redo / 多帧动画+撤销重做 |
| 📋 Layer Panel / 图层面板 | Drag-drop reorder + 24×24 thumbnails / 拖拽排序+缩略图预览 |
| 🎯 3D Positioning / 3D定位 | X/Y/Z + Roll/Pitch/Yaw freely adjustable / 自由调整位置和旋转 |
| 📡 Signal-Driven / 信号驱动 | IMAGE position/rotation via input signals / 通过输入信号驱位置旋转 |
| 📡 Redstone Input / 红石输入 | Read Redstone Link signals / 从红石链接网络读取信号 |

---

### 🖥️ Blueprint Computer / 蓝图计算机
**Redstone Link controller / 红石链接控制器**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📡 Redstone I/O / 红石I/O | Read/Write Redstone Link network / 读写红石链接网络 |
| 🔗 Private Signal / 私有信号 | Named channel cross-computer communication / 命名通道跨计算机通信 |
| 🚌 Bus System / 总线系统 | BUS_IN/BUS_OUT multi-band data sharing / 多频段数据共享 |
| 📦 Encapsulation I/O / 封装导入导出 | File browser import/export .nbt files / 文件浏览器导入导出 |

---

### ⚡ Speed Proxy / 转速代理控制器
**Speed Controller direct control / 转速直控**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🔄 Speed Control / 转速控制 | Set adjacent Speed Controller RPM (-256~256) / 设置相邻转速控制器RPM |
| 🔗 Private Signal / 私有信号输入 | Named channel cross-computer coordination / 命名通道跨计算机联动 |

---

### ⚙️ Programmable Transmission / 可编程变速器
**In-line programmable gearbox / 轴上可编程变速**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🎚️ Target RPM / 目标转速 | Scroll-set absolute output RPM; graph TX_OUT node overrides while running / 滚轮设定绝对输出转速，图运行时由 TX_OUT 节点接管 |
| 🔀 Face-Relative Direction / 面相对转向 | Output CW/CCW follows the placed face, creative-motor style / 输出逆/顺随放置面（创造马达同款语义） |
| ↕️ 3-Axis Placement / 三向放置 | Horizontal or vertical in-line placement; wrench on side face rotates the axis / 水平/竖直线上放置，扳手点侧面换轴 |
| ⚙️ Bearing Visuals / 轴承动画 | Two independent shaft stubs: input follows network, output follows target / 两端独立轴头动画：输入随网络、输出随目标 |
| 🤝 Stress Network Member / 应力网络成员 | First-class driven member — no input power, no output / 官方应力网络一等成员，无输入动力则无输出 |

---

### 🔧 CNC Gearbox / 数控齿轮箱
**Driven clutch + motion quota / 从动离合器 + 运动配额**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🔌 Clutch / 离合器 | Output face carries a shaft only while engaged; idle auto-disengages / 仅接合时输出面对外传轴，空闲自动分离 |
| 📜 Command Stack / 指令栈 | FIFO motion commands: rotate (degrees) / move (meters) / wait / FIFO 运动指令：旋转（度）/ 直线（米）/ 等待 |
| 🎯 Motion Quota / 运动配额 | Open-loop travel booking, fires a completion pulse at zero / 开环行程记账，配额归零自动打完成脉冲 |
| 🔄 Encoder / 编码器 | Rotary (degrees) + linear (meters) accumulation for graph readback / 旋转（度）+ 直线（米）累计供图读取 |
| ↕️ 3-Axis + Wrench / 三向 + 扳手 | Wrench on end face flips the input, side face rotates the axis / 扳手点端面翻转输入端、点侧面换轴 |

---

### 🔌 Program Computer / 编程计算机
**Sequential logic / 时序逻辑专用机**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📡 Redstone I/O / 红石I/O | Redstone Link network communication / 红石链接网络通信 |
| 🔗 Private Signal / 私有信号 | PRIVATE_IN/PRIVATE_OUT named channel I/O / 命名通道I/O |
| ⏱️ Sequential Nodes / 时序节点 | Delay/Latch/T Flip-Flop/Gate/Pulse Extend/Loop/Fuse/Accumulator/Integrator / 延时/锁存器/T触发器/闸门/脉冲延长/循环/保险/累计器/连[...]

---

### 🪑 Control Seat / 控制座椅
**Sit-able controller / 可乘坐控制器**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| ⌨️ 58 Key Bindings / 58键绑定 | Click-to-bind UI / 点击绑定 |
| 🖱️ Dual Mode / 双模式 | Joystick (mouse delta) / View Angle (rotation difference) / 摇杆/视角差 |
| 🎮 Gamepad / 手柄 | Dual stick + 15 buttons + analog triggers LT/RT / 双摇杆+15键+模拟扳机 |
| 🔄 Sable Compatible / Sable兼容 | Two camera modes (FIXED / VIEW_DIFFERENCE), world-orientation tracking via quaternion / 双相机模式，四元数世界朝向追踪 |
| 🚪 Controls / 操作 | Right-click sit / `Shift`+Right-click editor / `~` dismount / `TAB` mode / `ESC` release |

---

### 📐 Attitude Sensor / 姿态传感器
**Physics structure orientation / 物理结构姿态读取**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📐 ATTITUDE / 姿态 | Block world-space pitch and roll (facing × sub-level rotation) / 方块自身世界姿态俯仰与横滚（朝向 × 子世界旋转） |
| 🧭 FORWARD / 前方朝向 | World-space forward yaw/pitch / 结构世界空间朝向 |
| ⚡ ACCELERATION / 加速度 | Block-local X/Y/Z acceleration (structure motion in block axes) / 方块本地加速度（结构运动按方块朝向分解） |
| 🚀 VELOCITY / 速度 | Block-local velocity ×2 m/s (structure motion in block axes) / 方块本地速度（结构运动按方块朝向分解） |
| 🔄 POSE_CONVERT / 姿态换算 | Coordinate conversion / 坐标系转换 |

---

### 📡 3D Holographic Radar / 3D全息显示雷达
**Real-time scanner / 实时实体扫描器**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📡 Scan Range / 扫描范围 | 1-128 blocks configurable / 1-128格可配置 |
| 🎯 Target Lock / 目标锁定 | Manual right-click + auto closest / 手动右键锁定+自动最近 |
| 🖥️ Display Style / 显示风格 | Classic XYZ axes / Holographic (white cube + blue plane) / 经典/全息 |
| 📊 TARGET_OUT | Output X/Y/Z/entity ID/distance / 输出坐标/实体ID/距离 |
| 🔍 Filters / 过滤 | Show/hide players, mobs, Sable structures / 独立显示玩家/生物/Sable |

---

### 📱 Portable Terminal / 便携终端
**Handheld remote editor / 手持远程编辑器**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📡 Device Scan / 设备扫描 | Scan 1-128 blocks for programmable blocks / 扫描1-128格可编程方块 |
| ✏️ One-Click Edit / 一键编辑 | Open native GUI instantly / 即时打开原生GUI |
| 📦 All 12 Blocks / 全12方块 | Monitor, Blueprint, Program, Radar, Seat, Sensor, SpeedProxy, Transmission, CNC Gearbox, Kinetic Gauge, Amplifier, Speaker |
| 🔄 Sable Compatible / Sable兼容 | Sub-level scanning with rotation correction / 子世界扫描+旋转修正 |

---

### 🎛️ Kinetic Gauge / 动力传感器
**In-line kinetic readout + graph host / 轴上传动读数 + 节点图宿主**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 📊 Built-in Readout / 内置读数 | Speed digits + stress bar (green→yellow→red, overload blink) + percentage, painted on the panel / 转速数字 + 应力条（绿→黄→红，超载�[...]
| 🖥️ Graph Display / 图形接管 | DATA/TEXT nodes in its graph take over the panel (server-authoritative eval snapshot) / 图里的 DATA/TEXT 节点接管面板（服务端求值快照权�[...]
| 📈 STRESS / 应力状态 | 4 outputs — ratio (0-1, >1 overloaded), used (SU), unused (0-1), left (SU) / 四个输出——占比、已用（SU）、未用、剩余（SU） |
| ⚡ RPM / 转速 | 1 output — signed network speed (0 unpowered/overloaded) / 一个输出——带符号网络转速（无动力/过载为 0） |
| 🔄 3-Axis Placement / 三轴放置 | Floor/ceiling placement points the display at the player (all 4 yaws, vertical state placeable directly); wall placement uses the clicked face; auto-align[...]
| 🔧 Wrench / 扳手 | Shaft end: roll 90° through the four same-shaft poses (shaft fixed); top/bottom: yaw 90° keeping the current tilt; other faces: Create's IWrenchable default / 轴端面：��[...]
| ⚙️ Shaft Pass-Through / 贯通传轴 | Passes rotation along its own axis and reads the network it sits in / 自身沿旋转轴贯通传轴，并读取所在动力网络的转速与应力 |
| 🧩 Two Model Variants / 双模型变体 | Lectern panel for horizontal shafts, flat panel for vertical shafts; text always upright / 水平轴用讲台面板，竖直轴用平板屏，文字永远[...]

---

### 🎛️ Amplifier Computer / 功放计算机

**Audio graph host with program-computer timing / 音频图宿主 + 编程计算机时序**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🎵 Audio Nodes / 音频节点 | MUSIC · AMP · WSHAPE · AUDIO_OUT · CHANNEL · SPEAKER_PLAY / 曲目 · 功放 · 波形整形 · 频段发布 · 声道拆分 · 播放 |
| 🎚️ Dynamics Curve / 力度曲线 | XY curve on the AMP node: note loudness → output level, chain gain applies after / AMP 节点 XY 曲线：音符力度→输出力度，链上增益随后��[...]
| 🌊 Wave Shaper / 波形整形 | Per-sample shaping curve, identity curve = true bypass / 逐采样整形曲线，恒等曲线 = 真旁路 |
| 📡 Band Publish / 频段发布 | Publish audio to bus / private bands for speaker graphs to subscribe / 发布音频到总线/私有频段，供电音图订阅 |
| ⏱️ Timing / 时序 | Program-computer capability for play/stop triggers and delay staging / 编程计算机能力，供起停触发与延时编排 |

---

### 🔊 Speaker / 音响

**Band-subscribed playback terminal / 频段订阅式播放终端**

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🔉 Dumb Terminal / 哑终端 | Player-authored graph `BUS_IN --audio wire--> SPEAKER_PLAY` passes through identically; channel splitting belongs to the upstream CHANNEL / 图由玩家自己摆放�[...]
| 🔊 World Playback / 世界发声 | Plays the subscribed band at its own position / 在自身坐标播放订阅频段 |
| ⚙️ Playback Settings / 播放设置 | Gain (0-4) · audible radius (1-4096, default 48) · mute, in the editor panel / 编辑器面板设增益（0-4）、可听半径（1-4096，默认 48）、[...]
| 🔴 Redstone Mute / 红石静音 | Redstone high silences the speaker / 红石高电平静音 |

---

<a id="multiplayer-collaboration--多人协作v124"></a>
## 👥 Multiplayer Collaboration / 多人协作（v1.2.4+）

Real-time collaborative graph editing for all 12 block types. Multiple players can edit the same graph simultaneously.
全部 12 种方块支持多人实时协作编辑同一节点图。

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🖱️ Live Cursor Tracking / 实时光标 | Colored crosshairs with player names / 彩色十字准星+玩家名 |
| 📦 Remote Drag / 远程拖拽 | Smooth animated node movement / 平滑动画节点移动 |
| 🔗 Wire Preview / 连线预览 | Live bezier curve while dragging / 实时贝塞尔曲线预览 |
| 👤 Player List / 玩家列表 | Right-side vertical list, host highlighted / 右侧竖向列表，房主高亮 |
| 🔒 Node Lock / 节点锁定 | IMAGE nodes protected during pixel edit / 像素编辑时锁定IMAGE节点 |
| 🚪 Auto-Close / 自动关闭 | UI closes when block destroyed / 方块破坏时自动关闭 |
| ⚡ Join/Leave / 加入离开 | Appear immediately on open, disappear on close / 打开即现，关闭即消 |

---

<a id="formula-script-node--公式脚本节点"></a>
## 📝 Formula Script Node / 公式脚本节点

Multi-line script editor (v1.2.0+) — assignments, control flow, vec3, named outputs, comments, line continuation.
多行脚本编辑器 — 支持赋值、控制流、vec3、命名输出、注释、续行。完整语法见 [`docs/formula-syntax-manual.md`](https://github.com/y15173334444/create-schematic-compute/[…]

### 🧮 Syntax Overview / 语法速览

**赋值与输入引脚 / Assignments & input pins** — any name assigned anywhere is an internal variable; every other name that is read becomes an input pin. 任何处被赋值的名字是内部变[...]
```
x = a + 1        -- x 内部变量 / internal; a 成为输入引脚 / input pin
@output x
```
→ 1 input pin + 1 output / 1 输入 + 1 输出

... (内容保持不变，以下段落继续原 README 的其余内容) ...

<a id="bus-system--bus-总线系统"></a>
## 🚌 BUS System / BUS 总线系统

Global named-channel communication across computers. Like publish-subscribe message bus.
全局命名通道跨计算机通信系统，类似发布-订阅消息总线。

| Feature / 功能 | Description / 说明 |
|----------------|-------------------|
| 🚌 BUS_OUT / 总线输出 | Write values to named channel with bands / 写入命名通道+频段 |
| 🚌 BUS_IN / 总线输入 | Read band values from channel / 从通道读取频段值 |
| 📋 Band System / 频段 | Named sub-fields per channel / 每通道命名字段 |
| 🔢 Ref Counting / 引用计数 | Auto-cleanup when no BUS_OUT references / 无引用时自动清理 |
| ⚠️ Conflict Detection / 冲突检测 | Reject duplicate channel names / 拒绝重名通道 |

---

<a id="node-reference--节点参考101-"></a>
## 🧩 Node Reference / 节点参考（101 种）

<details>
<summary><b>📦 Values / 数值</b></summary>

| Node / 节点 | Description / 说明 |
|-------------|-------------------|
| Constant / 常量 | Outputs constant value / 输出常量值 |
| Redstone Input / 红石输入 | Reads from Redstone Link / 从红石链接网络读取 |
| Private Signal Input / 私有信号输入 | Reads float from named channel / 从命名通道读取浮点数 |
| Bus Input / 总线输入 | Reads bus channel bands / 从总线通道读取频段值 |

</details>

... (其余 Node Reference 内容保持不变) ...

<a id="controls--操作指南"></a>
## 🎮 Controls / 操作指南

| Action / 操作 | Input / 按键 |
|---------------|-------------|
| Add node menu / 添加节点 | Right-click empty / 右键空白 |
| **Two-column layout / 双列布局** | Click the state-label button on the menu title row (`1 Col`/`2 Col`), gold = two columns on / 点击菜单标题行右侧状态文字按钮（`单列`/`双列`[...] |

... (其余 Controls 内容保持不变) ...

<a id="sable-physics-integration--sable-物理集成"></a>
## 🔄 Sable Physics Integration / Sable 物理集成

Deep integration with Sable physics engine for rotating structures. / 与Sable物理引擎深度集成，支持旋转结构。

| Block / 方块 | Feature / 功能 |
|-------------|---------------|
| Control Seat / 控制座椅 | Entity yaw tracks sub-level rotation / 实体yaw追踪子世界 |
| Attitude Sensor / 姿态传感器 | Read `logicalPose()` quaternion / 读取姿态四元数 |

> **Thread safe / 线程安全**: Shared fields `volatile`. **Without Sable / 无Sable**: Control Seat fully functional, Sensor outputs 0 / 控制座椅完全可用，传感器输出0

---

<a id="schematic-support--蓝图兼容"></a>
## 💾 Schematic Support / 蓝图兼容

All 12 blocks support **Create's Schematicannon** — graphs and state fully preserved. / 全部十二种方块支持**蓝图大炮**，图与状态完整保留。

Uses Create's `IMergeableBE` + `SafeNbtWriter` / 采用 Create 官方接口

---

<a id="recipes--合成配方"></a>
## 📦 Recipes / 合成配方

| Block / 方块 | Materials / 材料 |
|-------------|-----------------|
| 🖥️ Monitor / 全息显示器 | Redstone Link×2 + Precision Mechanism + Glass Pane×2 + Brass Casing + Glowstone Dust×2 |
| 🖥️ Blueprint / 蓝图计算机 | Redstone Link×2 + Precision Mechanism + Glass Pane×2 + Repeater + Comparator + Brass Casing×2 |
| ⚡ Speed Proxy / 转速代理 | Brass Ingot×4 + Cogwheel + Glass Pane×2 + Comparator + Andesite Casing |
| 🔌 Program / 编程计算机 | Andesite Casing×4 + Repeater + Glass Pane×2 + Comparator + Andesite Alloy |
| 🪑 Control Seat / 控制座椅 | Heavy Weighted Pressure Plate + Iron Ingot×2 + Brass Casing + Redstone + Redstone Link×4 |
| 📐 Attitude Sensor / 姿态传感器 | Iron Ingot×6 + Repeater + Comparator + Brass Casing×2 |
| 📡 Radar / 雷达 | Monitor×2 + Iron Ingot×4 + Brass Casing + Redstone Block×2 |
| 📱 Portable Terminal / 便携终端 | Redstone Link×4 + Blueprint Computer + Glass Pane×4 |
| 🎛️ Kinetic Gauge / 动力传感器 | Iron Ingot×4 + Shaft×4 + Brass Casing |

---

<a id="block-properties--方块属性"></a>
## ⚙️ Block Properties / 方块属性

| Property / 属性 | Value / 值 |
|-----------------|-----------|
| Hardness / 硬度 | 1.0 (hand breakable / 空手可破坏) |
| Hand break / 空手破坏 | Drops without NBT / 掉落无NBT |
| Wrench right-click / 扳手右键 | Rotate FACING / 旋转方向 |
| Wrench Shift+right-click / 扳手Shift+右键 | Pick up with full NBT / 收回保留NBT |
| 🎛️ Kinetic Gauge wrench / 动力传感器扳手 | Shaft end: roll through the 4 same-shaft poses (shaft fixed); top/bottom: tilt-preserving yaw; others: Create's default / 轴端面：同�[...]

---

<a id="technical-highlights--技术亮点"></a>
## 🔧 Technical Highlights / 技术亮点

| Feature / 特性 | Description / 说明 |
|----------------|-------------------|
| 👥 Multiplayer Collaboration / 多人协作 | Real-time editing, cursor tracking, node lock / 实时编辑、光标追踪、节点锁 |
| ⚡ Server-Authoritative Eval / 服务端权威评估 | Client receives `EvalSnapshot` — no local evaluator / 客户端接收快照，无本地评估器 |
| ⚡ Topological Sort Eval / 拓扑排序求值 | O(1) input query cache / O(1) 输入查询缓存 |
| 🚀 GC-Friendly / GC友好 | Reused evaluator instances / 重用求值器 |
| 🔄 Signal Bus / 信号总线 | Global named-channel communication / 全局命名通道通信 |
| 🧹 PID Anti-Windup / PID抗饱和 | Integral capping / 积分上限钳制 |
| 🛡️ Cycle Detection / 环检测 | Compile-time circular dependency check / 编译时循环引用检测 |
| 🎮 GLFW Raw Input / GLFW原始输入 | Bypass Minecraft keybinding system / 绕过MC键位系统 |
| 🔄 Sable Integration / Sable集成 | `BlockEntitySubLevelActor` sub-level pose reading / 子世界姿态读取 |

---

<a id="changelog--更新日志"></a>
## 📜 Changelog / 更新日志

完整变更日志已拆分至 **[`CHANGELOG.md`](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md)** —— 每个版本一个 `<details>` 块。
The full changelog now lives in **[`CHANGELOG.md`](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md)** — one `<details>` block per release.

| Version | 标题 / Title |
|---------|--------------|
| [v1.2.5.2](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md#v1252) | 修复：动力传感器扳手旋转（同轴滚转 · 点上/下保倾偏航）· 贴地放�[...]
| [v1.2.5.1](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md#v1251) | 动力传感器（kinetic_gauge，Create 表同款 3 轴放置 · STRESS/RPM 节点 · 蓝屏[...]
| [v1.2.5](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md#v125) | 公式语言升级：控制流 + vec3 + 预算池 / GUI 架构迁移 / 像素编辑器 / 可编�[...]
| [v1.2.4.1](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md#v1241) | 回归审计 · 总线系统 · 封装状态 · 公式一致性 · Sable 加固 |
| [v1.2.4](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md#v124) | 多人协作 + 调试工具链 + 公式编辑器体验 |

更早版本（v1.2.3 及以前）见 [`CHANGELOG.md`](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md)。
Older releases (v1.2.3 and earlier): see [`CHANGELOG.md`](https://github.com/y15173334444/create-schematic-compute/blob/main/CHANGELOG.md).

---

<a id="faq--常见问题"></a>
## ❓ FAQ / 常见问题

<details>
<summary><b>Node editor laggy? / 节点编辑器卡顿？</b></summary>
Editor drawing is viewport-culled (v1.2.5.2): only on-screen nodes cost render time. 1024 nodes on screen runs fine — node count and PID count are no longer practical limits. / v1.2.5.2 起编辑器[...]
</details>

<details>
<summary><b>Speed Proxy not working? / 转速代理不工作？</b></summary>
Place directly adjacent to a Speed Controller. / 放置在转速控制器相邻面。
</details>

<details>
<summary><b>State lost after schematic? / 蓝图放置后状态丢失？</b></summary>
Use Create 6.0.10+. Full NBT interfaces registered. / 确保使用Create 6.0.10+。
</details>

<details>
<summary><b>Can computers communicate? / 计算机可以通信吗？</b></summary>
Yes — Private Signal I/O (named channels) or BUS_IN/BUS_OUT (banded). / 可以——私有信号I/O或BUS总线。
</details>

---

<a id="installation--安装"></a>
## 📥 Installation / 安装

| Dependency / 依赖 | Version / 版本 |
|------------------|---------------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233+ |
| Create | 6.0.10+ |

*Sable is optional / Sable 为可选*

1. Install NeoForge + Create / 安装 NeoForge + Create
2. Place `.jar` in `mods` folder / 将 `.jar` 放入 `mods`
3. Launch! / 启动！

---

<a id="links--链接"></a>
## 🌐 Links / 链接

- **GitHub**: [github.com/y15173334444/create-schematic-compute](https://github.com/y15173334444/create-schematic-compute)
- **Modrinth**: [modrinth.com/mod/create-schematic-compute](https://modrinth.com/mod/create-schematic-compute)
- **License / 许可证**: MIT © 2026 StarryNight_Luo

<p align="center">
  <b>⭐ If you enjoy this mod, star us on GitHub! / 喜欢请在GitHub点⭐！</b><br>
  <i>Unleash Create's potential with visual programming! / 用可视化编程释放机械动力的潜力！🚀</i>
</p>
