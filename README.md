# SQDCLib

Minecraft 1.21.1   NeoForge 21.1.219+   Java 21

冒险模组里真正磨人的从来不是玩法 是那些绕不开的配角工作 状态存哪 任务怎么推进 对话怎么分支 演出怎么播 每次开新坑都得重写一遍 写到一半就发现每个模组都长出了一套自己的半成品框架

SQDCLib 把这四件事抽出来做成一套前置依赖 四个模块彼此解耦 只用其中一两个也没问题 没有第三方内容模组依赖 全部走 NeoForge 官方 API

作者 Mornfire

---

## 接入

```bash
./gradlew build
```

产物是 `build/libs/sqdclib-1.0.0.jar` 需要 JDK 21 首次构建会下载并反编译 Minecraft 与 NeoForge 等几分钟

放进 `libs/` 后

```gradle
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    implementation "com.mornfire:sqdclib:1.0.2"
}
```

然后在自己的 `neoforge.mods.toml` 里声明依赖

```toml
[[dependencies.your_mod]]
    modId="sqdclib"
    type="required"
    versionRange="[1.0.0,)"
    ordering="AFTER"
    side="BOTH"
```

---

## 四个模块

```
com.mornfire.sqdclib
├── storystate        状态层 唯一的事实来源
│   ├── StoryProgress     玩家级状态 AttachmentType
│   ├── WorldState        世界级状态 SavedData
│   ├── Story             跨作用域查询门面
│   ├── condition/        条件表达式
│   ├── action/           动作与跨层桥
│   ├── event/            变更事件
│   └── net/              世界状态同步
├── quest             任务层 数据包定义 + 状态机
├── dialogue          对话层 服务端权威会话 + 客户端界面
└── cutscene          演出层 关键帧相机 + 时间轴 + 屏幕覆盖
```

分层不是为了让目录好看 核心考虑是**条件表达式必须只有一份** 任务目标 对话选项 演出分支判断的其实是同一批东西 flag 计数 阶段 任务完成度 如果条件系统归任务层 对话层和演出层就得反过来依赖任务层 那后面每加一层都要重新排依赖 所以条件放在状态层 上面三层共用一个求值器 而且它在客户端也能跑 玩家的状态是自动同步过去的 世界状态有专门的镜像包

层与层之间的调用走 `StoryBridge` 回调 状态层里没有任何指向另外三层的 import 这一点下面 跨层组合 会展开

---

## 状态层

### 玩家状态

每个玩家一份 挂在实体上 写操作会自动同步给本人 死亡后保留 靠 `AttachmentType` 的 `copyOnDeath`

```java
StoryProgress p = StoryProgress.of(player);

p.setFlag("relic_found");              // 返回是否真的发生了变化
p.setFlag("gate_open", false);         // 清除
p.hasFlag("relic_found");
p.getFlag("relic_found");              // 和 hasFlag 同义 写条件表达式时顺手些

p.getCounter("shards");                // 缺省 0
p.setCounter("shards", 3);             // 返回旧值
p.addCounter("shards", 1);             // 返回新值

p.completeQuest(ResourceLocation.parse("sqdclib:prologue"));
p.isQuestCompleted(ResourceLocation.parse("sqdclib:prologue"));
p.revokeQuest(...);

p.isQuestActive(...);
p.activateQuest(...);
p.deactivateQuest(...);

p.getFlags();                          // 只读视图
p.getCompletedQuests();
p.getActiveQuests();
```

每次写入都会把**整个状态**推给该玩家客户端 且只推给本人 叙事 flag 是私有数据 广播给周围的人既没必要也不合适 flag 变化时还会在 `NeoForge.EVENT_BUS` 上发 `StoryFlagChangedEvent`

### 世界状态

每个存档一份 物理位置是主世界的 `data/sqdclib_story_state.dat` 因为挂在主世界上 所以**所有维度天然共享同一份** 不需要额外做维度间的同步

```java
WorldState s = WorldState.get(player.level());   // 服务端和客户端都能这么拿
WorldState s = WorldState.get(minecraftServer);  // 只有 server 的场合

s.setFlag("gate_open");
s.hasFlag("gate_open");

s.getPhase();                     // 空字符串表示还没设置过
s.setPhase("chapter1");           // 返回是否变化

s.getFlags();
s.isServerSide();                 // 客户端镜像返回 false
```

