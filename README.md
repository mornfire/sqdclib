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
    implementation "com.mornfire:sqdclib:1.0.0"
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
```

后四个通过 `StoryBridge` 转发给对应模块 没装对应实现时是空操作 不会炸

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
| `on_start` | 空 | 接取时执行 |
| `on_complete` | 空 | 完成时执行 在写入完成状态之后 |

有个地方值得单独说 **目标没有 已完成 这个状态** 达成与否完全由 `done` 条件实时求值 这样条件和进度不可能对不上 不会出现 条件早就满足了但界面还显示未完成 的情况 代价是每 20 tick 要重新求值一遍 任务量大时可以考虑改成事件驱动

`objectives` 为空的任务不会自动完成 得靠 `complete_quest` 或者代码显式结束

状态只有四种

```
LOCKED ──前置全完成──► AVAILABLE ──start()──► ACTIVE ──目标全达成──► COMPLETED
```

```java
Quests.status(player, questId);        // LOCKED / AVAILABLE / ACTIVE / COMPLETED
Quests.definition(player, questId);    // Optional<QuestDefinition>
Quests.isVisible(player, questId);     // 该不该在任务书里显示
Quests.hasPrerequisites(player, def);
Quests.isReady(player, def);           // 目标是否全达成
Quests.start(player, questId);         // 只在 AVAILABLE 时生效
Quests.complete(player, questId);      // 只在 ACTIVE 时生效
Quests.refresh(player);                // 手动跑一轮检查 返回本轮完成的任务
```

```java
@SubscribeEvent
public static void onQuestDone(QuestCompletedEvent event) {
    ServerPlayer player = event.getPlayer();
    ResourceLocation quest = event.getQuest();
}
```

`QuestStartedEvent` 和它都继承自 `QuestEvent`

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

节点字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `speaker` | 无 | 留空就不显示名字行 |
| `text` | 必填 | 按对话框宽度自动换行 |
| `next` | 无 | 没有可用选项时自动跳这里 |
| `options` | 空 | 分支选项 |
| `actions` | 空 | 进入节点时执行 |

选项字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `text` | 必填 | |
| `condition` | 无 | 不满足时**根本不会发给客户端** |
| `next` | 无 | 留空表示选完就结束 |
| `actions` | 空 | 选择后执行 |

对话是服务端权威的 服务端持有会话 负责过滤选项 执行动作 推进节点 客户端只负责画和回传下标 **条件判断全在服务端做** 客户端连不满足条件的选项长什么样都不知道 也就无从伪造

推进规则没什么花样 过滤完还有选项就显示 没选项但有 `next` 就自动跳 最多连跳 64 次 防 JSON 写出死循环 两个都没有就结束 对话期间游戏不暂停 玩家可以按 Esc 主动退出

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
| `camera` | 空 | 留空就不接管相机 |
| `events` | 空 | 时间轴事件 |

相机关键帧里 `yaw` `pitch` `roll` `fov` 都可省 `fov` 默认 70 其余默认 0 缓动可填 `linear` `ease_in` `ease_out` `ease_in_out` 作用于**本帧到下一帧**这一段 `yaw` 走最短路径插值 所以 350 度到 10 度是往前转 20 度 不会倒着绕一大圈

时间轴事件里 `duration` 默认 40 tick 指的是文字停留多久 `fade` 的 `ticks` 是过渡时长

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
| `/story quest <ID> [start\|complete]` | 查询 接取或完成任务 |
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

## 取舍和坑

**`play_cutscene` 不阻塞** 演出播着的时候对话会话还开着 想要 演完接着聊 得自己轮询 `Cutscenes.isPlaying`

**客户端改不了世界状态** 现在会把整个状态广播回客户端 一旦客户端也能写就会来回打架 如果需要 玩家选择了改变世界 这种设计 得补一个客户端到服务端的包 目前没做

**演出没有音频层** 想要音效就在 `events` 里用 `command` 动作执行 `/playsound` 够用

**相机接管用了 Mixin** 和其他修改相机位置或视角的模组可能互相覆盖 谁生效看加载顺序 只改角度的模组不受影响

**对话不落盘** 玩家掉线就丢 重新上线要重新触发 对话本来就是瞬时状态 存下来反而要在意 重连后该恢复到哪一句

**任务每 20 tick 检查一次** 依赖计数器的任务完成最多有 1 秒延迟 需要即时反馈就直接调 `Quests.refresh(player)`

---

## 许可

MIT  完整条款见仓库根目录的 LICENSE 文件
