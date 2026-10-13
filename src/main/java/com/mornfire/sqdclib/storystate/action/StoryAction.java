package com.mornfire.sqdclib.storystate.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.FlagScope;
import com.mornfire.sqdclib.storystate.StoryProgress;
import com.mornfire.sqdclib.storystate.WorldState;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public sealed interface StoryAction {
    void run(ServerPlayer player);

    String type();

    Codec<StoryAction> CODEC = Codec.recursive("StoryAction", StoryAction::buildCodec);

    static void runAll(ServerPlayer player, List<StoryAction> actions) {
        for (StoryAction action : actions) {
            action.run(player);
        }
    }

    static void runSequentially(ServerPlayer player, List<StoryAction> actions, Runnable onComplete) {
        runFrom(player, actions, 0, onComplete);
    }

    private static void runFrom(ServerPlayer player, List<StoryAction> actions, int index, Runnable onComplete) {
        for (int i = index; i < actions.size(); i++) {
            StoryAction action = actions.get(i);
            if (action instanceof PlayCutscene play && play.await()) {
                int resume = i + 1;
                if (StoryBridge.playCutscene(player, play.cutscene(),
                        () -> runFrom(player, actions, resume, onComplete))) {
                    return;
                }
                continue;
            }
            action.run(player);
        }
        onComplete.run();
    }

    private static Codec<StoryAction> buildCodec(Codec<StoryAction> self) {
        return Codec.STRING.partialDispatch(
                "type",
                action -> DataResult.success(action.type()),
                name -> switch (name) {
                    case SetFlag.TYPE -> DataResult.success(SetFlag.codec());
                    case AddCounter.TYPE -> DataResult.success(AddCounter.codec());
                    case SetPhase.TYPE -> DataResult.success(SetPhase.codec());
                    case Command.TYPE -> DataResult.success(Command.codec());
                    case StartQuest.TYPE -> DataResult.success(StartQuest.codec());
                    case CompleteQuest.TYPE -> DataResult.success(CompleteQuest.codec());
                    case StartDialogue.TYPE -> DataResult.success(StartDialogue.codec());
                    case PlayCutscene.TYPE -> DataResult.success(PlayCutscene.codec());
                    default -> DataResult.error(() -> "Unknown story action type: " + name);
                });
    }

    record SetFlag(String flag, boolean value, FlagScope scope) implements StoryAction {
        public static final String TYPE = "set_flag";

        public static MapCodec<SetFlag> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("flag").forGetter(SetFlag::flag),
                    Codec.BOOL.optionalFieldOf("value", true).forGetter(SetFlag::value),
                    FlagScope.CODEC.optionalFieldOf("scope", FlagScope.PLAYER).forGetter(SetFlag::scope))
                    .apply(instance, SetFlag::new));
        }

        @Override
        public void run(ServerPlayer player) {
            if (scope == FlagScope.WORLD) {
                WorldState.get(player.serverLevel()).setFlag(flag, value);
            } else {
                StoryProgress.of(player).setFlag(flag, value);
            }
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record AddCounter(String key, int amount) implements StoryAction {
        public static final String TYPE = "add_counter";

        public static MapCodec<AddCounter> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("key").forGetter(AddCounter::key),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(AddCounter::amount))
                    .apply(instance, AddCounter::new));
        }

        @Override
        public void run(ServerPlayer player) {
            StoryProgress.of(player).addCounter(key, amount);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record SetPhase(String phase) implements StoryAction {
        public static final String TYPE = "set_phase";

        public static MapCodec<SetPhase> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("phase").forGetter(SetPhase::phase))
                    .apply(instance, SetPhase::new));
        }

        @Override
        public void run(ServerPlayer player) {
            WorldState.get(player.serverLevel()).setPhase(phase);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record Command(String command) implements StoryAction {
        public static final String TYPE = "command";

        public static MapCodec<Command> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("command").forGetter(Command::command))
                    .apply(instance, Command::new));
        }

        @Override
        public void run(ServerPlayer player) {
            MinecraftServer server = player.serverLevel().getServer();
            server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record StartQuest(ResourceLocation quest) implements StoryAction {
        public static final String TYPE = "start_quest";

        public static MapCodec<StartQuest> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("quest").forGetter(StartQuest::quest))
                    .apply(instance, StartQuest::new));
        }

        @Override
        public void run(ServerPlayer player) {
            StoryBridge.startQuest(player, quest);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record CompleteQuest(ResourceLocation quest) implements StoryAction {
        public static final String TYPE = "complete_quest";

        public static MapCodec<CompleteQuest> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("quest").forGetter(CompleteQuest::quest))
                    .apply(instance, CompleteQuest::new));
        }

        @Override
        public void run(ServerPlayer player) {
            StoryBridge.completeQuest(player, quest);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record StartDialogue(ResourceLocation dialogue) implements StoryAction {
        public static final String TYPE = "start_dialogue";

        public static MapCodec<StartDialogue> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("dialogue").forGetter(StartDialogue::dialogue))
                    .apply(instance, StartDialogue::new));
        }

        @Override
        public void run(ServerPlayer player) {
            StoryBridge.startDialogue(player, dialogue);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record PlayCutscene(ResourceLocation cutscene, boolean await) implements StoryAction {
        public static final String TYPE = "play_cutscene";

        public static MapCodec<PlayCutscene> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("cutscene").forGetter(PlayCutscene::cutscene),
                    Codec.BOOL.optionalFieldOf("await", false).forGetter(PlayCutscene::await))
                    .apply(instance, PlayCutscene::new));
        }

        @Override
        public void run(ServerPlayer player) {
            StoryBridge.playCutscene(player, cutscene);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }
}