客户端拿到的是一份只读镜像 在玩家登录 切换维度 以及世界状态每次变化时推送 **在客户端调用任何写方法会抛 `UnsupportedOperationException`** 这是故意的 让客户端误改本地镜像 只会得到界面和服务端不一致这种最难查的 bug

### 统一查询

条件判断经常不关心某个 flag 到底存在哪一边 这时候用 `Story`

```java
Story.hasFlag(player, "relic_found");                    // 先查玩家 没有再查世界
Story.hasFlag(player, FlagScope.PLAYER, "relic_found");  // 只查玩家
Story.hasFlag(player, FlagScope.WORLD, "gate_open");     // 只查世界

Story.setFlag(player, "met_elder");
Story.clearFlag(player, "met_elder");

Story.getCounter(player, "shards");
Story.addCounter(player, "shards", 1);
Story.setCounter(player, "shards", 3);

Story.completeQuest(player, ResourceLocation.parse("sqdclib:prologue"));
Story.isQuestCompleted(player, ResourceLocation.parse("sqdclib:prologue"));
```

### 事件

```java
@SubscribeEvent
public static void onFlag(StoryFlagChangedEvent event) {
    if (event.getScope() == FlagScope.PLAYER && event.getNewValue()) {
        Player player = event.getPlayer();
        String flag = event.getFlag();
    }
}

@SubscribeEvent
public static void onPhase(StoryPhaseChangedEvent event) {
    String from = event.getOldPhase();
    String to   = event.getNewPhase();
}
```

---

## 条件表达式

七个类型 都能嵌套 JSON 里靠 `type` 字段区分

```json
{ "type": "flag", "flag": "relic_found" }
{ "type": "flag", "flag": "gate_open", "scope": "world" }
{ "type": "flag", "flag": "betrayed", "value": false }
```

`flag` 的 `scope` 可填 `player` `world` `any` 默认 `any` `value` 默认 `true` 填 `false` 就是 未设置

```json
{ "type": "quest", "quest": "sqdclib:prologue" }
{ "type": "counter", "key": "shards", "at_least": 3 }
{ "type": "phase", "phase": "chapter1" }
{ "type": "not", "value": { "type": "flag", "flag": "gate_open" } }
```

`at_least` 默认 1 逻辑组合用 `all` 和 `any`

```json
{
  "type": "all",
  "values": [
    { "type": "flag", "flag": "met_elder" },
    { "type": "counter", "key": "shards", "at_least": 3 },
    {
      "type": "any",
      "values": [
        { "type": "quest", "quest": "sqdclib:first_relic" },
        { "type": "phase", "phase": "chapter2" }
      ]
    }
  ]
}
```

---

## 动作

同样带 `type` 字段 可以写在任务 对话 演出的任意位置

写状态

```json
{ "type": "set_flag", "flag": "met_elder" }
{ "type": "set_flag", "flag": "gate_open", "value": true, "scope": "world" }
{ "type": "add_counter", "key": "shards", "amount": 1 }
{ "type": "set_phase", "phase": "chapter1" }
```

`set_flag` 的 `scope` 默认 `player`

以玩家身份执行命令

```json
{ "type": "command", "command": "give @s minecraft:bread 4" }
```

驱动流程

```json
{ "type": "start_quest", "quest": "sqdclib:first_relic" }
{ "type": "complete_quest", "quest": "sqdclib:first_relic" }
{ "type": "start_dialogue", "dialogue": "sqdclib:elder_greeting" }
{ "type": "play_cutscene", "cutscene": "sqdclib:relic_reveal" }
{ "type": "play_cutscene", "cutscene": "sqdclib:relic_reveal", "await": true }
```

后四个通过 `StoryBridge` 转发给对应模块 没装对应实现时是空操作 不会炸

`play_cutscene` 默认不阻塞 播完之前后面的动作照常执行 加了 `await` 之后这个动作会挂起整条动作链 演出结束后从下一条继续 写对话时用它可以做到 说完这句演一段再回到对话 详见下面的演出章节

---

## 任务

定义放在 `data/<命名空间>/story_quest/<任务名>.json`

