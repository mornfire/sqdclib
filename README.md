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
    implementation "com.mornfire:sqdclib:2.1.2"
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

## 可选依赖

GeckoLib 是可选的 只有在你用 `animation` 事件时才需要 库本身不在编译期引用它的任何类 而是运行时反射查找 `software.bernie.geckolib.animatable.GeoAnimatable`

这意味着没装 GeckoLib 时 `animation` 事件是空操作 不会抛 `NoClassDefFoundError` 也不会让演出中断 装了就会正常触发

`neoforge.mods.toml` 里已经声明了软依赖 打包时不用额外处理

没引入 PlayerAnimator 它绑定在真玩家实体上 而替身需要的是"真玩家隐形加一个纯客户端模型" 两者模型对不上 替身需要的走位 转身 举手用 `ModelPart` 直接驱动就够 加进来只会多一层依赖

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
{ "type": "string", "key": "player_name", "equals": "Mornfire" }
{ "type": "string", "key": "player_name" }
{ "type": "not", "value": { "type": "flag", "flag": "gate_open" } }
```

`at_least` 默认 1 `string` 不写 `equals` 时判断这个变量非空 逻辑组合用 `all` 和 `any`

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

### 自定义动作

内置八种不够用时可以注册自己的类型

```java
StoryActions.register(
        ResourceLocation.fromNamespaceAndPath("ymondf", "avatar_reveal"),
        RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("tick").forGetter(AvatarReveal::tick))
                .apply(instance, AvatarReveal::new)));
