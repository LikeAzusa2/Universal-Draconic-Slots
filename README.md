# Universal Draconic Slots

Additional modular host support for **Draconic Evolution**.

Universal Draconic Slots is an **unofficial addon mod** for Draconic Evolution.
It allows compatible armor, tools, and weapons to be upgraded through a machine,
gain a Draconic Evolution module network, and optionally expose an independent OP interface.

---

## English

### Introduction

This mod is designed for players who want to extend the DE module system to
non-native equipment without directly replacing the original item.

The current design is based on these principles:

- Keep the original item as the original item
- Add the module host through item upgrade data
- Keep `OP` and other power systems separate
- Reuse Draconic Evolution logic where possible
- Bridge missing DE behavior only where necessary

This project is **not an official part of Draconic Evolution**.

### Main Features

- Add a DE module host to upgraded items instead of all items by default
- Upgrade equipment through the `Host Forge`
- Unlock host size with tier-specific materials
- Add an optional independent `OP` capability to upgraded items
- Support upgraded armor, melee weapons, mining tools, bows, and crossbows
- Support durability repair with OP
- Provide an optional left-side energy HUD for upgraded equipment
- Reuse parts of DE shield, HUD, and armor behavior through compatibility bridges

### Requirements

- Minecraft **1.21.1**
- **NeoForge 21.1.72**
- **Draconic Evolution 3.1.4.632**
- **Brandon's Core 3.2.1.307**
- **CodeChicken Lib 4.6.1.524**
- **Curios 9.5.1+1.21.1**

### Usage

#### 1. Upgrade items

Use `Host Forge` to upgrade a target item.

Basic slot layout:

- Center: target armor / tool / weapon
- Top: tech core
- Left: width expansion material
- Right: height expansion material
- Bottom: matching energy core

Different tech tiers limit max width and height, and require different materials.

#### 2. OP interface

Upgraded items can gain an independent `OP` capability.

Design rules:

- `OP` is used for DE-related module logic
- Existing power systems on the original item are not actively replaced
- `OP` and `FE` are intended to stay separate by default

#### 3. Durability repair

If enabled in config, upgraded damageable items will consume their own `OP`
to repair durability automatically.

#### 4. Energy HUD

The left-side HUD can show upgraded item icons and their current energy percentage.

- Default visibility can be changed in client config
- Visibility can also be toggled with a keybind

Default key:

- `H`: Toggle Energy HUD

### Configuration

Current config options include:

- Maximum grid size for Wyvern tier
- Maximum grid size for Draconic tier
- Maximum grid size for Chaotic tier
- Whether upgraded items repair durability with OP
- Whether the energy HUD is shown by default on the client

### Compatibility Notes

- This mod is designed around **bridging** DE behavior, not replacing it completely
- Compatibility with heavily customized equipment mods is not guaranteed
- Some visual behavior depends on how other mods render armor or equipped items
- Small DE version updates may change internal methods and require compatibility fixes

If you use this mod in a modpack, please test:

- Shield behavior
- Flight behavior
- Weapon/tool module effects
- Curios interaction
- Custom armor rendering

### Backup Warning

Before adding this mod to an existing world or updating to a newer version,
it is strongly recommended to **back up your save files first**.

### Support Statement

Issues can be used for bug reports and feedback, but please note:

- Updates are **not guaranteed**
- Fixes are **not guaranteed**
- Feature requests may be declined
- Long-term maintenance is **not promised**

Please include:

- Minecraft version
- NeoForge version
- Draconic Evolution version
- Other related mods
- `latest.log`
- crash report if available

Reports without logs may be difficult to investigate.

### Disclaimer

This project is not affiliated with or endorsed by:

- Draconic Evolution
- Brandon's Core
- Curios API

This is an unofficial addon project.

### License

This project is licensed under the **MIT License**.

See [LICENSE.md](./LICENSE.md) for details.

---

## 中文

### 简介

这是一个 **非官方的龙之研究附属模组**。