```json
{
  "title": { "text": "第一件遗物" },
  "description": { "text": "收集三块散落的碎片" },
  "kind": "main",
  "prerequisites": ["sqdclib:prologue"],
  "visible_when": { "type": "phase", "phase": "chapter1" },
  "objectives": [
    {
      "id": "collect",
      "title": { "text": "收集 3 块碎片" },
      "done": { "type": "counter", "key": "shards", "at_least": 3 }
    }
  ],
  "on_start": [
    { "type": "set_flag", "flag": "relic_hunt_started" }
  ],
  "on_complete": [
    { "type": "set_flag", "flag": "relic_found" },
    { "type": "play_cutscene", "cutscene": "sqdclib:relic_reveal" }
  ]
}
```

| 字段 | 默认 | 说明 |
|---|---|---|
| `title` | 必填 | 任务标题文本 |
| `description` | 无 | 任务描述 |
| `kind` | `main` | `main` 或 `side` |
| `prerequisites` | 空 | 全部完成后本任务才可接取 |
| `visible_when` | 无 | 是否在任务书里显示 和能否接取是两回事 |
| `objectives` | 空 | 全部达成就自动完成 |
| `fail_when` | 无 | 满足即判定失败 和 objectives 一样每次检查时实时求值 |
| `on_start` | 空 | 接取时执行 |
| `on_complete` | 空 | 完成时执行 在写入完成状态之后 |
| `on_fail` | 空 | 失败时执行 在写入失败状态之后 |

每个目标自己的字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `id` | 必填 | 目标标识 |
| `title` | 必填 | 显示文本 |
| `done` | 必填 | 达成条件 |
| `progress` | 无 | 进度来源 `{ "counter": "shards", "max": 5 }` |

`progress` 只影响显示 不影响判定 判定永远看 `done` 想给玩家看 3/5 就在目标上写它 拿渲染结果读 `Quests.objectiveTitle(player, objective)` 没写 `progress` 的目标返回不带括号的原标题

有个地方值得单独说 **目标没有 已完成 这个状态** 达成与否完全由 `done` 条件实时求值 这样条件和进度不可能对不上 不会出现 条件早就满足了但界面还显示未完成 的情况 代价是每 20 tick 要重新求值一遍 任务量大时可以考虑改成事件驱动

`objectives` 为空的任务不会自动完成 得靠 `complete_quest` 或者代码显式结束

状态只有五种

```
LOCKED ──前置全完成──► AVAILABLE ──start()──► ACTIVE ──目标全达成──► COMPLETED
                          ▲                     │
                          └──── abandon() ──────┤
                                                └── fail_when 满足 ──► FAILED
```

`FAILED` 是终态 不会再被 refresh 检查 也不能直接 start 想重来要先 `reset` `abandon` 不一样 它只是放下 前置满足的话任务立刻回到 AVAILABLE

```java
Quests.status(player, questId);        // LOCKED / AVAILABLE / ACTIVE / COMPLETED / FAILED
Quests.definition(player, questId);    // Optional<QuestDefinition>
Quests.isVisible(player, questId);     // 该不该在任务书里显示
Quests.hasPrerequisites(player, def);
Quests.isReady(player, def);           // 目标是否全达成
Quests.isFailed(player, def);          // fail_when 是否满足
Quests.start(player, questId);         // 只在 AVAILABLE 时生效
Quests.complete(player, questId);      // 只在 ACTIVE 时生效
Quests.fail(player, questId);          // 只在 ACTIVE 时生效
Quests.abandon(player, questId);       // 放弃 回到可接取
Quests.reset(player, questId);         // 清掉完成和失败记录 从头再来
Quests.refresh(player);                // 手动跑一轮检查 返回本轮状态有变的任务
```

```java
@SubscribeEvent
public static void onQuestDone(QuestCompletedEvent event) {
    ServerPlayer player = event.getPlayer();
    ResourceLocation quest = event.getQuest();
}
```

`QuestStartedEvent` `QuestCompletedEvent` `QuestFailedEvent` 都继承自 `QuestEvent`

---

## 对话

定义放在 `data/<命名空间>/story_dialogue/<对话名>.json`

