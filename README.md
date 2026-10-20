# SQDCLib

Minecraft 1.21.1   NeoForge 21.1.219+   Java 21

鍐掗櫓妯＄粍閲岀湡姝ｇ（浜虹殑浠庢潵涓嶆槸鐜╂硶 鏄偅浜涚粫涓嶅紑鐨勯厤瑙掑伐浣?鐘舵€佸瓨鍝?浠诲姟鎬庝箞鎺ㄨ繘 瀵硅瘽鎬庝箞鍒嗘敮 婕斿嚭鎬庝箞鎾?姣忔寮€鏂板潙閮藉緱閲嶅啓涓€閬?鍐欏埌涓€鍗婂氨鍙戠幇姣忎釜妯＄粍閮介暱鍑轰簡涓€濂楄嚜宸辩殑鍗婃垚鍝佹鏋?
SQDCLib 鎶婅繖鍥涗欢浜嬫娊鍑烘潵鍋氭垚涓€濂楀墠缃緷璧?鍥涗釜妯″潡褰兼瑙ｈ€?鍙敤鍏朵腑涓€涓や釜涔熸病闂 娌℃湁绗笁鏂瑰唴瀹规ā缁勪緷璧?鍏ㄩ儴璧?NeoForge 瀹樻柟 API

浣滆€?Mornfire

---

## 鎺ュ叆

```bash
./gradlew build
```

浜х墿鏄?`build/libs/sqdclib-1.0.0.jar` 闇€瑕?JDK 21 棣栨鏋勫缓浼氫笅杞藉苟鍙嶇紪璇?Minecraft 涓?NeoForge 绛夊嚑鍒嗛挓

鏀捐繘 `libs/` 鍚?
```gradle
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    implementation "com.mornfire:sqdclib:3.0.1"
}
```

鐒跺悗鍦ㄨ嚜宸辩殑 `neoforge.mods.toml` 閲屽０鏄庝緷璧?
```toml
[[dependencies.your_mod]]
    modId="sqdclib"
    type="required"
    versionRange="[1.0.0,)"
    ordering="AFTER"
    side="BOTH"
```

---

## 鍙€変緷璧?
GeckoLib 鏄彲閫夌殑 鍙湁鍦ㄤ綘鐢?`animation` 浜嬩欢鏃舵墠闇€瑕?搴撴湰韬笉鍦ㄧ紪璇戞湡寮曠敤瀹冪殑浠讳綍绫?鑰屾槸杩愯鏃跺弽灏勬煡鎵?`software.bernie.geckolib.animatable.GeoAnimatable`

杩欐剰鍛崇潃娌¤ GeckoLib 鏃?`animation` 浜嬩欢鏄┖鎿嶄綔 涓嶄細鎶?`NoClassDefFoundError` 涔熶笉浼氳婕斿嚭涓柇 瑁呬簡灏变細姝ｅ父瑙﹀彂

`neoforge.mods.toml` 閲屽凡缁忓０鏄庝簡杞緷璧?鎵撳寘鏃朵笉鐢ㄩ澶栧鐞?
娌″紩鍏?PlayerAnimator 瀹冪粦瀹氬湪鐪熺帺瀹跺疄浣撲笂 鑰屾浛韬渶瑕佺殑鏄?鐪熺帺瀹堕殣褰㈠姞涓€涓函瀹㈡埛绔ā鍨? 涓よ€呮ā鍨嬪涓嶄笂 鏇胯韩闇€瑕佺殑璧颁綅 杞韩 涓炬墜鐢?`ModelPart` 鐩存帴椹卞姩灏卞 鍔犺繘鏉ュ彧浼氬涓€灞備緷璧?
---

## 鍥涗釜妯″潡

```
com.mornfire.sqdclib
鈹溾攢鈹€ storystate        鐘舵€佸眰 鍞竴鐨勪簨瀹炴潵婧?鈹?  鈹溾攢鈹€ StoryProgress     鐜╁绾х姸鎬?AttachmentType
鈹?  鈹溾攢鈹€ WorldState        涓栫晫绾х姸鎬?SavedData
鈹?  鈹溾攢鈹€ Story             璺ㄤ綔鐢ㄥ煙鏌ヨ闂ㄩ潰
鈹?  鈹溾攢鈹€ condition/        鏉′欢琛ㄨ揪寮?鈹?  鈹溾攢鈹€ action/           鍔ㄤ綔涓庤法灞傛ˉ
鈹?  鈹溾攢鈹€ event/            鍙樻洿浜嬩欢
鈹?  鈹斺攢鈹€ net/              涓栫晫鐘舵€佸悓姝?鈹溾攢鈹€ quest             浠诲姟灞?鏁版嵁鍖呭畾涔?+ 鐘舵€佹満
鈹溾攢鈹€ dialogue          瀵硅瘽灞?鏈嶅姟绔潈濞佷細璇?+ 瀹㈡埛绔晫闈?鈹斺攢鈹€ cutscene          婕斿嚭灞?鍏抽敭甯х浉鏈?+ 鏃堕棿杞?+ 灞忓箷瑕嗙洊
```

鍒嗗眰涓嶆槸涓轰簡璁╃洰褰曞ソ鐪?鏍稿績鑰冭檻鏄?*鏉′欢琛ㄨ揪寮忓繀椤诲彧鏈変竴浠?* 浠诲姟鐩爣 瀵硅瘽閫夐」 婕斿嚭鍒嗘敮鍒ゆ柇鐨勫叾瀹炴槸鍚屼竴鎵逛笢瑗?flag 璁℃暟 闃舵 浠诲姟瀹屾垚搴?濡傛灉鏉′欢绯荤粺褰掍换鍔″眰 瀵硅瘽灞傚拰婕斿嚭灞傚氨寰楀弽杩囨潵渚濊禆浠诲姟灞?閭ｅ悗闈㈡瘡鍔犱竴灞傞兘瑕侀噸鏂版帓渚濊禆 鎵€浠ユ潯浠舵斁鍦ㄧ姸鎬佸眰 涓婇潰涓夊眰鍏辩敤涓€涓眰鍊煎櫒 鑰屼笖瀹冨湪瀹㈡埛绔篃鑳借窇 鐜╁鐨勭姸鎬佹槸鑷姩鍚屾杩囧幓鐨?涓栫晫鐘舵€佹湁涓撻棬鐨勯暅鍍忓寘

灞備笌灞備箣闂寸殑璋冪敤璧?`StoryBridge` 鍥炶皟 鐘舵€佸眰閲屾病鏈変换浣曟寚鍚戝彟澶栦笁灞傜殑 import 杩欎竴鐐逛笅闈?璺ㄥ眰缁勫悎 浼氬睍寮€

---

## 鐘舵€佸眰

### 鐜╁鐘舵€?
姣忎釜鐜╁涓€浠?鎸傚湪瀹炰綋涓?鍐欐搷浣滀細鑷姩鍚屾缁欐湰浜?姝讳骸鍚庝繚鐣?闈?`AttachmentType` 鐨?`copyOnDeath`

```java
StoryProgress p = StoryProgress.of(player);

p.setFlag("relic_found");              // 杩斿洖鏄惁鐪熺殑鍙戠敓浜嗗彉鍖?p.setFlag("gate_open", false);         // 娓呴櫎
p.hasFlag("relic_found");
p.getFlag("relic_found");              // 鍜?hasFlag 鍚屼箟 鍐欐潯浠惰〃杈惧紡鏃堕『鎵嬩簺

p.getCounter("shards");                // 缂虹渷 0
p.setCounter("shards", 3);             // 杩斿洖鏃у€?p.addCounter("shards", 1);             // 杩斿洖鏂板€?
p.completeQuest(ResourceLocation.parse("sqdclib:prologue"));
p.isQuestCompleted(ResourceLocation.parse("sqdclib:prologue"));
p.revokeQuest(...);

p.isQuestActive(...);
p.activateQuest(...);
p.deactivateQuest(...);

p.getFlags();                          // 鍙瑙嗗浘
p.getCompletedQuests();
p.getActiveQuests();
```

姣忔鍐欏叆閮戒細鎶?*鏁翠釜鐘舵€?*鎺ㄧ粰璇ョ帺瀹跺鎴风 涓斿彧鎺ㄧ粰鏈汉 鍙欎簨 flag 鏄鏈夋暟鎹?骞挎挱缁欏懆鍥寸殑浜烘棦娌″繀瑕佷篃涓嶅悎閫?flag 鍙樺寲鏃惰繕浼氬湪 `NeoForge.EVENT_BUS` 涓婂彂 `StoryFlagChangedEvent`

### 涓栫晫鐘舵€?
姣忎釜瀛樻。涓€浠?鐗╃悊浣嶇疆鏄富涓栫晫鐨?`data/sqdclib_story_state.dat` 鍥犱负鎸傚湪涓讳笘鐣屼笂 鎵€浠?*鎵€鏈夌淮搴﹀ぉ鐒跺叡浜悓涓€浠?* 涓嶉渶瑕侀澶栧仛缁村害闂寸殑鍚屾

```java
WorldState s = WorldState.get(player.level());   // 鏈嶅姟绔拰瀹㈡埛绔兘鑳借繖涔堟嬁
WorldState s = WorldState.get(minecraftServer);  // 鍙湁 server 鐨勫満鍚?
s.setFlag("gate_open");
s.hasFlag("gate_open");

s.getPhase();                     // 绌哄瓧绗︿覆琛ㄧず杩樻病璁剧疆杩?s.setPhase("chapter1");           // 杩斿洖鏄惁鍙樺寲

s.getFlags();
s.isServerSide();                 // 瀹㈡埛绔暅鍍忚繑鍥?false
```

瀹㈡埛绔嬁鍒扮殑鏄竴浠藉彧璇婚暅鍍?鍦ㄧ帺瀹剁櫥褰?鍒囨崲缁村害 浠ュ強涓栫晫鐘舵€佹瘡娆″彉鍖栨椂鎺ㄩ€?**鍦ㄥ鎴风璋冪敤浠讳綍鍐欐柟娉曚細鎶?`UnsupportedOperationException`** 杩欐槸鏁呮剰鐨?璁╁鎴风璇敼鏈湴闀滃儚 鍙細寰楀埌鐣岄潰鍜屾湇鍔＄涓嶄竴鑷磋繖绉嶆渶闅炬煡鐨?bug

### 缁熶竴鏌ヨ

鏉′欢鍒ゆ柇缁忓父涓嶅叧蹇冩煇涓?flag 鍒板簳瀛樺湪鍝竴杈?杩欐椂鍊欑敤 `Story`

```java
Story.hasFlag(player, "relic_found");                    // 鍏堟煡鐜╁ 娌℃湁鍐嶆煡涓栫晫
Story.hasFlag(player, FlagScope.PLAYER, "relic_found");  // 鍙煡鐜╁
Story.hasFlag(player, FlagScope.WORLD, "gate_open");     // 鍙煡涓栫晫

Story.setFlag(player, "met_elder");
Story.clearFlag(player, "met_elder");

Story.getCounter(player, "shards");
Story.addCounter(player, "shards", 1);
Story.setCounter(player, "shards", 3);

Story.completeQuest(player, ResourceLocation.parse("sqdclib:prologue"));
Story.isQuestCompleted(player, ResourceLocation.parse("sqdclib:prologue"));
```