```

自定义动作实现 `StoryAction` 接口 提供 `run(ServerPlayer)` 和 `type()` 然后 JSON 里写全名

```json
{ "type": "ymondf:avatar_reveal", "tick": 300 }
```

`type()` 返回的字符串要和注册时的资源位置完全一致 带冒号 不带冒号的按内置类型查找

注册要在 mod 构造阶段完成 早于任何数据包解析就行

签名收的是 `MapCodec` 不是 `Codec` 因为底层的 type 分发只能接 `MapCodec` 把 record 的 `mapCodec` 直接传进来即可 不需要额外包一层

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
| `mode` | `ambient` | `ambient` 或 `cinematic` 见下 |
| `lock_movement` | `false` | 对话期间是否锁住移动 跳跃 潜行 |
| `placement` | `bottom` | 对话框位置 `bottom` `center` `top` |
| `text_speed` | `0` | 每 tick 显示几个字 0 表示不做打字效果 |
| `speaker_color` | 浅金 | 说话人名字的 RGB 整数 |
| `nodes` | 必填 | 节点表 |

### 显示模式

`ambient` 是默认 半透明对话框浮在屏幕底部 玩家可以继续走动 选项用数字键 `1` 到 `9` 选择 按 Esc 退出 适合支线和日常交互

`cinematic` 会压暗屏幕 底部浮出一整条黑幕 黑幕里放头像 名字和台词 玩家不能移动 选项用鼠标点 适合剧情转折点 黑幕只占底部 中间留给演出画面

两种模式都会显示头像 前提是说话人写了 `portrait`

### 说话人与头像

`speaker` 可以是一个纯文本组件 也可以是一个带头像的对象

```json
"speaker": { "text": "长老" }
```

```json
"speaker": {
  "name": { "text": "长老" },
  "portrait": "sqdclib:elder"
}
```

`portrait` 是一个资源位置 贴图放在 `assets/<命名空间>/textures/gui/portrait/<路径>.png` 界面上按 48x48 绘制 建议贴图也用 48x48 尺寸不符会被拉伸

不写 `portrait` 就只显示名字 不占位置

节点字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `speaker` | 无 | 留空就不显示名字行 |
| `text` | 必填 | 按对话框宽度自动换行 |
| `next` | 无 | 没有可用选项时自动跳这里 |
| `options` | 空 | 分支选项 |
| `actions` | 空 | 进入节点时执行 |
| `continue_text` | 无 | 填了就变成等玩家点击的节点 见下 |
| `input` | 无 | 文本输入框 见下 |

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

### 输入框

节点上写 `input` 会在对话框里放一个文本输入框 玩家按回车提交 内容存成字符串变量 然后走 `next` 它和 `continue_text` 一样只在没有可用选项时出现

```json
{
  "speaker": { "text": "长老" },
  "text": { "text": "那么 我该怎么称呼你" },
  "input": {
    "key": "player_name",
    "max_length": 16,
    "placeholder": { "text": "输入你的名字" }
  },
  "next": "greet_named"
}
```

| 字段 | 默认 | 说明 |
|---|---|---|
| `key` | 必填 | 存到哪个字符串变量 |
| `max_length` | `32` | 服务端会按这个长度再截一次 不信任客户端 |
| `placeholder` | 无 | 输入框为空时的灰字提示 |

### 文本插值

任何显示给玩家的文本 用 `%变量名%` 就能取出字符串变量的值

```json
{ "text": "%player_name% 这个名字我记住了" }
```

节点正文 说话人名 选项文本都支持 变量不存在时替换成空串 所以不会看到裸露的百分号

### 可点击文字

正文里的文字可以直接用原版的文本组件样式 带 `click_event` 或 `hover_event` 的片段会变成可点区域 鼠标移上去变色并显示提示 点下去执行对应动作

```json
{
  "text": "详细的记载在这个",
  "extra": [
    {
      "text": "资料库",
      "color": "aqua",
      "underlined": true,
      "click_event": { "action": "open_url", "value": "https://example.com" },
      "hover_event": { "action": "show_text", "value": { "text": "蓝色文字可以点" } }
    },
    { "text": "里" }
  ]
}
```

动作类型就是原版那几种 `open_url` `run_command` `suggest_command` `copy_to_clipboard` `change_page` 这里不重复列举 执行走的是 Minecraft 自己的组件点击处理 所以行为和其他界面里的链接一致

`click_event` 只有打在文字片段上才有效 整段纯文本不会触发

### 界面

对话框是一块自适应高度的面板 正文按宽度自动换行 开启 `text_speed` 后逐字显示 打字过程中点一下或按空格立刻出全 选项和可点击文字都按各自的位置做命中检测

面板高度按**完整正文**计算 所以逐字显示时框不会越变越高

```java
Dialogues.start(player, dialogueId);
Dialogues.select(player, index);       // 客户端界面回传的下标
Dialogues.submit(player, text);        // 客户端输入框回传的内容
Dialogues.stop(player);                // 结束 清断点
Dialogues.suspend(player);             // 保留断点 只清内存会话
Dialogues.resume(player);              // 从断点接着聊
Dialogues.isTalking(player);
Dialogues.currentDialogue(player);
Dialogues.currentNode(player);
Dialogues.availableOptions(player, node);
Dialogues.definition(player, dialogueId);
```

`Dialogues.CLOSE_INDEX` 是 `-1` 作为请求关闭的下标

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
| `music` | 无 | 过场音轨 开始自动播 结束或跳过自动停 |

`skippable` 打开之后 演出期间按 Esc 是跳过而不是暂停 跳过会把还没触发的 `actions` 按顺序全部补齐执行 再走正常的结束流程 所以挂在它后面的 `await` 对话照样会继续 奖励也不会因为跳过而丢

没开 `skippable` 的演出按 Esc 就是普通的暂停菜单 关键剧情建议保持默认 报幕那种长段落可以打开

相机关键帧里 `yaw` `pitch` `roll` `fov` 都可省 `fov` 默认 70 其余默认 0 缓动可填 `linear` `ease_in` `ease_out` `ease_in_out` 作用于**本帧到下一帧**这一段 `yaw` 走最短路径插值 所以 350 度到 10 度是往前转 20 度 不会倒着绕一大圈

### 看向一个点

关键帧可以写 `look_at` 给一个世界坐标 相机会自动算出朝向 这时候 `yaw` 和 `pitch` 只作为退化情况下的缺省

```json
{
  "time": 60,
  "position": [4.0, 68.5, -6.0],
  "look_at": [0.5, 66.0, 0.5],
  "fov": 58.0,
  "easing": "ease_in_out"
}
```

目标会移动的时候这个比手算角度可靠 你只要关心注视点在哪 环绕角度由位置自动推出来

时间轴事件里 `duration` 默认 40 tick 指的是文字停留多久 `fade` 的 `ticks` 是过渡时长

除了文字和淡入淡出 一个时间轴事件还可以带 `sound` `animation` `player_stand_in` `qte` 四种子效果 各自的字段见下面每一节 同一个事件可以同时带多种 它们按 音效 动画 替身 QTE 默认动作 的顺序生效

`music` 写一个音效 ID 过场开始时循环播放 结束时停 被跳过时也停 用这个就不用在 tick 0 手写一个 `sound` 事件 也就不会出现跳过之后音乐还在响的情况

### 每 tick 回调

服务端每个 tick 会发一次 `CutsceneTickEvent` tick 是 `0` 到 `duration - 1`

```java
@SubscribeEvent
public static void onCutsceneTick(CutsceneTickEvent event) {
    ServerPlayer player = event.getPlayer();
    int tick = event.getTick();
    int duration = event.getDuration();
    float progress = duration <= 1 ? 1.0F : tick / (float) (duration - 1);
}
```

这个 tick 和 `CutsceneDefinition.duration` 是同一条时间轴 所以透明度 光环 平行对话都能卡在真实进度上 不需要自己再起一个计数器

注意是**服务端**事件 客户端要同步效果的话得自己发包

### 从指定 tick 开始

```java
Cutscenes.play(player, id, 400);   // 直接从第 400 tick 开始
Cutscenes.seek(player, 500);       // 播放中往后跳到 500
```

`play` 带起始 tick 时**之前的事件不会补执行** 它们被标记成已触发 直接跳过 `seek` 同理 只跳时间轴 不补动作

这样断线重连时可以跳过已经看过的前段 直接到对话和黑幕 而不是整段跳过

`seek` 只能往前 传一个更小的 tick 会返回 false 需要回退就重新 `play`

`sound` 写一个音效 ID 加可选音量和音高 它和文字 淡入淡出一样跑在客户端时钟上 三者的触发时机是一帧对齐的 所以能卡点 不像用 `command` 执行 playsound 那样受命令系统调度 最多会差 1 tick

### 实体动画

时间轴可以触发 GeckoLib 动画 目标实体需要自身实现 `GeoAnimatable`

```json
{
  "time": 40,
  "animation": {
    "entity": "@nearest",
    "controller": "main",
    "name": "wave",
    "loop": false,
    "speed": 1.0
  }
}
```

| 字段 | 默认 | 说明 |
|---|---|---|
| `entity` | `@player` | `@player` `@self` `@nearest` 或原版选择器 `@e[type=...]` |
| `controller` | `main` | 控制器名 |
| `name` | 必填 | 动画名 |
| `loop` | `false` | 预留给循环控制 见下 |
| `speed` | `1.0` | 播放速度 |

GeckoLib 是可选依赖 没装的时候这个事件是空操作 不报错也不影响其余时间轴 `speed` 通过反射调整控制器 调整失败就按动画原本的速度播 `loop` 目前读不到 GeckoLib 的循环开关 那由动画定义本身决定 字段先留着

### 玩家替身

演出要让玩家"做点什么"但不想真的移动玩家时用替身 真玩家隐形 在原位置放一个套用本人皮肤的假人 由演出控制走位和动作 结束时自动还原

```json
{
  "time": 176,
  "player_stand_in": {
    "hide": true,
    "keys": [
      { "time": 0,  "offset": [0.0, 0.0, 0.0],  "yaw_offset": 0.0,  "motion": "idle" },
      { "time": 30, "offset": [1.5, 0.0, -1.0], "yaw_offset": 45.0, "motion": "walk" },
      { "time": 60, "offset": [1.5, 0.0, -1.0], "yaw_offset": 90.0, "motion": "wave" }
    ]
  }
}
```

`hide` 为 `true` 开始替身 为 `false` 结束

`keys` 里 `time` 是相对替身开始的 tick `offset` 相对玩家站立的位置 `yaw_offset` 叠加在玩家当前朝向之上

`motion` 是预设动作 `idle` `walk` `wave` `point` `look_up` `bow` 两帧之间取较近的那个

替身是纯客户端渲染 不生成实体 没有碰撞 没有 AI 服务端也不知道它存在 皮肤用玩家自己的 参与光照但不参与阴影

位置和朝向在关键帧之间插值 动作是切换的 所以两个不同动作交界处会有一次硬切 想要过渡就在中间多给一帧

### QTE

时间轴可以插入一次限时按键 提示画在屏幕下方中央 不挡住演出画面

```json
{
  "time": 200,
  "qte": {
    "kind": "key",
    "key": "key.jump",
    "duration": 40,
    "optional": true,
    "prompt": { "text": "接住碎片" },
    "on_success": [ { "type": "set_flag", "flag": "relic_caught" } ],
    "on_fail": [ { "type": "set_flag", "flag": "relic_dropped" } ]
  }
}
```

| 字段 | 默认 | 说明 |
|---|---|---|
| `kind` | `key` | `key` 按一次 `mash` 连按五次 |
| `key` | `key.jump` | 原版按键映射名 |
| `duration` | `40` | 限定时间 单位 tick |
| `optional` | `true` | 见下 |
| `prompt` | 无 | 提示文字 |
| `on_success` | 空 | 成功时执行 |
| `on_fail` | 空 | 失败时执行 |

`optional` 只影响你怎么组织数据 引擎行为不变 设 `true` 就把 `on_fail` 当成"没接住的默认剧情" 设 `false` 就当成"失败分支"

无论成败演出都继续 超时算失败 跳过演出算成功 所以剧情不会卡死

按键写的是映射名 `key.jump` `key.attack` 这类 玩家改过键之后按新键算成功 因为比对的是映射不是物理键位

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

演出结束会发 `CutsceneFinishedEvent` 带一个原因

```java
@SubscribeEvent
public static void onCutsceneFinished(CutsceneFinishedEvent event) {
    switch (event.getReason()) {
        case FINISHED -> { }   // 正常放完
        case SKIPPED -> { }    // 玩家按 Esc 跳过
        case STOPPED -> { }    // 被外部 stop 或者被新演出顶掉
    }
}
```

区分这个很重要 生成 Boss 停音乐这类副作用该不该在跳过时发生 取决于你的剧情设计 但库至少要把三种情况分开告诉你

时间轴两端各跑各的 服务端按 tick 触发 `actions` 客户端按 tick 加部分帧触发文字和淡入淡出 两端不共享时钟 动作和画面之间最多差 1 tick 这是有意的取舍 换成同步时钟得为一次演出额外维护往返状态 不划算

相机接管是这里唯一一处 Mixin 原因很直接 NeoForge 的 `ViewportEvent.ComputeCameraAngles` 触发在 `Camera.setup()` 的**第一行** 紧接着 `setRotation` 和 `setPosition` 就会把它覆盖掉 所以在那个事件里改位置是无效的 相机位置没有官方钩子 只能在 `setup()` 返回时注入 另外用 AccessTransformer 打开了 `Camera.setPosition` 和 `setRotation` 它们原本是 protected

---

## 音乐

循环背景音乐 播放和停止都由服务端控制 跟着玩家的会话走

```java
Music.play(player, ResourceLocation.parse("minecraft:music.overworld.forest"));
Music.play(player, ResourceLocation.parse("minecraft:music.game"), 0.6F);   // 带音量
Music.stop(player);
Music.isPlaying(player);
Music.current(player);        // 当前曲目 没播就是 null
```

音效 ID 用的是同一个音效注册表 所以原版音乐 资源包音乐 别的模组注册的音乐都能直接写

播放走客户端的 MUSIC 声道 受玩家的音乐音量设置影响 同一时刻一个玩家只有一首 播新的会顶掉旧的 服务端 `stop` 或者客户端掉线都会停

---

## 渲染层级

cinematic 对话 演出 替身 QTE 同时在场时 从下往上依次是

```
世界与实体
  相机关键帧接管视角
  玩家替身 用玩家模型和皮肤画
  上下黑边 letterbox
  演出字幕
  演出淡入淡出
  报幕
  对话压暗层        仅 cinematic
  对话黑幕与头像    仅 cinematic
  ambient 对话框    仅 ambient
  QTE 提示