```json
{
  "start": "greet",
  "nodes": {
    "greet": {
      "speaker": { "text": "长老" },
      "text": { "text": "旅人 你终于醒了" },
      "options": [
        { "text": { "text": "我在哪" }, "next": "where" },
        { "text": { "text": "沉默" }, "next": "silence" },
        {
          "text": { "text": "我来找遗物" },
          "condition": { "type": "flag", "flag": "relic_hunt_started" },
          "next": "relic"
        }
      ]
    },
    "task": {
      "speaker": { "text": "长老" },
      "text": { "text": "去收集三块碎片" },
      "actions": [
        { "type": "set_flag", "flag": "met_elder" },
        { "type": "start_quest", "quest": "sqdclib:first_relic" }
      ],
      "options": [
        { "text": { "text": "明白了" }, "next": "farewell" }
      ]
    },
    "farewell": {
      "speaker": { "text": "长老" },
      "text": { "text": "愿星光指引你" }
    }
  }
}
```

对话定义有三个顶层字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `start` | 必填 | 起始节点 ID |
| `lock_movement` | `false` | 对话期间是否锁住移动 跳跃 潜行 |
| `nodes` | 必填 | 节点表 |

节点字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `speaker` | 无 | 留空就不显示名字行 |
| `text` | 必填 | 按对话框宽度自动换行 |
| `next` | 无 | 没有可用选项时自动跳这里 |
| `options` | 空 | 分支选项 |
| `actions` | 空 | 进入节点时执行 |
| `continue_text` | 无 | 填了就变成等玩家点击的节点 见下 |

选项字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `text` | 必填 | |
| `condition` | 无 | 不满足时**根本不会发给客户端** |
| `next` | 无 | 留空表示选完就结束 |
| `actions` | 空 | 选择后执行 |

对话是服务端权威的 服务端持有会话 负责过滤选项 执行动作 推进节点 客户端只负责画和回传下标 **条件判断全在服务端做** 客户端连不满足条件的选项长什么样都不知道 也就无从伪造

推进规则没什么花样 过滤完还有选项就显示 没选项但有 `next` 就自动跳 最多连跳 64 次 防 JSON 写出死循环 两个都没有就结束 对话期间游戏不暂停 玩家可以按 Esc 主动退出

只有一句台词想让玩家看完再点一下 就在节点上写 `continue_text` 它会在没有可用选项时变成一个按钮 按下去走 `next` 这是对话里最常用的一种节点

```json
{
  "speaker": { "text": "长老" },
  "text": { "text": "……你听见了吗" },
  "continue_text": { "text": "继续" },
  "next": "after_noise"
}
```

不写 `continue_text` 的话 `next` 是自动跳的 两种写法别混 要玩家停顿就用前者 要让流程自己往前走就用后者

节点和选项的 actions 里如果写了 `play_cutscene` 加 `await` 对话会停在那句 等演出播完再往下走 用来做 说完这段演一段再回来 的场景 挂起期间玩家也不能操作

对话位置是落盘的 玩家掉线时服务端记住停在哪一句 重新上线会自动接着聊 主动按 Esc 关掉则视为结束 断点一并清掉 下次从头开始

```java
Dialogues.start(player, dialogueId);
Dialogues.select(player, index);       // 客户端界面回传的下标
Dialogues.stop(player);
Dialogues.isTalking(player);
Dialogues.currentDialogue(player);
Dialogues.availableOptions(player, node);
Dialogues.definition(player, dialogueId);
```

`Dialogues.CLOSE_INDEX` 是 `-1` 作为 请求关闭 的下标

---

## 演出

定义放在 `data/<命名空间>/story_cutscene/<演出名>.json`

```json
{
  "duration": 160,
  "lock_movement": true,
  "letterbox": true,
  "camera": [
    {
      "time": 0,
      "position": [0.5, 66.0, 0.5],
      "yaw": 0.0,
      "pitch": 10.0,
      "fov": 70.0,
      "easing": "linear"
    },
    {
      "time": 60,
      "position": [4.0, 68.5, -6.0],
      "yaw": 140.0,
      "pitch": -12.0,
      "fov": 58.0,
      "easing": "ease_in_out"
    },
    {
      "time": 145,
      "position": [0.5, 66.0, -1.5],
      "yaw": 0.0,
      "pitch": 0.0,
      "fov": 70.0,
      "easing": "ease_in_out"
    }
  ],
  "events": [
    { "time": 1, "fade": { "to": 1.0, "ticks": 1 } },
    { "time": 3, "fade": { "to": 0.0, "ticks": 20 } },
    { "time": 25, "text": { "text": "碎片在你掌中苏醒" }, "duration": 55 },
    { "time": 90, "text": { "text": "它指向了北方" }, "duration": 45 },
    { "time": 150, "fade": { "to": 1.0, "ticks": 10 } },
    { "time": 155, "actions": [ { "type": "set_flag", "flag": "relic_revealed" } ] }
  ]
}
```