### 浜嬩欢

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

## 鏉′欢琛ㄨ揪寮?
涓冧釜绫诲瀷 閮借兘宓屽 JSON 閲岄潬 `type` 瀛楁鍖哄垎

```json
{ "type": "flag", "flag": "relic_found" }
{ "type": "flag", "flag": "gate_open", "scope": "world" }
{ "type": "flag", "flag": "betrayed", "value": false }
```

`flag` 鐨?`scope` 鍙～ `player` `world` `any` 榛樿 `any` `value` 榛樿 `true` 濉?`false` 灏辨槸 鏈缃?
```json
{ "type": "quest", "quest": "sqdclib:prologue" }
{ "type": "counter", "key": "shards", "at_least": 3 }
{ "type": "phase", "phase": "chapter1" }
{ "type": "string", "key": "player_name", "equals": "Mornfire" }
{ "type": "string", "key": "player_name" }
{ "type": "not", "value": { "type": "flag", "flag": "gate_open" } }
```

`at_least` 榛樿 1 `string` 涓嶅啓 `equals` 鏃跺垽鏂繖涓彉閲忛潪绌?閫昏緫缁勫悎鐢?`all` 鍜?`any`

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

## 鍔ㄤ綔

鍚屾牱甯?`type` 瀛楁 鍙互鍐欏湪浠诲姟 瀵硅瘽 婕斿嚭鐨勪换鎰忎綅缃?
鍐欑姸鎬?
```json
{ "type": "set_flag", "flag": "met_elder" }
{ "type": "set_flag", "flag": "gate_open", "value": true, "scope": "world" }
{ "type": "add_counter", "key": "shards", "amount": 1 }
{ "type": "set_phase", "phase": "chapter1" }
```

`set_flag` 鐨?`scope` 榛樿 `player`

浠ョ帺瀹惰韩浠芥墽琛屽懡浠?
```json
{ "type": "command", "command": "give @s minecraft:bread 4" }
```

椹卞姩娴佺▼

```json
{ "type": "start_quest", "quest": "sqdclib:first_relic" }
{ "type": "complete_quest", "quest": "sqdclib:first_relic" }
{ "type": "start_dialogue", "dialogue": "sqdclib:elder_greeting" }
{ "type": "play_cutscene", "cutscene": "sqdclib:relic_reveal" }
{ "type": "play_cutscene", "cutscene": "sqdclib:relic_reveal", "await": true }
```

鍚庡洓涓€氳繃 `StoryBridge` 杞彂缁欏搴旀ā鍧?娌¤瀵瑰簲瀹炵幇鏃舵槸绌烘搷浣?涓嶄細鐐?
### 鑷畾涔夊姩浣?
鍐呯疆鍏涓嶅鐢ㄦ椂鍙互娉ㄥ唽鑷繁鐨勭被鍨?
```java
StoryActions.register(
        ResourceLocation.fromNamespaceAndPath("ymondf", "avatar_reveal"),
        RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("tick").forGetter(AvatarReveal::tick))
                .apply(instance, AvatarReveal::new)));
```

鑷畾涔夊姩浣滃疄鐜?`StoryAction` 鎺ュ彛 鎻愪緵 `run(ServerPlayer)` 鍜?`type()` 鐒跺悗 JSON 閲屽啓鍏ㄥ悕

```json
{ "type": "ymondf:avatar_reveal", "tick": 300 }
```

`type()` 杩斿洖鐨勫瓧绗︿覆瑕佸拰娉ㄥ唽鏃剁殑璧勬簮浣嶇疆瀹屽叏涓€鑷?甯﹀啋鍙?涓嶅甫鍐掑彿鐨勬寜鍐呯疆绫诲瀷鏌ユ壘

娉ㄥ唽瑕佸湪 mod 鏋勯€犻樁娈靛畬鎴?鏃╀簬浠讳綍鏁版嵁鍖呰В鏋愬氨琛?
绛惧悕鏀剁殑鏄?`MapCodec` 涓嶆槸 `Codec` 鍥犱负搴曞眰鐨?type 鍒嗗彂鍙兘鎺?`MapCodec` 鎶?record 鐨?`mapCodec` 鐩存帴浼犺繘鏉ュ嵆鍙?涓嶉渶瑕侀澶栧寘涓€灞?
`play_cutscene` 榛樿涓嶉樆濉?鎾畬涔嬪墠鍚庨潰鐨勫姩浣滅収甯告墽琛?鍔犱簡 `await` 涔嬪悗杩欎釜鍔ㄤ綔浼氭寕璧锋暣鏉″姩浣滈摼 婕斿嚭缁撴潫鍚庝粠涓嬩竴鏉＄户缁?鍐欏璇濇椂鐢ㄥ畠鍙互鍋氬埌 璇村畬杩欏彞婕斾竴娈靛啀鍥炲埌瀵硅瘽 璇﹁涓嬮潰鐨勬紨鍑虹珷鑺?
---

## 浠诲姟

瀹氫箟鏀惧湪 `data/<鍛藉悕绌洪棿>/story_quest/<浠诲姟鍚?.json`

```json
{
  "title": { "text": "绗竴浠堕仐鐗? },
  "description": { "text": "鏀堕泦涓夊潡鏁ｈ惤鐨勭鐗? },
  "kind": "main",
  "prerequisites": ["sqdclib:prologue"],
  "visible_when": { "type": "phase", "phase": "chapter1" },
  "objectives": [
    {
      "id": "collect",
      "title": { "text": "鏀堕泦 3 鍧楃鐗? },
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

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `title` | 蹇呭～ | 浠诲姟鏍囬鏂囨湰 |
| `description` | 鏃?| 浠诲姟鎻忚堪 |
| `kind` | `main` | `main` 鎴?`side` |
| `prerequisites` | 绌?| 鍏ㄩ儴瀹屾垚鍚庢湰浠诲姟鎵嶅彲鎺ュ彇 |
| `visible_when` | 鏃?| 鏄惁鍦ㄤ换鍔′功閲屾樉绀?鍜岃兘鍚︽帴鍙栨槸涓ゅ洖浜?|
| `objectives` | 绌?| 鍏ㄩ儴杈炬垚灏辫嚜鍔ㄥ畬鎴?|
| `fail_when` | 鏃?| 婊¤冻鍗冲垽瀹氬け璐?鍜?objectives 涓€鏍锋瘡娆℃鏌ユ椂瀹炴椂姹傚€?|
| `on_start` | 绌?| 鎺ュ彇鏃舵墽琛?|
| `on_complete` | 绌?| 瀹屾垚鏃舵墽琛?鍦ㄥ啓鍏ュ畬鎴愮姸鎬佷箣鍚?|
| `on_fail` | 绌?| 澶辫触鏃舵墽琛?鍦ㄥ啓鍏ュけ璐ョ姸鎬佷箣鍚?|

姣忎釜鐩爣鑷繁鐨勫瓧娈?
| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `id` | 蹇呭～ | 鐩爣鏍囪瘑 |
| `title` | 蹇呭～ | 鏄剧ず鏂囨湰 |
| `done` | 蹇呭～ | 杈炬垚鏉′欢 |
| `progress` | 鏃?| 杩涘害鏉ユ簮 `{ "counter": "shards", "max": 5 }` |

`progress` 鍙奖鍝嶆樉绀?涓嶅奖鍝嶅垽瀹?鍒ゅ畾姘歌繙鐪?`done` 鎯崇粰鐜╁鐪?3/5 灏卞湪鐩爣涓婂啓瀹?鎷挎覆鏌撶粨鏋滆 `Quests.objectiveTitle(player, objective)` 娌″啓 `progress` 鐨勭洰鏍囪繑鍥炰笉甯︽嫭鍙风殑鍘熸爣棰?
鏈変釜鍦版柟鍊煎緱鍗曠嫭璇?**鐩爣娌℃湁 宸插畬鎴?杩欎釜鐘舵€?* 杈炬垚涓庡惁瀹屽叏鐢?`done` 鏉′欢瀹炴椂姹傚€?杩欐牱鏉′欢鍜岃繘搴︿笉鍙兘瀵逛笉涓?涓嶄細鍑虹幇 鏉′欢鏃╁氨婊¤冻浜嗕絾鐣岄潰杩樻樉绀烘湭瀹屾垚 鐨勬儏鍐?浠ｄ环鏄瘡 20 tick 瑕侀噸鏂版眰鍊间竴閬?浠诲姟閲忓ぇ鏃跺彲浠ヨ€冭檻鏀规垚浜嬩欢椹卞姩

`objectives` 涓虹┖鐨勪换鍔′笉浼氳嚜鍔ㄥ畬鎴?寰楅潬 `complete_quest` 鎴栬€呬唬鐮佹樉寮忕粨鏉?
鐘舵€佸彧鏈変簲绉?
```
LOCKED 鈹€鈹€鍓嶇疆鍏ㄥ畬鎴愨攢鈹€鈻?AVAILABLE 鈹€鈹€start()鈹€鈹€鈻?ACTIVE 鈹€鈹€鐩爣鍏ㄨ揪鎴愨攢鈹€鈻?COMPLETED
                          鈻?                    鈹?                          鈹斺攢鈹€鈹€鈹€ abandon() 鈹€鈹€鈹€鈹€鈹€鈹€鈹?                                                鈹斺攢鈹€ fail_when 婊¤冻 鈹€鈹€鈻?FAILED