```

真玩家在替身生效期间不画 结束时画回来 演出结束时替身消失 对话如果没结束就继续 两者互不干扰

QTE 在对话之上 所以剧情对话里插一次按键也看得见 它画在屏幕下方中央 不挡演出画面

cinematic 的压暗层会盖住演出画面但不会盖住黑幕和台词 中间那块是留给演出本身的 只压暗不遮挡

---

## 任务界面

内置一个任务列表 按 `J` 打开 也可以用 `/story quests` 打开

界面按状态分组列出所有可见任务 接取中的排在最前 每条显示标题 描述 以及每个目标带完成标记和进度 比如 `[ ] 收集碎片 (3/5)`

数据全部来自客户端已有的东西 任务定义是同步注册表下发的 玩家进度是自动同步的 所以界面不产生任何额外网络流量 也不会有延迟

键位可以在原版的按键设置里改 分类是 SQDCLib

---

## 数据包路径

```
data/<包命名空间>/sqdclib/story_quest/<任务名>.json
data/<包命名空间>/sqdclib/story_dialogue/<对话名>.json
data/<包命名空间>/sqdclib/story_cutscene/<演出名>.json
```

注意中间那层 `sqdclib/` 不能省 数据包注册表的路径规则是 `data/<包命名空间>/<注册表命名空间>/<注册表路径>/` 三个注册表的命名空间都是 `sqdclib` 所以要多一层 少了这层的话注册表会是空的 而且不会报错 只是什么都加载不到

三类都是同步注册表 定义会自动下发到客户端 ID 由路径决定 `data/mypack/sqdclib/story_quest/first_relic.json` 就是 `sqdclib:first_relic`

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

命令里的 `<ID>` 可以写全称 `sqdclib:first_relic` 也可以省掉命名空间直接写 `first_relic` 省掉时按 `sqdclib` 解析 其他命名空间的内容必须写全称

```
/story talk elder_greeting
/story talk villager_chat
/story counter shards 3
/story cutscene relic_reveal
```

第一个是 cinematic 模式 第二个是 ambient 模式 第三个会把任务推到完成 第四个演示相机 动画 替身 QTE 和报幕

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
| `/story cutscene <ID> <起始 tick>` | 从指定 tick 开始 |
| `/story seek <tick>` | 播放中往后跳 |
| `/story music <ID>` | 循环播放音乐 |
| `/story music stop` | 停止音乐 |
| `/story quests` | 打开任务列表 这个是客户端命令 不需要 OP |

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
Story.getString(player, "player_name")
Story.setString(player, "player_name", "旅人")
Story.clearString(player, "player_name")
StoryText.resolve(player, component)            // 把 %变量% 替换成实际值
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
Cutscenes.play(player, id, startTick)           // 从指定 tick 开始 之前的不补执行
Cutscenes.seek(player, tick)                    // 播放中往后跳
Cutscenes.currentTick(player)                   // 没在播就是 -1
Cutscenes.playFor(players, id)                  // 给一组人播 返回开始了几场
Cutscenes.playNear(anchor, radius, id)          // 给附近的人播
Cutscenes.skip(player)                          // 补齐剩余 actions 后结束
Cutscenes.stop(player)
Cutscenes.isPlaying(player)
Cutscenes.isSkippable(player)
Cutscenes.currentCutscene(player)
Cutscenes.definition(player, id)
```