时间单位一律是 tick 20 tick 一秒

| 顶层字段 | 默认 | 说明 |
|---|---|---|
| `duration` | 必填 | 总时长 |
| `lock_movement` | `true` | 锁移动 跳跃 潜行 |
| `letterbox` | `true` | 上下黑边 |
| `skippable` | `false` | 允许按 Esc 跳过 |
| `camera` | 空 | 留空就不接管相机 |
| `events` | 空 | 时间轴事件 |
| `credits` | 无 | 报幕 见下面单独一节 |

`skippable` 打开之后 演出期间按 Esc 是跳过而不是暂停 跳过会把还没触发的 `actions` 按顺序全部补齐执行 再走正常的结束流程 所以挂在它后面的 `await` 对话照样会继续 奖励也不会因为跳过而丢

没开 `skippable` 的演出按 Esc 就是普通的暂停菜单 关键剧情建议保持默认 报幕那种长段落可以打开

相机关键帧里 `yaw` `pitch` `roll` `fov` 都可省 `fov` 默认 70 其余默认 0 缓动可填 `linear` `ease_in` `ease_out` `ease_in_out` 作用于**本帧到下一帧**这一段 `yaw` 走最短路径插值 所以 350 度到 10 度是往前转 20 度 不会倒着绕一大圈

时间轴事件里 `duration` 默认 40 tick 指的是文字停留多久 `fade` 的 `ticks` 是过渡时长

`sound` 写一个音效 ID 加可选音量和音高 它和文字 淡入淡出一样跑在客户端时钟上 三者的触发时机是一帧对齐的 所以能卡点 不像用 `command` 执行 playsound 那样受命令系统调度 最多会差 1 tick

### 报幕

演出可以带一段从下往上滚的演职员表 时间轴照常跑 报幕在自己的时间段里滚

```json
"credits": {
  "start": 175,
  "duration": 500,
  "speed_up": true,
  "lines": [
    { "text": "SQDCLib", "scale": 2.0, "gap": 30, "color": 16764057 },
    { "text": "叙事前置库", "gap": 40 },
    { "text": "感谢游玩", "scale": 1.5, "gap": 0, "color": 16764057 }
  ]
}
```

| 字段 | 默认 | 说明 |
|---|---|---|
| `start` | `0` | 从第几 tick 开始滚 |
| `duration` | 必填 | 滚完全程用多少 tick |
| `speed_up` | `true` | 先慢后快 长名单不至于拖太久 |
| `lines` | 空 | 每一行的内容 |

每行自己的字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `text` | 必填 | 文本组件 |
| `scale` | `1.0` | 字号倍数 |
| `gap` | `8` | 与下一行的间距 单位是像素 |
| `color` | 白色 | RGB 整数 16764057 是浅金色 11184810 是灰 |

报幕画在淡入淡出之上 想做成电影那种黑底白字 就在 events 里先 fade 到 1 字会盖在黑底上面

```java
Cutscenes.play(player, cutsceneId);
Cutscenes.stop(player);
Cutscenes.isPlaying(player);
Cutscenes.currentCutscene(player);
Cutscenes.definition(player, cutsceneId);
```

`play` 不阻塞 调用完立刻返回 想知道演完没有 轮询 `isPlaying`

时间轴两端各跑各的 服务端按 tick 触发 `actions` 客户端按 tick 加部分帧触发文字和淡入淡出 两端不共享时钟 动作和画面之间最多差 1 tick 这是有意的取舍 换成同步时钟得为一次演出额外维护往返状态 不划算