它的目标是让一部分原本不属于 DE 原生模块宿主体系的护甲、工具和武器，
在经过改造后也能获得：

- 龙之研究模块网络
- 模块安装能力
- 独立的 `OP` 接口
- 尽可能接近原版 DE 的部分行为与显示效果

本模组不会直接把原物品替换成 DE 原生物品，而是尽量通过“改造数据 + 兼容桥接逻辑”的方式扩展原物品。

### 主要功能

- 默认不会给所有装备强制附加模块宿主，只有经过改造的物品才会启用模块网络
- 提供机器 `Host Forge / 宿主锻造台` 用于改造装备
- 通过不同等级核心与材料扩展宿主宽高
- 可选为改造后的物品附加独立的 `OP` 接口
- 支持护甲、近战武器、工具、弓、弩等改造
- 支持消耗 OP 自动修复耐久
- 提供可开关的左侧能量 HUD
- 通过兼容桥接尽量复用 DE 的护盾、HUD、护甲和模块相关逻辑

### 依赖

- Minecraft **1.21.1**
- **NeoForge 21.1.72**
- **Draconic Evolution 3.1.4.632**
- **Brandon's Core 3.2.1.307**
- **CodeChicken Lib 4.6.1.524**
- **Curios 9.5.1+1.21.1**

### 使用说明

#### 1. 改造物品

使用 `Host Forge / 宿主锻造台` 改造目标物品。

基础槽位结构如下：

- 中央槽位：目标护甲 / 工具 / 武器
- 上方槽位：科技核心
- 左侧槽位：扩展宽度材料
- 右侧槽位：扩展高度材料
- 下方槽位：对应等级的能量核心

不同等级核心会限制最大宽高，并要求不同材料。

#### 2. OP 接口

改造后的物品可以获得独立的 `OP` capability。

设计原则如下：

- `OP` 主要用于 DE 相关模块逻辑
- 不主动覆盖原物品已有的其他能量系统
- `OP` 与 `FE` 默认分离

#### 3. 耐久修复

如果配置开启，已改造且有耐久的物品会消耗自身 `OP` 自动修复耐久。

#### 4. 能量 HUD

左侧 HUD 可以显示已改造物品的小图标与当前能量百分比。

- 默认显示状态可在客户端配置中调整
- 也可以通过按键随时开关

默认按键：

- `H`：切换能量 HUD 显示

### 配置项

当前配置主要包括：

- 飞龙等级最大网格尺寸
- 神龙等级最大网格尺寸
- 混沌等级最大网格尺寸
- 是否启用消耗 OP 自动修复耐久
- 客户端是否默认显示左侧能量 HUD

### 兼容性说明

- 本模组的核心思路是“桥接”DE 逻辑，而不是完整重做一套 DE
- 对高度自定义渲染或特殊装备系统的模组，兼容性不保证完全一致
- 一些视觉效果会受到其他模组护甲渲染方式影响
- DE 小版本更新可能修改内部方法签名，届时可能需要额外适配

如果你把本模组加入整合包，建议重点测试：

- 护盾功能
- 飞行功能
- 工具 / 武器模块效果
- Curios 交互
- 自定义护甲渲染

### 备份提醒

在把本模组加入已有存档，或者更新到新版本之前，强烈建议先备份世界存档。

### 支持说明

欢迎通过 Issue 反馈问题，但请注意：

- 不保证持续更新
- 不保证一定修复问题
- 功能请求可能会被拒绝
- 不承诺长期维护

建议反馈时附带：

- Minecraft 版本
- NeoForge 版本
- 龙之研究版本
- 相关联动模组
- `latest.log`
- 如有崩溃请附上 crash report

没有日志的问题通常很难排查。

### 免责声明

本项目与以下模组及其作者没有官方隶属或背书关系：

- Draconic Evolution
- Brandon's Core
- Curios API

这是一个非官方附属项目。

### 许可证

本项目基于 **MIT License** 开源。

详细内容见 [LICENSE.md](./LICENSE.md)。