```

`FAILED` 鏄粓鎬?涓嶄細鍐嶈 refresh 妫€鏌?涔熶笉鑳界洿鎺?start 鎯抽噸鏉ヨ鍏?`reset` `abandon` 涓嶄竴鏍?瀹冨彧鏄斁涓?鍓嶇疆婊¤冻鐨勮瘽浠诲姟绔嬪埢鍥炲埌 AVAILABLE

```java
Quests.status(player, questId);        // LOCKED / AVAILABLE / ACTIVE / COMPLETED / FAILED
Quests.definition(player, questId);    // Optional<QuestDefinition>
Quests.isVisible(player, questId);     // 璇ヤ笉璇ュ湪浠诲姟涔﹂噷鏄剧ず
Quests.hasPrerequisites(player, def);
Quests.isReady(player, def);           // 鐩爣鏄惁鍏ㄨ揪鎴?Quests.isFailed(player, def);          // fail_when 鏄惁婊¤冻
Quests.start(player, questId);         // 鍙湪 AVAILABLE 鏃剁敓鏁?Quests.complete(player, questId);      // 鍙湪 ACTIVE 鏃剁敓鏁?Quests.fail(player, questId);          // 鍙湪 ACTIVE 鏃剁敓鏁?Quests.abandon(player, questId);       // 鏀惧純 鍥炲埌鍙帴鍙?Quests.reset(player, questId);         // 娓呮帀瀹屾垚鍜屽け璐ヨ褰?浠庡ご鍐嶆潵
Quests.refresh(player);                // 鎵嬪姩璺戜竴杞鏌?杩斿洖鏈疆鐘舵€佹湁鍙樼殑浠诲姟
```

```java
@SubscribeEvent
public static void onQuestDone(QuestCompletedEvent event) {
    ServerPlayer player = event.getPlayer();
    ResourceLocation quest = event.getQuest();
}
```

`QuestStartedEvent` `QuestCompletedEvent` `QuestFailedEvent` 閮界户鎵胯嚜 `QuestEvent`

---

## 瀵硅瘽

瀹氫箟鏀惧湪 `data/<鍛藉悕绌洪棿>/story_dialogue/<瀵硅瘽鍚?.json`

```json
{
  "start": "greet",
  "nodes": {
    "greet": {
      "speaker": { "text": "闀胯€? },
      "text": { "text": "鏃呬汉 浣犵粓浜庨啋浜? },
      "options": [
        { "text": { "text": "鎴戝湪鍝? }, "next": "where" },
        { "text": { "text": "娌夐粯" }, "next": "silence" },
        {
          "text": { "text": "鎴戞潵鎵鹃仐鐗? },
          "condition": { "type": "flag", "flag": "relic_hunt_started" },
          "next": "relic"
        }
      ]
    },
    "task": {
      "speaker": { "text": "闀胯€? },
      "text": { "text": "鍘绘敹闆嗕笁鍧楃鐗? },
      "actions": [
        { "type": "set_flag", "flag": "met_elder" },
        { "type": "start_quest", "quest": "sqdclib:first_relic" }
      ],
      "options": [
        { "text": { "text": "鏄庣櫧浜? }, "next": "farewell" }
      ]
    },
    "farewell": {
      "speaker": { "text": "闀胯€? },
      "text": { "text": "鎰挎槦鍏夋寚寮曚綘" }
    }
  }
}
```

瀵硅瘽瀹氫箟鏈変笁涓《灞傚瓧娈?
| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `start` | 蹇呭～ | 璧峰鑺傜偣 ID |
| `mode` | `ambient` | `ambient` 鎴?`cinematic` 瑙佷笅 |
| `lock_movement` | `false` | 瀵硅瘽鏈熼棿鏄惁閿佷綇绉诲姩 璺宠穬 娼滆 |
| `placement` | `bottom` | 瀵硅瘽妗嗕綅缃?`bottom` `center` `top` |
| `text_speed` | `0` | 姣?tick 鏄剧ず鍑犱釜瀛?0 琛ㄧず涓嶅仛鎵撳瓧鏁堟灉 |
| `speaker_color` | 娴呴噾 | 璇磋瘽浜哄悕瀛楃殑 RGB 鏁存暟 |
| `nodes` | 蹇呭～ | 鑺傜偣琛?|

### 鏄剧ず妯″紡

`ambient` 鏄粯璁?鍗婇€忔槑瀵硅瘽妗嗘诞鍦ㄥ睆骞曞簳閮?鐜╁鍙互缁х画璧板姩 閫夐」鐢ㄦ暟瀛楅敭 `1` 鍒?`9` 閫夋嫨 鎸?Esc 閫€鍑?閫傚悎鏀嚎鍜屾棩甯镐氦浜?
`cinematic` 浼氬帇鏆楀睆骞?搴曢儴娴嚭涓€鏁存潯榛戝箷 榛戝箷閲屾斁澶村儚 鍚嶅瓧鍜屽彴璇?鐜╁涓嶈兘绉诲姩 閫夐」鐢ㄩ紶鏍囩偣 閫傚悎鍓ф儏杞姌鐐?榛戝箷鍙崰搴曢儴 涓棿鐣欑粰婕斿嚭鐢婚潰

涓ょ妯″紡閮戒細鏄剧ず澶村儚 鍓嶆彁鏄璇濅汉鍐欎簡 `portrait`

### 璇磋瘽浜轰笌澶村儚

`speaker` 鍙互鏄竴涓函鏂囨湰缁勪欢 涔熷彲浠ユ槸涓€涓甫澶村儚鐨勫璞?
```json
"speaker": { "text": "闀胯€? }
```

```json
"speaker": {
  "name": { "text": "闀胯€? },
  "portrait": "sqdclib:elder"
}
```

`portrait` 闄や簡鍐欏湪 `speaker` 閲?涔熷彲浠ュ啓鍦ㄨ妭鐐逛笂 鎴栬€呭啓鍦ㄥ璇濆畾涔夐《灞備綔涓洪粯璁ゅ€?浼樺厛绾ф槸 鑺傜偣 楂樹簬 璇磋瘽浜?楂樹簬 瀹氫箟椤跺眰

```json
{
  "start": "greet",
  "portrait": "ymondf:elder",
  "nodes": {
    "lore": {
      "portrait": "ymondf:textures/gui/avatar_portrait.png",
      "text": { "text": "..." }
    }
  }
}
```

鍐欑畝鍐欐椂璐村浘鏀惧湪 `assets/<鍛藉悕绌洪棿>/textures/gui/portrait/<璺緞>.png` 鐣岄潰涓婃寜 48 鍍忕礌鐨勬渶闀胯竟绛夋瘮缂╂斁

濡傛灉璺緞浠?`textures/` 寮€澶存垨鑰呬互 `.png` 缁撳熬 灏变細琚綋鎴愬畬鏁磋创鍥捐矾寰勫師鏍蜂娇鐢?鎵€浠?`ymondf:textures/gui/avatar_portrait.png` 鐩存帴鎸囧悜閭ｅ紶鍥?涓嶄細鍐嶆嫾鍓嶇紑 涓ょ鍐欐硶鍙互娣风敤

涓嶅啓 `portrait` 灏卞彧鏄剧ず鍚嶅瓧 涓嶅崰浣嶇疆

鑺傜偣瀛楁

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `speaker` | 鏃?| 鐣欑┖灏变笉鏄剧ず鍚嶅瓧琛?|
| `text` | 蹇呭～ | 鎸夊璇濇瀹藉害鑷姩鎹㈣ |
| `next` | 鏃?| 娌℃湁鍙敤閫夐」鏃惰嚜鍔ㄨ烦杩欓噷 |
| `options` | 绌?| 鍒嗘敮閫夐」 |
| `actions` | 绌?| 杩涘叆鑺傜偣鏃舵墽琛?|
| `continue_text` | 鏃?| 濉簡灏卞彉鎴愮瓑鐜╁鐐瑰嚮鐨勮妭鐐?瑙佷笅 |
| `input` | 鏃?| 鏂囨湰杈撳叆妗?瑙佷笅 |

閫夐」瀛楁

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `text` | 蹇呭～ | |
| `condition` | 鏃?| 涓嶆弧瓒虫椂**鏍规湰涓嶄細鍙戠粰瀹㈡埛绔?* |
| `next` | 鏃?| 鐣欑┖琛ㄧず閫夊畬灏辩粨鏉?|
| `actions` | 绌?| 閫夋嫨鍚庢墽琛?|

瀵硅瘽鏄湇鍔＄鏉冨▉鐨?鏈嶅姟绔寔鏈変細璇?璐熻矗杩囨护閫夐」 鎵ц鍔ㄤ綔 鎺ㄨ繘鑺傜偣 瀹㈡埛绔彧璐熻矗鐢诲拰鍥炰紶涓嬫爣 **鏉′欢鍒ゆ柇鍏ㄥ湪鏈嶅姟绔仛** 瀹㈡埛绔繛涓嶆弧瓒虫潯浠剁殑閫夐」闀夸粈涔堟牱閮戒笉鐭ラ亾 涔熷氨鏃犱粠浼€?
鎺ㄨ繘瑙勫垯娌′粈涔堣姳鏍?杩囨护瀹岃繕鏈夐€夐」灏辨樉绀?娌￠€夐」浣嗘湁 `next` 灏辫嚜鍔ㄨ烦 鏈€澶氳繛璺?64 娆?闃?JSON 鍐欏嚭姝诲惊鐜?涓や釜閮芥病鏈夊氨缁撴潫 瀵硅瘽鏈熼棿娓告垙涓嶆殏鍋?鐜╁鍙互鎸?Esc 涓诲姩閫€鍑?
鍙湁涓€鍙ュ彴璇嶆兂璁╃帺瀹剁湅瀹屽啀鐐逛竴涓?灏卞湪鑺傜偣涓婂啓 `continue_text` 瀹冧細鍦ㄦ病鏈夊彲鐢ㄩ€夐」鏃跺彉鎴愪竴涓寜閽?鎸変笅鍘昏蛋 `next` 杩欐槸瀵硅瘽閲屾渶甯哥敤鐨勪竴绉嶈妭鐐?
```json
{
  "speaker": { "text": "闀胯€? },
  "text": { "text": "鈥︹€︿綘鍚浜嗗悧" },
  "continue_text": { "text": "缁х画" },
  "next": "after_noise"
}
```

涓嶅啓 `continue_text` 鐨勮瘽 `next` 鏄嚜鍔ㄨ烦鐨?涓ょ鍐欐硶鍒贩 瑕佺帺瀹跺仠椤垮氨鐢ㄥ墠鑰?瑕佽娴佺▼鑷繁寰€鍓嶈蛋灏辩敤鍚庤€?
鑺傜偣鍜岄€夐」鐨?actions 閲屽鏋滃啓浜?`play_cutscene` 鍔?`await` 瀵硅瘽浼氬仠鍦ㄩ偅鍙?绛夋紨鍑烘挱瀹屽啀寰€涓嬭蛋 鐢ㄦ潵鍋?璇村畬杩欐婕斾竴娈靛啀鍥炴潵 鐨勫満鏅?鎸傝捣鏈熼棿鐜╁涔熶笉鑳芥搷浣?
瀵硅瘽浣嶇疆鏄惤鐩樼殑 鐜╁鎺夌嚎鏃舵湇鍔＄璁颁綇鍋滃湪鍝竴鍙?閲嶆柊涓婄嚎浼氳嚜鍔ㄦ帴鐫€鑱?涓诲姩鎸?Esc 鍏虫帀鍒欒涓虹粨鏉?鏂偣涓€骞舵竻鎺?涓嬫浠庡ご寮€濮?
### 杈撳叆妗?
鑺傜偣涓婂啓 `input` 浼氬湪瀵硅瘽妗嗛噷鏀句竴涓枃鏈緭鍏ユ 鐜╁鎸夊洖杞︽彁浜?鍐呭瀛樻垚瀛楃涓插彉閲?鐒跺悗璧?`next` 瀹冨拰 `continue_text` 涓€鏍峰彧鍦ㄦ病鏈夊彲鐢ㄩ€夐」鏃跺嚭鐜?
```json
{
  "speaker": { "text": "闀胯€? },
  "text": { "text": "閭ｄ箞 鎴戣鎬庝箞绉板懠浣? },
  "input": {
    "key": "player_name",
    "max_length": 16,
    "placeholder": { "text": "杈撳叆浣犵殑鍚嶅瓧" }
  },
  "next": "greet_named"
}
```

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `key` | 蹇呭～ | 瀛樺埌鍝釜瀛楃涓插彉閲?|
| `max_length` | `32` | 鏈嶅姟绔細鎸夎繖涓暱搴﹀啀鎴竴娆?涓嶄俊浠诲鎴风 |
| `placeholder` | 鏃?| 杈撳叆妗嗕负绌烘椂鐨勭伆瀛楁彁绀?|

### 鏂囨湰鎻掑€?
浠讳綍鏄剧ず缁欑帺瀹剁殑鏂囨湰 鐢?`%鍙橀噺鍚?` 灏辫兘鍙栧嚭瀛楃涓插彉閲忕殑鍊?
```json
{ "text": "%player_name% 杩欎釜鍚嶅瓧鎴戣浣忎簡" }
```

鑺傜偣姝ｆ枃 璇磋瘽浜哄悕 閫夐」鏂囨湰閮芥敮鎸?鍙橀噺涓嶅瓨鍦ㄦ椂鏇挎崲鎴愮┖涓?鎵€浠ヤ笉浼氱湅鍒拌８闇茬殑鐧惧垎鍙?
### 鍙偣鍑绘枃瀛?
姝ｆ枃閲岀殑鏂囧瓧鍙互鐩存帴鐢ㄥ師鐗堢殑鏂囨湰缁勪欢鏍峰紡 甯?`click_event` 鎴?`hover_event` 鐨勭墖娈典細鍙樻垚鍙偣鍖哄煙 榧犳爣绉讳笂鍘诲彉鑹插苟鏄剧ず鎻愮ず 鐐逛笅鍘绘墽琛屽搴斿姩浣?
```json
{
  "text": "璇︾粏鐨勮杞藉湪杩欎釜",
  "extra": [
    {
      "text": "璧勬枡搴?,
      "color": "aqua",
      "underlined": true,
      "click_event": { "action": "open_url", "value": "https://example.com" },
      "hover_event": { "action": "show_text", "value": { "text": "钃濊壊鏂囧瓧鍙互鐐? } }
    },
    { "text": "閲? }
  ]
}
```

鍔ㄤ綔绫诲瀷灏辨槸鍘熺増閭ｅ嚑绉?`open_url` `run_command` `suggest_command` `copy_to_clipboard` `change_page` 杩欓噷涓嶉噸澶嶅垪涓?鎵ц璧扮殑鏄?Minecraft 鑷繁鐨勭粍浠剁偣鍑诲鐞?鎵€浠ヨ涓哄拰鍏朵粬鐣岄潰閲岀殑閾炬帴涓€鑷?
`click_event` 鍙湁鎵撳湪鏂囧瓧鐗囨涓婃墠鏈夋晥 鏁存绾枃鏈笉浼氳Е鍙?
### 鐣岄潰

瀵硅瘽妗嗘槸涓€鍧楄嚜閫傚簲楂樺害鐨勯潰鏉?姝ｆ枃鎸夊搴﹁嚜鍔ㄦ崲琛?寮€鍚?`text_speed` 鍚庨€愬瓧鏄剧ず 鎵撳瓧杩囩▼涓偣涓€涓嬫垨鎸夌┖鏍肩珛鍒诲嚭鍏?閫夐」鍜屽彲鐐瑰嚮鏂囧瓧閮芥寜鍚勮嚜鐨勪綅缃仛鍛戒腑妫€娴?
闈㈡澘楂樺害鎸?*瀹屾暣姝ｆ枃**璁＄畻 鎵€浠ラ€愬瓧鏄剧ず鏃舵涓嶄細瓒婂彉瓒婇珮

```java
Dialogues.start(player, dialogueId);
Dialogues.select(player, index);       // 瀹㈡埛绔晫闈㈠洖浼犵殑涓嬫爣
Dialogues.submit(player, text);        // 瀹㈡埛绔緭鍏ユ鍥炰紶鐨勫唴瀹?Dialogues.stop(player);                // 缁撴潫 娓呮柇鐐?Dialogues.suspend(player);             // 淇濈暀鏂偣 鍙竻鍐呭瓨浼氳瘽
Dialogues.resume(player);              // 浠庢柇鐐规帴鐫€鑱?Dialogues.isTalking(player);
Dialogues.currentDialogue(player);
Dialogues.currentNode(player);
Dialogues.availableOptions(player, node);
Dialogues.definition(player, dialogueId);
```

`Dialogues.CLOSE_INDEX` 鏄?`-1` 浣滀负璇锋眰鍏抽棴鐨勪笅鏍?
---

## 婕斿嚭

瀹氫箟鏀惧湪 `data/<鍛藉悕绌洪棿>/story_cutscene/<婕斿嚭鍚?.json`

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
    { "time": 25, "text": { "text": "纰庣墖鍦ㄤ綘鎺屼腑鑻忛啋" }, "duration": 55 },
    { "time": 90, "text": { "text": "瀹冩寚鍚戜簡鍖楁柟" }, "duration": 45 },
    { "time": 150, "fade": { "to": 1.0, "ticks": 10 } },
    { "time": 155, "actions": [ { "type": "set_flag", "flag": "relic_revealed" } ] }
  ]
}
```

鏃堕棿鍗曚綅涓€寰嬫槸 tick 20 tick 涓€绉?
| 椤跺眰瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `duration` | 蹇呭～ | 鎬绘椂闀?|
| `lock_movement` | `true` | 閿佺Щ鍔?璺宠穬 娼滆 |
| `letterbox` | `true` | 涓婁笅榛戣竟 |
| `skippable` | `false` | 鍏佽鎸?Esc 璺宠繃 |
| `camera` | 绌?| 鐣欑┖灏变笉鎺ョ鐩告満 |
| `events` | 绌?| 鏃堕棿杞翠簨浠?|
| `credits` | 鏃?| 鎶ュ箷 瑙佷笅闈㈠崟鐙竴鑺?|
| `music` | 鏃?| 杩囧満闊宠建 寮€濮嬭嚜鍔ㄦ挱 缁撴潫鎴栬烦杩囪嚜鍔ㄥ仠 |

`skippable` 鎵撳紑涔嬪悗 婕斿嚭鏈熼棿鎸?Esc 鏄烦杩囪€屼笉鏄殏鍋?璺宠繃浼氭妸杩樻病瑙﹀彂鐨?`actions` 鎸夐『搴忓叏閮ㄨˉ榻愭墽琛?鍐嶈蛋姝ｅ父鐨勭粨鏉熸祦绋?鎵€浠ユ寕鍦ㄥ畠鍚庨潰鐨?`await` 瀵硅瘽鐓ф牱浼氱户缁?濂栧姳涔熶笉浼氬洜涓鸿烦杩囪€屼涪

娌″紑 `skippable` 鐨勬紨鍑烘寜 Esc 灏辨槸鏅€氱殑鏆傚仠鑿滃崟 鍏抽敭鍓ф儏寤鸿淇濇寔榛樿 鎶ュ箷閭ｇ闀挎钀藉彲浠ユ墦寮€

鐩告満鍏抽敭甯ч噷 `yaw` `pitch` `roll` `fov` 閮藉彲鐪?`fov` 榛樿 70 鍏朵綑榛樿 0 缂撳姩鍙～ `linear` `ease_in` `ease_out` `ease_in_out` 浣滅敤浜?*鏈抚鍒颁笅涓€甯?*杩欎竴娈?`yaw` 璧版渶鐭矾寰勬彃鍊?鎵€浠?350 搴﹀埌 10 搴︽槸寰€鍓嶈浆 20 搴?涓嶄細鍊掔潃缁曚竴澶у湀

### 鐪嬪悜涓€涓偣

鍏抽敭甯у彲浠ュ啓 `look_at` 缁欎竴涓笘鐣屽潗鏍?鐩告満浼氳嚜鍔ㄧ畻鍑烘湞鍚?杩欐椂鍊?`yaw` 鍜?`pitch` 鍙綔涓洪€€鍖栨儏鍐典笅鐨勭己鐪?
```json
{
  "time": 60,
  "position": [4.0, 68.5, -6.0],
  "look_at": [0.5, 66.0, 0.5],
  "fov": 58.0,
  "easing": "ease_in_out"
}
```

鐩爣浼氱Щ鍔ㄧ殑鏃跺€欒繖涓瘮鎵嬬畻瑙掑害鍙潬 浣犲彧瑕佸叧蹇冩敞瑙嗙偣鍦ㄥ摢 鐜粫瑙掑害鐢变綅缃嚜鍔ㄦ帹鍑烘潵

鏃堕棿杞翠簨浠堕噷 `duration` 榛樿 40 tick 鎸囩殑鏄枃瀛楀仠鐣欏涔?`fade` 鐨?`ticks` 鏄繃娓℃椂闀?
闄や簡鏂囧瓧鍜屾贰鍏ユ贰鍑?涓€涓椂闂磋酱浜嬩欢杩樺彲浠ュ甫 `sound` `animation` `player_stand_in` `qte` 鍥涚瀛愭晥鏋?鍚勮嚜鐨勫瓧娈佃涓嬮潰姣忎竴鑺?鍚屼竴涓簨浠跺彲浠ュ悓鏃跺甫澶氱 瀹冧滑鎸?闊虫晥 鍔ㄧ敾 鏇胯韩 QTE 榛樿鍔ㄤ綔 鐨勯『搴忕敓鏁?
`music` 鍐欎竴涓煶鏁?ID 杩囧満寮€濮嬫椂寰幆鎾斁 缁撴潫鏃跺仠 琚烦杩囨椂涔熷仠 鐢ㄨ繖涓氨涓嶇敤鍦?tick 0 鎵嬪啓涓€涓?`sound` 浜嬩欢 涔熷氨涓嶄細鍑虹幇璺宠繃涔嬪悗闊充箰杩樺湪鍝嶇殑鎯呭喌

杩欓噷鐨?ID 鏄?**SoundEvent 鐨勪簨浠跺悕** 涔熷氨鏄?`sounds.json` 閲屽畾涔夌殑閭ｄ釜閿?涓嶆槸闊抽鏂囦欢鐨勮矾寰?鍐?`ymondf:avatar_descent` 瀵瑰簲 `assets/ymondf/sounds.json` 閲岀殑涓€鏉?`"avatar_descent"` 璁板綍 闊抽鏂囦欢鏈韩鍦?`assets/ymondf/sounds/avatar_descent.ogg`

鎵句笉鍒板搴斾簨浠舵椂浼氬湪鏈嶅姟绔棩蹇楅噷鎵撲竴鏉¤鍛?涓嶄細闈欓粯澶辫触

### 姣?tick 鍥炶皟

鏈嶅姟绔瘡涓?tick 浼氬彂涓€娆?`CutsceneTickEvent` tick 鏄?`0` 鍒?`duration - 1`

```java
@SubscribeEvent
public static void onCutsceneTick(CutsceneTickEvent event) {
    ServerPlayer player = event.getPlayer();
    int tick = event.getTick();
    int duration = event.getDuration();
    float progress = duration <= 1 ? 1.0F : tick / (float) (duration - 1);
}
```

杩欎釜 tick 鍜?`CutsceneDefinition.duration` 鏄悓涓€鏉℃椂闂磋酱 鎵€浠ラ€忔槑搴?鍏夌幆 骞宠瀵硅瘽閮借兘鍗″湪鐪熷疄杩涘害涓?涓嶉渶瑕佽嚜宸卞啀璧蜂竴涓鏁板櫒

娉ㄦ剰鏄?*鏈嶅姟绔?*浜嬩欢 瀹㈡埛绔鍚屾鏁堟灉鐨勮瘽寰楄嚜宸卞彂鍖?
### 浠庢寚瀹?tick 寮€濮?
```java
Cutscenes.play(player, id, 400);   // 鐩存帴浠庣 400 tick 寮€濮?Cutscenes.seek(player, 500);       // 鎾斁涓線鍚庤烦鍒?500
```

`play` 甯﹁捣濮?tick 鏃?*涔嬪墠鐨勪簨浠朵笉浼氳ˉ鎵ц** 瀹冧滑琚爣璁版垚宸茶Е鍙?鐩存帴璺宠繃 `seek` 鍚岀悊 鍙烦鏃堕棿杞?涓嶈ˉ鍔ㄤ綔

杩欐牱鏂嚎閲嶈繛鏃跺彲浠ヨ烦杩囧凡缁忕湅杩囩殑鍓嶆 鐩存帴鍒板璇濆拰榛戝箷 鑰屼笉鏄暣娈佃烦杩?
`seek` 鍙兘寰€鍓?浼犱竴涓洿灏忕殑 tick 浼氳繑鍥?false 闇€瑕佸洖閫€灏遍噸鏂?`play`

`sound` 鍐欎竴涓煶鏁?ID 鍔犲彲閫夐煶閲忓拰闊抽珮 瀹冨拰鏂囧瓧 娣″叆娣″嚭涓€鏍疯窇鍦ㄥ鎴风鏃堕挓涓?涓夎€呯殑瑙﹀彂鏃舵満鏄竴甯у榻愮殑 鎵€浠ヨ兘鍗＄偣 涓嶅儚鐢?`command` 鎵ц playsound 閭ｆ牱鍙楀懡浠ょ郴缁熻皟搴?鏈€澶氫細宸?1 tick

### 瀹炰綋鍔ㄧ敾

鏃堕棿杞村彲浠ヨЕ鍙?GeckoLib 鍔ㄧ敾 鐩爣瀹炰綋闇€瑕佽嚜韬疄鐜?`GeoAnimatable`

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

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `entity` | `@player` | `@player` `@self` `@nearest` 鎴栧師鐗堥€夋嫨鍣?`@e[type=...]` |
| `controller` | `main` | 鎺у埗鍣ㄥ悕 |
| `name` | 蹇呭～ | 鍔ㄧ敾鍚?|
| `loop` | `false` | 棰勭暀缁欏惊鐜帶鍒?瑙佷笅 |
| `speed` | `1.0` | 鎾斁閫熷害 |

GeckoLib 鏄彲閫変緷璧?娌¤鐨勬椂鍊欒繖涓簨浠舵槸绌烘搷浣?涓嶆姤閿欎篃涓嶅奖鍝嶅叾浣欐椂闂磋酱 `speed` 閫氳繃鍙嶅皠璋冩暣鎺у埗鍣?璋冩暣澶辫触灏辨寜鍔ㄧ敾鍘熸湰鐨勯€熷害鎾?`loop` 鐩墠璇讳笉鍒?GeckoLib 鐨勫惊鐜紑鍏?閭ｇ敱鍔ㄧ敾瀹氫箟鏈韩鍐冲畾 瀛楁鍏堢暀鐫€

### 鐜╁鏇胯韩

婕斿嚭瑕佽鐜╁"鍋氱偣浠€涔?浣嗕笉鎯崇湡鐨勭Щ鍔ㄧ帺瀹舵椂鐢ㄦ浛韬?鐪熺帺瀹堕殣褰?鍦ㄥ師浣嶇疆鏀句竴涓鐢ㄦ湰浜虹毊鑲ょ殑鍋囦汉 鐢辨紨鍑烘帶鍒惰蛋浣嶅拰鍔ㄤ綔 缁撴潫鏃惰嚜鍔ㄨ繕鍘?
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

`hide` 涓?`true` 寮€濮嬫浛韬?涓?`false` 缁撴潫

`keys` 閲?`time` 鏄浉瀵规浛韬紑濮嬬殑 tick `offset` 鐩稿鐜╁绔欑珛鐨勪綅缃?`yaw_offset` 鍙犲姞鍦ㄧ帺瀹跺綋鍓嶆湞鍚戜箣涓?
`motion` 鏄璁惧姩浣?`idle` `walk` `wave` `point` `look_up` `bow` 涓ゅ抚涔嬮棿鍙栬緝杩戠殑閭ｄ釜

鏇胯韩鏄函瀹㈡埛绔覆鏌?涓嶇敓鎴愬疄浣?娌℃湁纰版挒 娌℃湁 AI 鏈嶅姟绔篃涓嶇煡閬撳畠瀛樺湪 鐨偆鐢ㄧ帺瀹惰嚜宸辩殑 鍙備笌鍏夌収浣嗕笉鍙備笌闃村奖

浣嶇疆鍜屾湞鍚戝湪鍏抽敭甯т箣闂存彃鍊?鍔ㄤ綔鏄垏鎹㈢殑 鎵€浠ヤ袱涓笉鍚屽姩浣滀氦鐣屽浼氭湁涓€娆＄‖鍒?鎯宠杩囨浮灏卞湪涓棿澶氱粰涓€甯?
### QTE

鏃堕棿杞村彲浠ユ彃鍏ヤ竴娆￠檺鏃舵寜閿?鎻愮ず鐢诲湪灞忓箷涓嬫柟涓ぎ 涓嶆尅浣忔紨鍑虹敾闈?
```json
{
  "time": 200,
  "qte": {
    "kind": "key",
    "key": "key.jump",
    "duration": 40,
    "optional": true,
    "prompt": { "text": "鎺ヤ綇纰庣墖" },
    "on_success": [ { "type": "set_flag", "flag": "relic_caught" } ],
    "on_fail": [ { "type": "set_flag", "flag": "relic_dropped" } ]
  }
}
```

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `kind` | `key` | `key` 鎸変竴娆?`mash` 杩炴寜浜旀 |
| `key` | `key.jump` | 鍘熺増鎸夐敭鏄犲皠鍚?|
| `duration` | `40` | 闄愬畾鏃堕棿 鍗曚綅 tick |
| `optional` | `true` | 瑙佷笅 |
| `prompt` | 鏃?| 鎻愮ず鏂囧瓧 |
| `on_success` | 绌?| 鎴愬姛鏃舵墽琛?|
| `on_fail` | 绌?| 澶辫触鏃舵墽琛?|

`optional` 鍙奖鍝嶄綘鎬庝箞缁勭粐鏁版嵁 寮曟搸琛屼负涓嶅彉 璁?`true` 灏辨妸 `on_fail` 褰撴垚"娌℃帴浣忕殑榛樿鍓ф儏" 璁?`false` 灏卞綋鎴?澶辫触鍒嗘敮"

鏃犺鎴愯触婕斿嚭閮界户缁?瓒呮椂绠楀け璐?璺宠繃婕斿嚭绠楁垚鍔?鎵€浠ュ墽鎯呬笉浼氬崱姝?
鎸夐敭鍐欑殑鏄槧灏勫悕 `key.jump` `key.attack` 杩欑被 鐜╁鏀硅繃閿箣鍚庢寜鏂伴敭绠楁垚鍔?鍥犱负姣斿鐨勬槸鏄犲皠涓嶆槸鐗╃悊閿綅

### 鎶ュ箷

婕斿嚭鍙互甯︿竴娈典粠涓嬪線涓婃粴鐨勬紨鑱屽憳琛?鏃堕棿杞寸収甯歌窇 鎶ュ箷鍦ㄨ嚜宸辩殑鏃堕棿娈甸噷婊?
```json
"credits": {
  "start": 175,
  "duration": 500,
  "speed_up": true,
  "lines": [
    { "text": "SQDCLib", "scale": 2.0, "gap": 30, "color": 16764057 },
    { "text": "鍙欎簨鍓嶇疆搴?, "gap": 40 },
    { "text": "鎰熻阿娓哥帺", "scale": 1.5, "gap": 0, "color": 16764057 }
  ]
}
```

| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `start` | `0` | 浠庣鍑?tick 寮€濮嬫粴 |
| `duration` | 蹇呭～ | 婊氬畬鍏ㄧ▼鐢ㄥ灏?tick |
| `speed_up` | `true` | 鍏堟參鍚庡揩 闀垮悕鍗曚笉鑷充簬鎷栧お涔?|
| `lines` | 绌?| 姣忎竴琛岀殑鍐呭 |

姣忚鑷繁鐨勫瓧娈?
| 瀛楁 | 榛樿 | 璇存槑 |
|---|---|---|
| `text` | 蹇呭～ | 鏂囨湰缁勪欢 |
| `scale` | `1.0` | 瀛楀彿鍊嶆暟 |
| `gap` | `8` | 涓庝笅涓€琛岀殑闂磋窛 鍗曚綅鏄儚绱?|
| `color` | 鐧借壊 | RGB 鏁存暟 16764057 鏄祬閲戣壊 11184810 鏄伆 |

鎶ュ箷鐢诲湪娣″叆娣″嚭涔嬩笂 鎯冲仛鎴愮數褰遍偅绉嶉粦搴曠櫧瀛?灏卞湪 events 閲屽厛 fade 鍒?1 瀛椾細鐩栧湪榛戝簳涓婇潰

```java
Cutscenes.play(player, cutsceneId);
Cutscenes.stop(player);
Cutscenes.isPlaying(player);
Cutscenes.currentCutscene(player);
Cutscenes.definition(player, cutsceneId);
```

`play` 涓嶉樆濉?璋冪敤瀹岀珛鍒昏繑鍥?鎯崇煡閬撴紨瀹屾病鏈?杞 `isPlaying`

婕斿嚭缁撴潫浼氬彂 `CutsceneFinishedEvent` 甯︿竴涓師鍥?
```java
@SubscribeEvent
public static void onCutsceneFinished(CutsceneFinishedEvent event) {
    switch (event.getReason()) {
        case FINISHED -> { }   // 姝ｅ父鏀惧畬
        case SKIPPED -> { }    // 鐜╁鎸?Esc 璺宠繃
        case STOPPED -> { }    // 琚閮?stop 鎴栬€呰鏂版紨鍑洪《鎺?    }
}
```

鍖哄垎杩欎釜寰堥噸瑕?鐢熸垚 Boss 鍋滈煶涔愯繖绫诲壇浣滅敤璇ヤ笉璇ュ湪璺宠繃鏃跺彂鐢?鍙栧喅浜庝綘鐨勫墽鎯呰璁?浣嗗簱鑷冲皯瑕佹妸涓夌鎯呭喌鍒嗗紑鍛婅瘔浣?
鏃堕棿杞翠袱绔悇璺戝悇鐨?鏈嶅姟绔寜 tick 瑙﹀彂 `actions` 瀹㈡埛绔寜 tick 鍔犻儴鍒嗗抚瑙﹀彂鏂囧瓧鍜屾贰鍏ユ贰鍑?涓ょ涓嶅叡浜椂閽?鍔ㄤ綔鍜岀敾闈箣闂存渶澶氬樊 1 tick 杩欐槸鏈夋剰鐨勫彇鑸?鎹㈡垚鍚屾鏃堕挓寰椾负涓€娆℃紨鍑洪澶栫淮鎶ゅ線杩旂姸鎬?涓嶅垝绠?
鐩告満鎺ョ鏄繖閲屽敮涓€涓€澶?Mixin 鍘熷洜寰堢洿鎺?NeoForge 鐨?`ViewportEvent.ComputeCameraAngles` 瑙﹀彂鍦?`Camera.setup()` 鐨?*绗竴琛?* 绱ф帴鐫€ `setRotation` 鍜?`setPosition` 灏变細鎶婂畠瑕嗙洊鎺?鎵€浠ュ湪閭ｄ釜浜嬩欢閲屾敼浣嶇疆鏄棤鏁堢殑 鐩告満浣嶇疆娌℃湁瀹樻柟閽╁瓙 鍙兘鍦?`setup()` 杩斿洖鏃舵敞鍏?鍙﹀鐢?AccessTransformer 鎵撳紑浜?`Camera.setPosition` 鍜?`setRotation` 瀹冧滑鍘熸湰鏄?protected

---

## 闊充箰

寰幆鑳屾櫙闊充箰 鎾斁鍜屽仠姝㈤兘鐢辨湇鍔＄鎺у埗 璺熺潃鐜╁鐨勪細璇濊蛋

```java
Music.play(player, ResourceLocation.parse("minecraft:music.overworld.forest"));
Music.play(player, ResourceLocation.parse("minecraft:music.game"), 0.6F);   // 甯﹂煶閲?Music.stop(player);
Music.isPlaying(player);
Music.current(player);        // 褰撳墠鏇茬洰 娌℃挱灏辨槸 null
```

闊虫晥 ID 鐢ㄧ殑鏄悓涓€涓煶鏁堟敞鍐岃〃 鎵€浠ュ師鐗堥煶涔?璧勬簮鍖呴煶涔?鍒殑妯＄粍娉ㄥ唽鐨勯煶涔愰兘鑳界洿鎺ュ啓

鎾斁璧板鎴风鐨?MUSIC 澹伴亾 鍙楃帺瀹剁殑闊充箰闊抽噺璁剧疆褰卞搷 鍚屼竴鏃跺埢涓€涓帺瀹跺彧鏈変竴棣?鎾柊鐨勪細椤舵帀鏃х殑 鏈嶅姟绔?`stop` 鎴栬€呭鎴风鎺夌嚎閮戒細鍋?
---

## 娓叉煋灞傜骇

cinematic 瀵硅瘽 婕斿嚭 鏇胯韩 QTE 鍚屾椂鍦ㄥ満鏃?浠庝笅寰€涓婁緷娆℃槸

```
涓栫晫涓庡疄浣?  鐩告満鍏抽敭甯ф帴绠¤瑙?  鐜╁鏇胯韩 鐢ㄧ帺瀹舵ā鍨嬪拰鐨偆鐢?  涓婁笅榛戣竟 letterbox
  婕斿嚭瀛楀箷
  婕斿嚭娣″叆娣″嚭
  鎶ュ箷
  瀵硅瘽鍘嬫殫灞?       浠?cinematic
  瀵硅瘽榛戝箷涓庡ご鍍?   浠?cinematic
  ambient 瀵硅瘽妗?   浠?ambient
  QTE 鎻愮ず
```

鐪熺帺瀹跺湪鏇胯韩鐢熸晥鏈熼棿涓嶇敾 缁撴潫鏃剁敾鍥炴潵 婕斿嚭缁撴潫鏃舵浛韬秷澶?瀵硅瘽濡傛灉娌＄粨鏉熷氨缁х画 涓よ€呬簰涓嶅共鎵?
QTE 鍦ㄥ璇濅箣涓?鎵€浠ュ墽鎯呭璇濋噷鎻掍竴娆℃寜閿篃鐪嬪緱瑙?瀹冪敾鍦ㄥ睆骞曚笅鏂逛腑澶?涓嶆尅婕斿嚭鐢婚潰

cinematic 鐨勫帇鏆楀眰浼氱洊浣忔紨鍑虹敾闈絾涓嶄細鐩栦綇榛戝箷鍜屽彴璇?涓棿閭ｅ潡鏄暀缁欐紨鍑烘湰韬殑 鍙帇鏆椾笉閬尅

---

## 浠诲姟鐣岄潰

鍐呯疆涓€涓换鍔″垪琛?鎸?`J` 鎵撳紑 涔熷彲浠ョ敤 `/story quests` 鎵撳紑

鐣岄潰鎸夌姸鎬佸垎缁勫垪鍑烘墍鏈夊彲瑙佷换鍔?鎺ュ彇涓殑鎺掑湪鏈€鍓?姣忔潯鏄剧ず鏍囬 鎻忚堪 浠ュ強姣忎釜鐩爣甯﹀畬鎴愭爣璁板拰杩涘害 姣斿 `[ ] 鏀堕泦纰庣墖 (3/5)`

鏁版嵁鍏ㄩ儴鏉ヨ嚜瀹㈡埛绔凡鏈夌殑涓滆タ 浠诲姟瀹氫箟鏄悓姝ユ敞鍐岃〃涓嬪彂鐨?鐜╁杩涘害鏄嚜鍔ㄥ悓姝ョ殑 鎵€浠ョ晫闈笉浜х敓浠讳綍棰濆缃戠粶娴侀噺 涔熶笉浼氭湁寤惰繜

閿綅鍙互鍦ㄥ師鐗堢殑鎸夐敭璁剧疆閲屾敼 鍒嗙被鏄?SQDCLib

---

## 瑙嗛杩囧満

杩囧満鍙互鏁存鎹㈡垚涓€娈佃棰?鍏ㄥ睆鎾斁 鑷甫涓婁笅榛戝箷 鎾畬鑷姩鍏抽棴骞堕€氱煡鏈嶅姟绔?
```json
{
  "type": "video",
  "video_id": "ymondf:videos/ultimate_new_moon",
  "lock_movement": true,
  "audio": "ymondf:video_audio"
}
```

鏂囦欢鏀惧湪 `data/<鍖呭懡鍚嶇┖闂?/sqdclib/story_cutscene/<鍚嶅瓧>.json` 娉ㄦ剰閭ｄ竴灞?`sqdclib/`

浠ｇ爜閲屼篃鍙互鐩存帴鎾?
```java
Cutscenes.playVideo(player, ResourceLocation.parse("ymondf:videos/ultimate_new_moon"));
```

### 鏍煎紡

鍙敮鎸?**MP4 + H.264**锛岃В鐮佸櫒鐢?JCodec 绾?Java 瀹炵幇

**3.0.1 璧疯В鐮佸櫒涓嶅啀鎵撳寘杩?jar**銆備箣鍓嶇殑 3.0.0 鎶?JCodec 鎵撹繘鍘讳箣鍚庢ā缁勬棤娉曞惎鍔紝鍘熷洜鏄袱涓?JCodec jar 閮藉甫 `org.jcodec.common` 鍖咃紝鏃犺鐢?jarJar 宓屽杩樻槸瑙ｅ寘杩涗富 jar锛岄兘浼氬湪妯″潡瑙ｆ瀽闃舵鍑洪棶棰橈紝鑰屼笖澶辫触鍙戠敓鍦ㄦ棩蹇楀垵濮嬪寲涔嬪墠锛岃〃鐜颁负"鐐逛簡娌″弽搴?銆?
鎵€浠ョ幇鍦?JCodec 鏄紪璇戞湡渚濊禆锛岃繍琛屾椂涓嶅甫銆傜己瀹冪殑鏃跺€欒棰戜簨浠朵細璧?3 绉掑厹搴曞苟鎵撲竴鏉℃棩蹇楄鏄庡師鍥狅紝鍏朵綑鍔熻兘瀹屽叏涓嶅彈褰卞搷銆?
瑕佽瑙嗛鐪熸鎾斁锛岄渶瑕佹妸 JCodec 鏀捐繘鍘伙紝鐩墠杩樺湪鎵句竴涓笉鐮村潖鍚姩鐨勬墦鍖呮柟寮忋€傝繖鏄?3.0.1 鐨勫凡鐭ラ檺鍒躲€?
**涓嶆敮鎸?WebM / VP9** 绾?Java 娌℃湁鍙敤鐨?VP9 瑙ｇ爜瀹炵幇

瑙嗛鏀惧湪 `assets/<鍛藉悕绌洪棿>/videos/<鍚嶅瓧>.mp4` 鍐?`ymondf:ultimate_new_moon` 浼氳嚜鍔ㄨˉ鎴?`ymondf:videos/ultimate_new_moon.mp4` 鍐欏畬鏁磋矾寰勪篃璁?
### 闊抽

JCodec 鍙В瑙嗛杞?涓嶈В闊抽 鎵€浠ヨ棰戞枃浠堕噷鐨勯煶杞ㄤ細琚拷鐣?澹伴煶瑕佸崟鐙粰

`audio` 鍐欎竴涓?SoundEvent 鐨?id 鍜岃棰戝悓鏃跺紑濮嬫挱鏀?闊抽鏂囦欢鐓у父鏀惧湪 `assets/<鍛藉悕绌洪棿>/sounds/` 骞跺湪 `sounds.json` 閲屾敞鍐?鍏蜂綋鍐欐硶鍜?`music` 涓€鏍?
鎾斁鏈熼棿浼氬仠鎺夎儗鏅煶涔愬拰澶╂皵闊虫晥 缁撴潫鍚庢父鎴忚嚜宸辨仮澶?
### 鎾斁娴佺▼

璧勬簮浠?jar 閲屽鍒跺埌涓存椂鏂囦欢鍐嶄氦缁欒В鐮佸櫒 鍥犱负瑙ｇ爜鍣ㄩ渶瑕佸彲闅忔満璁块棶鐨勮緭鍏?
瑙ｇ爜鍦ㄧ嫭绔嬬嚎绋?涓荤嚎绋嬫瘡 tick 鍙仛涓€娆＄汗鐞嗕笂浼?鎵€浠ヨ繃鍦烘湡闂翠笉浼氭帀甯?
鎸夋椂闂存埑涓㈠抚杩借刀鑰屼笉鏄€愬抚绛夊緟 瑙嗛鎱簡灏辫烦甯?涓嶄細瓒婃挱瓒婃參

### 澶辫触澶勭悊

瑙嗛缂哄け 瑙ｇ爜鎶ラ敊 鎴栬€?3 绉掑唴娌¤В鍑虹涓€甯?閮戒細鐩存帴鍏抽棴鐣岄潰骞舵寜澶辫触涓婃姤 鐜╁涓嶄細鍗″湪榛戝睆閲?澶辫触鍘熷洜甯﹀畬鏁磋矾寰勬墦鍦ㄥ鎴风鏃ュ織

鏈嶅姟绔敹鍒?`VideoFinishedEvent` 鍙互鐭ラ亾鎴愬姛杩樻槸澶辫触 鍐嶅喅瀹氳涓嶈缁х画鎴樻枟閫昏緫

---

## 鏁版嵁鍖呰矾寰?
```
data/<鍖呭懡鍚嶇┖闂?/sqdclib/story_quest/<浠诲姟鍚?.json
data/<鍖呭懡鍚嶇┖闂?/sqdclib/story_dialogue/<瀵硅瘽鍚?.json
data/<鍖呭懡鍚嶇┖闂?/sqdclib/story_cutscene/<婕斿嚭鍚?.json
```

娉ㄦ剰涓棿閭ｅ眰 `sqdclib/` 涓嶈兘鐪?鏁版嵁鍖呮敞鍐岃〃鐨勮矾寰勮鍒欐槸 `data/<鍖呭懡鍚嶇┖闂?/<娉ㄥ唽琛ㄥ懡鍚嶇┖闂?/<娉ㄥ唽琛ㄨ矾寰?/` 涓変釜娉ㄥ唽琛ㄧ殑鍛藉悕绌洪棿閮芥槸 `sqdclib` 鎵€浠ヨ澶氫竴灞?灏戜簡杩欏眰鐨勮瘽娉ㄥ唽琛ㄤ細鏄┖鐨?鑰屼笖涓嶄細鎶ラ敊 鍙槸浠€涔堥兘鍔犺浇涓嶅埌

涓夌被閮芥槸鍚屾娉ㄥ唽琛?瀹氫箟浼氳嚜鍔ㄤ笅鍙戝埌瀹㈡埛绔?ID 鐢辫矾寰勫喅瀹?`data/mypack/sqdclib/story_quest/first_relic.json` 灏辨槸 `sqdclib:first_relic`

涓変釜瀹氫箟閮藉彲浠ュ啓涓€涓彲閫夌殑 `schema` 瀛楁 榛樿 1 褰撳墠涔熸槸 1 浠ュ悗鍔犱簡鏂板瓧娈典細鎶婄増鏈彁涓婂幓 鏃х増鏈亣鍒版洿楂樼殑 schema 浼氱洿鎺ユ姤閿欏苟璺宠繃杩欎釜鏂囦欢 鑰屼笉鏄寜缂虹渷鍊艰В鏋愬嚭鍗婁釜姝ｇ‘鐨勭粨鏋?閭ｇ娌夐粯鐨勯敊鏈€闅炬煡

---

## 浠ｇ爜鍐呮敞鍐?
鏁版嵁鍖呬笉鏄敮涓€鍏ュ彛 涓変釜妯″潡閮借兘鐩存帴鍦?Java 閲屾敞鍐屽畾涔?浠ｇ爜娉ㄥ唽浼樺厛浜庢暟鎹寘娉ㄥ唽琛?鑰屼笖涓嶅彈鏁版嵁鍖呭姞杞藉奖鍝?
```java
Cutscenes.register(id, new CutsceneDefinition(...));
Dialogues.register(id, new DialogueDefinition(...));
Quests.register(id, new QuestDefinition(...));
```

閰嶅鏂规硶涓変釜妯″潡閮芥湁

```java
Cutscenes.unregister(id)      // 杩斿洖鏄惁鐪熺殑绉婚櫎浜?Cutscenes.registered()        // 褰撳墠浠ｇ爜娉ㄥ唽鐨?id
Quests.allIds(player)         // 浠ｇ爜娉ㄥ唽鍔犳暟鎹寘娉ㄥ唽 鍚堝苟鍘婚噸 浠诲姟鐣岄潰鐢ㄨ繖涓?```

鏌ヨ鍏ュ彛浼氳嚜鍔ㄥ厛鏌ヤ唬鐮佹敞鍐屽啀鏌ユ敞鍐岃〃 鎵€浠ヨ皟鐢ㄦ柟涓嶇敤鍏冲績瀹氫箟鏉ヨ嚜鍝噷

浠ｇ爜娉ㄥ唽鐨勬椂鏈鸿鍦?mod 鏋勯€犻樁娈?鎴栬€呰嚦灏戝湪浣跨敤涔嬪墠 瀹㈡埛绔拰鏈嶅姟绔兘浼氭墽琛屽悓涓€浠芥敞鍐屼唬鐮?鎵€浠ヤ袱杈圭湅鍒扮殑鍐呭涓€鑷?浠诲姟鐣岄潰涔熶笉渚嬪

杩欐潯璺緞瀹屽叏缁曞紑鏁版嵁鍖?鍛藉悕绌洪棿銆佺洰褰曞眰绾с€乣pack.mcmeta` 閮戒笉鍙備笌 閫傚悎鎶婃暣鍦烘紨鍑虹敤 Java 寤哄嚭鏉?
鏂囨湰瀛楁鍙互鍐欏畬鏁寸殑鍘熺増鏂囨湰缁勪欢

```json
{ "text": "鏅€氭枃鏈? }
{ "translate": "mypack.dialogue.greeting" }
{ "text": "绾㈣壊璀﹀憡", "color": "red" }
```

鐢?`translate` 鑳借鏂囨璺熺潃璇█鏂囦欢璧?
---

## 璋冭瘯鍛戒护

閮借 OP 鏉冮檺 鍐呯疆涓変唤绀轰緥鏁版嵁 鍙互鐩存帴鎷挎潵鍐掔儫娴嬭瘯

鍛戒护閲岀殑 `<ID>` 鍙互鍐欏叏绉?`sqdclib:first_relic` 涔熷彲浠ョ渷鎺夊懡鍚嶇┖闂寸洿鎺ュ啓 `first_relic` 鐪佹帀鏃舵寜 `sqdclib` 瑙ｆ瀽 鍏朵粬鍛藉悕绌洪棿鐨勫唴瀹瑰繀椤诲啓鍏ㄧО

```
/story talk elder_greeting
/story talk villager_chat
/story counter shards 3
/story cutscene relic_reveal
```

绗竴涓槸 cinematic 妯″紡 绗簩涓槸 ambient 妯″紡 绗笁涓細鎶婁换鍔℃帹鍒板畬鎴?绗洓涓紨绀虹浉鏈?鍔ㄧ敾 鏇胯韩 QTE 鍜屾姤骞?
| 鍛戒护 | 浣滅敤 |
|---|---|
| `/story status` | 鎵撳嵃闃舵 涓栫晫 flag 鐜╁ flag 杩涜涓拰宸插畬鎴愮殑浠诲姟 |
| `/story flag <鍚嶇О>` | 鍒囨崲鐜╁ flag |
| `/story worldflag <鍚嶇О>` | 鍒囨崲涓栫晫 flag |
| `/story counter <閿? <鏁伴噺>` | 澧炲噺璁℃暟鍣?|
| `/story phase [闃舵]` | 鏌ヨ鎴栬缃墽鎯呴樁娈?|
| `/story quest <ID> [start\|complete\|fail\|abandon\|reset]` | 鏌ヨ鎴栨敼鍙樹换鍔＄姸鎬?|
| `/story quest <ID> why` | 閫愭潯鎵撳嵃鐩爣涓庡け璐ユ潯浠朵负浠€涔堟病杩?|
| `/story talk <ID>` | 寮€濮嬪璇?|
| `/story cutscene <ID>` | 鎾斁婕斿嚭 |
| `/story cutscene <ID> <璧峰 tick>` | 浠庢寚瀹?tick 寮€濮?|
| `/story seek <tick>` | 鎾斁涓線鍚庤烦 |
| `/story music <ID>` | 寰幆鎾斁闊充箰 |
| `/story music stop` | 鍋滄闊充箰 |
| `/story quests` | 鎵撳紑浠诲姟鍒楄〃 杩欎釜鏄鎴风鍛戒护 涓嶉渶瑕?OP |

---

## 璺ㄥ眰缁勫悎

鍥涗釜妯″潡鍦?mod 鏋勯€犻樁娈靛悇鑷妸瀹炵幇娉ㄥ唽杩?`StoryBridge`

```java
StoryBridge.questStarter(Quests::start);
StoryBridge.questCompleter(Quests::complete);
StoryBridge.dialogueStarter(Dialogues::start);
StoryBridge.cutsceneStarter(Cutscenes::play);
```

浜庢槸杩欎簺缁勫悎閮藉彧鏄暟鎹寘閲岀殑涓€琛?JSON

| 鎯宠鐨勬晥鏋?| 鍐欏湪鍝?|
|---|---|
| 瀵硅瘽涓€旀彃涓€娈垫紨鍑?| 瀵硅瘽鑺傜偣鎴栭€夐」鐨?`actions` 閲屽啓 `play_cutscene` |
| 婕斿嚭杩囧崐鏃跺紑濮嬪璇?| 婕斿嚭鐨?`events` 鍦ㄦ寚瀹氭椂闂寸偣鍐?`start_dialogue` |
| 浠诲姟瀹屾垚鏃舵帹杩涘墽鎯呴樁娈?| 浠诲姟鐨?`on_complete` 閲屽啓 `set_phase` |
| 閫夐」鏍规嵁浠诲姟瀹屾垚搴︽樉绀?| 閫夐」鐨?`condition` 閲屽啓 `quest` |
| 婕斿嚭鍒嗘敮璇诲彇褰撳墠闃舵 | 鏉′欢閲屽啓 `phase` |

鎯冲姞鑷繁鐨勫姩浣滅被鍨?闇€瑕佽嚜宸辨敞鍐屾暟鎹被鍨嬭蛋妗?鍙槸鎯冲湪浠ｇ爜閲岄┍鍔ㄦ祦绋嬬殑璇?鐩存帴璋?`Quests` `Dialogues` `Cutscenes` 鐨勯潤鎬佹柟娉曞氨琛?
---

## API 閫熸煡

鐘舵€佸眰

```java
StoryProgress.of(player).setFlag("x")           // 杩斿洖鏄惁鐪熺殑鍙樹簡
StoryProgress.of(player).setFlag("x", false)
StoryProgress.of(player).hasFlag("x")
StoryProgress.of(player).getCounter("k")
StoryProgress.of(player).setCounter("k", 3)     // 杩斿洖鏃у€?StoryProgress.of(player).addCounter("k", 1)     // 杩斿洖鏂板€?StoryProgress.of(player).getFlags()
StoryProgress.of(player).getCompletedQuests()
StoryProgress.of(player).getActiveQuests()
StoryProgress.of(player).getFailedQuests()

WorldState.get(level)                           // 鏈嶅姟绔笌瀹㈡埛绔兘鑳借繖涔堟嬁
WorldState.get(server)
WorldState.get(level).setFlag("x")
WorldState.get(level).hasFlag("x")
WorldState.get(level).getPhase()
WorldState.get(level).setPhase("chapter1")
WorldState.get(level).isServerSide()

Story.hasFlag(player, "x")                      // 鐜╁浼樺厛 娌℃湁鍐嶆煡涓栫晫
Story.hasFlag(player, FlagScope.WORLD, "x")
Story.setFlag(player, "x")
Story.clearFlag(player, "x")
Story.getCounter(player, "k")
Story.setCounter(player, "k", 3)
Story.addCounter(player, "k", 1)
Story.completeQuest(player, id)
Story.isQuestCompleted(player, id)
Story.getString(player, "player_name")
Story.setString(player, "player_name", "鏃呬汉")
Story.clearString(player, "player_name")
StoryText.resolve(player, component)            // 鎶?%鍙橀噺% 鏇挎崲鎴愬疄闄呭€?```

浠诲姟灞?
```java
Quests.status(player, id)                       // LOCKED / AVAILABLE / ACTIVE / COMPLETED / FAILED
Quests.definition(player, id)                   // Optional<QuestDefinition>
Quests.isVisible(player, id)                    // 璇ヤ笉璇ュ湪浠诲姟涔﹂噷鏄剧ず
Quests.hasPrerequisites(player, def)
Quests.isReady(player, def)                     // 鐩爣鏄惁鍏ㄨ揪鎴?Quests.isFailed(player, def)                    // fail_when 鏄惁婊¤冻
Quests.start(player, id)
Quests.complete(player, id)
Quests.fail(player, id)
Quests.abandon(player, id)                      // 鏀句笅 鍥炲埌鍙帴鍙?Quests.reset(player, id)                        // 娓呮帀瀹屾垚涓庡け璐ヨ褰?Quests.refresh(player)                          // 鎵嬪姩璺戜竴杞?杩斿洖鐘舵€佹湁鍙樼殑浠诲姟
Quests.objectiveProgress(player, objective)     // Optional<Component> 褰㈠ 3/5
Quests.objectiveTitle(player, objective)        // 鏍囬甯︿笂杩涘害
Quests.explain(player, id)                      // List<String> 閫愭潯璇存槑涓轰粈涔堟病杩?Quests.registry(player)
```

瀵硅瘽灞?
```java
Dialogues.start(player, id)
Dialogues.select(player, index)
Dialogues.stop(player)                          // 缁撴潫 娓呮柇鐐?Dialogues.suspend(player)                       // 淇濈暀鏂偣 鍙竻鍐呭瓨浼氳瘽
Dialogues.resume(player)                        // 浠庢柇鐐规帴鐫€鑱?Dialogues.isTalking(player)
Dialogues.currentDialogue(player)
Dialogues.availableOptions(player, node)
Dialogues.definition(player, id)
```

婕斿嚭灞?
```java
Cutscenes.play(player, id)
Cutscenes.play(player, id, onFinish)            // 婕斿畬鎵ц鍥炶皟
Cutscenes.play(player, id, startTick)           // 浠庢寚瀹?tick 寮€濮?涔嬪墠鐨勪笉琛ユ墽琛?Cutscenes.seek(player, tick)                    // 鎾斁涓線鍚庤烦
Cutscenes.currentTick(player)                   // 娌″湪鎾氨鏄?-1
Cutscenes.playFor(players, id)                  // 缁欎竴缁勪汉鎾?杩斿洖寮€濮嬩簡鍑犲満
Cutscenes.playNear(anchor, radius, id)          // 缁欓檮杩戠殑浜烘挱
Cutscenes.skip(player)                          // 琛ラ綈鍓╀綑 actions 鍚庣粨鏉?Cutscenes.stop(player)
Cutscenes.isPlaying(player)
Cutscenes.isSkippable(player)
Cutscenes.currentCutscene(player)
Cutscenes.definition(player, id)
Cutscenes.playVideo(player, videoId)             // 鍏ㄥ睆鎾斁瑙嗛 缁撴潫鐢辨湇鍔＄鏀堕€氱煡
Cutscenes.playVideo(player, videoId, audio, lockMovement)
Cutscenes.isVideoPlaying(player)
```

闊充箰

```java
Music.play(player, soundId)                     // 寰幆鎾斁
Music.play(player, soundId, volume)
Music.stop(player)
Music.isPlaying(player)
Music.current(player)                           // 娌℃挱鏃舵槸 null
```

鏉′欢涓庡姩浣?鍐欎唬鐮佹椂涔熻兘鐩存帴鐢?
```java
condition.test(player)                          // 姹傚€?condition.explain(player)                       // 浜虹被鍙鐨勫け璐ュ師鍥?StoryAction.runAll(player, actions)
StoryAction.runSequentially(player, actions, onComplete)   // 閬囧埌 await 婕斿嚭浼氭寕璧?
StoryBridge.dialogueStarter(...)
StoryBridge.cutsceneStarter(...)
StoryBridge.questStarter(...)
StoryBridge.questCompleter(...)

StoryActions.register(type, mapCodec)           // 娉ㄥ唽鑷畾涔夊姩浣滅被鍨?StoryActions.registered()                       // 宸叉敞鍐岀殑绫诲瀷

Cutscenes.register(id, definition)              // 浠ｇ爜鍐呮敞鍐?浼樺厛浜庢暟鎹寘
Dialogues.register(id, definition)
Quests.register(id, definition)
Quests.allIds(player)                           // 浠ｇ爜娉ㄥ唽涓庢暟鎹寘娉ㄥ唽鐨勫悎骞?```

---

## 澶氫汉

瀵硅瘽鍜屾紨鍑洪兘鎸夌帺瀹堕殧绂?浼氳瘽琛ㄩ敭鏄帺瀹?UUID 涓や釜鐜╁鍚屾椂鎵惧悓涓€涓?NPC 鍚勮亰鍚勭殑 浜掍笉骞叉壈 鍚屼竴涓汉韬笂涔熶笉浼氬悓鏃跺紑涓ゅ満 鍚庢潵鐨勯《鎺夊厛鍓嶇殑

涓栫晫鐘舵€佹槸鍏ㄥ眬鐨?涓€涓汉鏀逛簡鍓ф儏闃舵 鎵€鏈変汉绔嬪埢鐪嬪埌 鐜╁鐘舵€佹槸绉佹湁鐨?鍙帹缁欐湰浜?
鎯宠涓€缇や汉涓€璧风湅鍚屼竴鍦烘紨鍑?鐢?`Cutscenes.playFor(players, id)` 鎸夎寖鍥存寫浜哄氨鐢?`playNear(anchor, radius, id)`

---

## 鍙栬垗鍜屽潙

**`play_cutscene` 榛樿涓嶉樆濉?* 瑕佺瓑瀹冩紨瀹屽啀鍔?`await`

**瀹㈡埛绔敼涓嶄簡涓栫晫鐘舵€?* 鐜板湪浼氭妸鏁翠釜鐘舵€佸箍鎾洖瀹㈡埛绔?涓€鏃﹀鎴风涔熻兘鍐欏氨浼氭潵鍥炴墦鏋?濡傛灉闇€瑕?鐜╁閫夋嫨浜嗘敼鍙樹笘鐣?杩欑璁捐 寰楄ˉ涓€涓鎴风鍒版湇鍔＄鐨勫寘 鐩墠娌″仛

**婕斿嚭闊虫晥璧板鎴风鎾斁** 杩欐牱鎵嶅拰鐢婚潰鍚屽抚 浠ｄ环鏄畠涓嶈窡闅忎笘鐣屽潗鏍?瑕佹煇涓柟鍧椾綅缃殑绔嬩綋澹拌繕寰楃敤 `command` 鍔ㄤ綔鎵ц playsound

**鐩告満鎺ョ鐢ㄤ簡 Mixin** 鍜屽叾浠栦慨鏀圭浉鏈轰綅缃垨瑙嗚鐨勬ā缁勫彲鑳戒簰鐩歌鐩?璋佺敓鏁堢湅鍔犺浇椤哄簭 鍙敼瑙掑害鐨勬ā缁勪笉鍙楀奖鍝?
**瀵硅瘽鏂偣鍙湪鎺夌嚎鏃朵繚鐣?* 涓诲姩鎸?Esc 鍏虫帀绛変簬缁撴潫 鎯充笅娆℃帴鐫€鑱婂氨鍒叧

**浠诲姟姣?20 tick 妫€鏌ヤ竴娆?* 瀹屾垚鍜屽け璐ュ垽瀹氶兘鍦ㄨ繖涓€杞噷璺?渚濊禆璁℃暟鍣ㄧ殑浠诲姟鏈€澶氭湁 1 绉掑欢杩?闇€瑕佸嵆鏃跺弽棣堝氨鐩存帴璋?`Quests.refresh(player)`

**鏇胯韩鐨勫姩浣滃垏鎹㈡槸纭殑** 鍏抽敭甯т箣闂翠綅缃拰鏈濆悜鎻掑€?浣?`motion` 鏄垏鎹㈢殑 涓や釜涓嶅悓鍔ㄤ綔浜ょ晫澶勪細璺充竴涓?涓棿澶氱粰涓€甯ц兘缂撹В浣嗛櫎涓嶆帀 鎯宠鐪熸杩炶疮鐨勫姩浣滃緱涓婇楠煎姩鐢绘柟妗?
**GeckoLib 鐨?`loop` 鐩墠涓嶇敓鏁?* 閭ｄ釜寮€鍏冲湪鍔ㄧ敾瀹氫箟閲屼笉鍦ㄦ帶鍒跺櫒涓?鍙嶅皠澶熶笉鐫€ 瀛楁鍏堢暀鐫€鍗犱綅 `speed` 鏄敖鍔涜€屼负 鍙嶅皠澶辫触灏辨寜鍔ㄧ敾鍘熸湰鐨勯€熷害鎾?
**ambient 妯″紡鐢ㄦ暟瀛楅敭閫夐€夐」** 鍥犱负瀹冧笉寮€鐣岄潰 鐜╁瑕佷繚鎸佽兘璧板姩灏辨病娉曠敤榧犳爣鐐?cinematic 鏄浉鍙嶇殑鍙栬垗 榧犳爣鑳界偣浣嗕笉鑳借蛋

**cinematic 榛戝箷鏄暣鏉℃í璺ㄥ睆骞曠殑** 瀹冨彧鍗犲簳閮?浣嗗鏋滄妸 `duration` 鎷夊緱寰堢煭 榛戝箷楂樺害浼氭寜鍐呭鎾戝埌瓒呰繃灞忓箷涓夊垎涔嬩竴

**鏁版嵁鍖呮敞鍐岃〃鍙湪涓栫晫鍔犺浇鏃惰涓€娆?* `/reload` 涓嶄細閲嶅缓瀹冧滑 鎹簡 jar 鎴栬€呮敼浜?`data/` 涓嬬殑鏂囦欢涔嬪悗瑕侀€€鍑轰笘鐣岄噸杩?鑰佸瓨妗ｈ繕浼氬甫鐫€宸茬煡鏁版嵁鍖呭垪琛?鎺掓煡鏃跺厛鐢ㄦ柊瀛樻。纭涓€娆?
**鍚姩鏃ュ織閲屾湁涓夎鑷** 鏈嶅姟绔捣濂戒箣鍚庝細鎵撳嵃

```
[sqdclib] story_quest holds 2 entries [sqdclib:first_relic, sqdclib:prologue] (loaded from data/<pack_namespace>/sqdclib/story_quest/)
```

濡傛灉鐪嬪埌 `is registered but holds no entries` 璇存槑娉ㄥ唽琛ㄥ缓鍑烘潵浜嗕絾涓€涓枃浠堕兘娌¤鍒?妫€鏌ヨ矾寰勬槸涓嶆槸灏戜簡涓€灞?`sqdclib/` 濡傛灉鐪嬪埌 `was never created` 璇存槑 `DataPackRegistryEvent` 娌¤窇鍒?閭ｆ槸搴撲晶鐨勯棶棰?
---

## 璁稿彲

MIT  瀹屾暣鏉℃瑙佷粨搴撴牴鐩綍鐨?LICENSE 鏂囦欢