相机接管是这里唯一一处 Mixin 原因很直接 NeoForge 的 `ViewportEvent.ComputeCameraAngles` 触发在 `Camera.setup()` 的**第一行** 紧接着 `setRotation` 和 `setPosition` 就会把它覆盖掉 所以在那个事件里改位置是无效的 相机位置没有官方钩子 只能在 `setup()` 返回时注入 另外用 AccessTransformer 打开了 `Camera.setPosition` 和 `setRotation` 它们原本是 protected

---

## 数据包路径

```
data/<命名空间>/story_quest/<任务名>.json
data/<命名空间>/story_dialogue/<对话名>.json
data/<命名空间>/story_cutscene/<演出名>.json
```

三类都是同步注册表 定义会自动下发到客户端 ID 由路径决定 `data/mypack/story_quest/first_relic.json` 就是 `sqdclib:first_relic`

三个定义都可以写一个可选的 `schema` 字段 默认 1 当前也是 1 以后加了新字段会把版本提上去 旧版本遇到更高的 schema 会直接报错并跳过这个文件 而不是按缺省值解析出半个正确的结果 那种沉默的错最难查

文本字段可以写完整的原版文本组件

```json
{ "text": "普通文本" }
{ "translate": "mypack.dialogue.greeting" }
{ "text": "红色警告", "color": "red" }
```

用 `translate` 能让文案跟着语言文件走

---

## 调试命令

都要 OP 权限 内置三份示例数据 可以直接拿来冒烟测试

```
/story talk sqdclib:elder_greeting
/story counter shards 3
/story cutscene sqdclib:relic_reveal
```

| 命令 | 作用 |
|---|---|
| `/story status` | 打印阶段 世界 flag 玩家 flag 进行中和已完成的任务 |
| `/story flag <名称>` | 切换玩家 flag |
| `/story worldflag <名称>` | 切换世界 flag |
| `/story counter <键> <数量>` | 增减计数器 |
| `/story phase [阶段]` | 查询或设置剧情阶段 |
| `/story quest <ID> [start\|complete\|fail\|abandon\|reset]` | 查询或改变任务状态 |
| `/story quest <ID> why` | 逐条打印目标与失败条件为什么没过 |
| `/story talk <ID>` | 开始对话 |
| `/story cutscene <ID>` | 播放演出 |

---

## 跨层组合

四个模块在 mod 构造阶段各自把实现注册进 `StoryBridge`

```java
StoryBridge.questStarter(Quests::start);
StoryBridge.questCompleter(Quests::complete);
StoryBridge.dialogueStarter(Dialogues::start);
StoryBridge.cutsceneStarter(Cutscenes::play);
```

于是这些组合都只是数据包里的一行 JSON

| 想要的效果 | 写在哪 |
|---|---|
| 对话中途插一段演出 | 对话节点或选项的 `actions` 里写 `play_cutscene` |
| 演出过半时开始对话 | 演出的 `events` 在指定时间点写 `start_dialogue` |
| 任务完成时推进剧情阶段 | 任务的 `on_complete` 里写 `set_phase` |
| 选项根据任务完成度显示 | 选项的 `condition` 里写 `quest` |
| 演出分支读取当前阶段 | 条件里写 `phase` |

想加自己的动作类型 需要自己注册数据类型走桥 只是想在代码里驱动流程的话 直接调 `Quests` `Dialogues` `Cutscenes` 的静态方法就行

---

## API 速查

状态层

```java
StoryProgress.of(player).setFlag("x")           // 返回是否真的变了
StoryProgress.of(player).setFlag("x", false)
StoryProgress.of(player).hasFlag("x")
StoryProgress.of(player).getCounter("k")
StoryProgress.of(player).setCounter("k", 3)     // 返回旧值
StoryProgress.of(player).addCounter("k", 1)     // 返回新值
StoryProgress.of(player).getFlags()
StoryProgress.of(player).getCompletedQuests()
StoryProgress.of(player).getActiveQuests()
StoryProgress.of(player).getFailedQuests()

WorldState.get(level)                           // 服务端与客户端都能这么拿
WorldState.get(server)
WorldState.get(level).setFlag("x")
WorldState.get(level).hasFlag("x")
WorldState.get(level).getPhase()
WorldState.get(level).setPhase("chapter1")
WorldState.get(level).isServerSide()

Story.hasFlag(player, "x")                      // 玩家优先 没有再查世界
Story.hasFlag(player, FlagScope.WORLD, "x")
Story.setFlag(player, "x")
Story.clearFlag(player, "x")
Story.getCounter(player, "k")
Story.setCounter(player, "k", 3)
Story.addCounter(player, "k", 1)
Story.completeQuest(player, id)
Story.isQuestCompleted(player, id)
```