音乐

```java
Music.play(player, soundId)                     // 循环播放
Music.play(player, soundId, volume)
Music.stop(player)
Music.isPlaying(player)
Music.current(player)                           // 没播时是 null
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

StoryActions.register(type, mapCodec)           // 注册自定义动作类型
StoryActions.registered()                       // 已注册的类型
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

**替身的动作切换是硬的** 关键帧之间位置和朝向插值 但 `motion` 是切换的 两个不同动作交界处会跳一下 中间多给一帧能缓解但除不掉 想要真正连贯的动作得上骨骼动画方案

**GeckoLib 的 `loop` 目前不生效** 那个开关在动画定义里不在控制器上 反射够不着 字段先留着占位 `speed` 是尽力而为 反射失败就按动画原本的速度播

**ambient 模式用数字键选选项** 因为它不开界面 玩家要保持能走动就没法用鼠标点 cinematic 是相反的取舍 鼠标能点但不能走

**cinematic 黑幕是整条横跨屏幕的** 它只占底部 但如果把 `duration` 拉得很短 黑幕高度会按内容撑到超过屏幕三分之一

**数据包注册表只在世界加载时读一次** `/reload` 不会重建它们 换了 jar 或者改了 `data/` 下的文件之后要退出世界重进 老存档还会带着已知数据包列表 排查时先用新存档确认一次

**启动日志里有三行自检** 服务端起好之后会打印

```
[sqdclib] story_quest holds 2 entries [sqdclib:first_relic, sqdclib:prologue] (loaded from data/<pack_namespace>/sqdclib/story_quest/)
```

如果看到 `is registered but holds no entries` 说明注册表建出来了但一个文件都没读到 检查路径是不是少了一层 `sqdclib/` 如果看到 `was never created` 说明 `DataPackRegistryEvent` 没跑到 那是库侧的问题

---

## 许可

MIT  完整条款见仓库根目录的 LICENSE 文件