任务层

```java
Quests.status(player, id)                       // LOCKED / AVAILABLE / ACTIVE / COMPLETED / FAILED
Quests.definition(player, id)                   // Optional<QuestDefinition>
Quests.isVisible(player, id)                    // 该不该在任务书里显示
Quests.hasPrerequisites(player, def)
Quests.isReady(player, def)                     // 目标是否全达成
Quests.isFailed(player, def)                    // fail_when 是否满足
Quests.start(player, id)
Quests.complete(player, id)
Quests.fail(player, id)
Quests.abandon(player, id)                      // 放下 回到可接取
Quests.reset(player, id)                        // 清掉完成与失败记录
Quests.refresh(player)                          // 手动跑一轮 返回状态有变的任务
Quests.objectiveProgress(player, objective)     // Optional<Component> 形如 3/5
Quests.objectiveTitle(player, objective)        // 标题带上进度
Quests.explain(player, id)                      // List<String> 逐条说明为什么没过
Quests.registry(player)
```

对话层

```java
Dialogues.start(player, id)
Dialogues.select(player, index)
Dialogues.stop(player)                          // 结束 清断点
Dialogues.suspend(player)                       // 保留断点 只清内存会话
Dialogues.resume(player)                        // 从断点接着聊
Dialogues.isTalking(player)
Dialogues.currentDialogue(player)
Dialogues.availableOptions(player, node)
Dialogues.definition(player, id)
```

演出层

```java
Cutscenes.play(player, id)
Cutscenes.play(player, id, onFinish)            // 演完执行回调
Cutscenes.playFor(players, id)                  // 给一组人播 返回开始了几场
Cutscenes.playNear(anchor, radius, id)          // 给附近的人播
Cutscenes.skip(player)                          // 补齐剩余 actions 后结束
Cutscenes.stop(player)
Cutscenes.isPlaying(player)
Cutscenes.isSkippable(player)
Cutscenes.currentCutscene(player)
Cutscenes.definition(player, id)
```

条件与动作 写代码时也能直接用

```java
condition.test(player)                          // 求值
condition.explain(player)                       // 人类可读的失败原因
StoryAction.runAll(player, actions)
StoryAction.runSequentially(player, actions, onComplete)   // 遇到 await 演出会挂起

StoryBridge.dialogueStarter(...)
StoryBridge.cutsceneStarter(...)
StoryBridge.questStarter(...)
StoryBridge.questCompleter(...)
```

---

## 多人

对话和演出都按玩家隔离 会话表键是玩家 UUID 两个玩家同时找同一个 NPC 各聊各的 互不干扰 同一个人身上也不会同时开两场 后来的顶掉先前的

世界状态是全局的 一个人改了剧情阶段 所有人立刻看到 玩家状态是私有的 只推给本人

想让一群人一起看同一场演出 用 `Cutscenes.playFor(players, id)` 按范围挑人就用 `playNear(anchor, radius, id)`

---

## 取舍和坑

**`play_cutscene` 默认不阻塞** 要等它演完再加 `await`

**客户端改不了世界状态** 现在会把整个状态广播回客户端 一旦客户端也能写就会来回打架 如果需要 玩家选择了改变世界 这种设计 得补一个客户端到服务端的包 目前没做

**演出音效走客户端播放** 这样才和画面同帧 代价是它不跟随世界坐标 要某个方块位置的立体声还得用 `command` 动作执行 playsound

**相机接管用了 Mixin** 和其他修改相机位置或视角的模组可能互相覆盖 谁生效看加载顺序 只改角度的模组不受影响

**对话断点只在掉线时保留** 主动按 Esc 关掉等于结束 想下次接着聊就别关

**任务每 20 tick 检查一次** 完成和失败判定都在这一轮里跑 依赖计数器的任务最多有 1 秒延迟 需要即时反馈就直接调 `Quests.refresh(player)`

---

## 许可

MIT  完整条款见仓库根目录的 LICENSE 文件
