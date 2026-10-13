package com.mornfire.sqdclib.quest;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.quest.event.QuestCompletedEvent;
import com.mornfire.sqdclib.quest.event.QuestFailedEvent;
import com.mornfire.sqdclib.quest.event.QuestStartedEvent;
import com.mornfire.sqdclib.storystate.Story;
import com.mornfire.sqdclib.storystate.StoryProgress;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import com.mornfire.sqdclib.storystate.condition.StoryCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

public final class Quests {
    public static final ResourceKey<Registry<QuestDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "story_quest"));

    public static Registry<QuestDefinition> registry(Player player) {
        return player.level().registryAccess().registryOrThrow(REGISTRY_KEY);
    }

    public static Optional<QuestDefinition> definition(Player player, ResourceLocation quest) {
        return registry(player).getOptional(quest);
    }

    public static QuestStatus status(ServerPlayer player, ResourceLocation quest) {
        StoryProgress progress = StoryProgress.of(player);
        if (progress.isQuestCompleted(quest)) {
            return QuestStatus.COMPLETED;
        }
        if (progress.hasFailedQuest(quest)) {
            return QuestStatus.FAILED;
        }
        if (progress.isQuestActive(quest)) {
            return QuestStatus.ACTIVE;
        }
        Optional<QuestDefinition> definition = definition(player, quest);
        if (definition.isEmpty()) {
            return QuestStatus.LOCKED;
        }
        return hasPrerequisites(player, definition.get()) ? QuestStatus.AVAILABLE : QuestStatus.LOCKED;
    }

    public static boolean hasPrerequisites(Player player, QuestDefinition definition) {
        StoryProgress progress = StoryProgress.of(player);
        for (ResourceLocation prerequisite : definition.prerequisites()) {
            if (!progress.isQuestCompleted(prerequisite)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isVisible(Player player, ResourceLocation quest) {
        return definition(player, quest)
                .map(definition -> definition.visibleWhen().isEmpty()
                        || definition.visibleWhen().get().test(player))
                .orElse(false);
    }

    public static boolean isReady(Player player, QuestDefinition definition) {
        if (definition.objectives().isEmpty()) {
            return false;
        }
        for (QuestObjective objective : definition.objectives()) {
            if (!objective.done().test(player)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFailed(Player player, QuestDefinition definition) {
        return definition.failWhen().isPresent() && definition.failWhen().get().test(player);
    }

    public static Optional<Component> objectiveProgress(Player player, QuestObjective objective) {
        return objective.progress().map(spec -> {
            int current = spec.current(Story.getCounter(player, spec.counter()));
            return (Component) Component.literal(current + "/" + spec.max());
        });
    }

    public static Component objectiveTitle(Player player, QuestObjective objective) {
        Optional<Component> progress = objectiveProgress(player, objective);
        if (progress.isEmpty()) {
            return objective.title();
        }
        return Component.empty()
                .append(objective.title())
                .append(" (")
                .append(progress.get())
                .append(")");
    }

    public static List<String> explain(Player player, ResourceLocation quest) {
        List<String> lines = new ArrayList<>();
        Optional<QuestDefinition> definition = definition(player, quest);
        if (definition.isEmpty()) {
            lines.add("quest " + quest + " is not registered");
            return lines;
        }
        QuestDefinition resolved = definition.get();
        lines.add("schema=" + resolved.schema() + "  kind=" + resolved.kind().getSerializedName()
                + "  status=" + (player instanceof ServerPlayer serverPlayer
                        ? status(serverPlayer, quest).name()
                        : "n/a"));
        for (QuestObjective objective : resolved.objectives()) {
            boolean done = objective.done().test(player);
            lines.add("objective " + objective.id() + " [" + (done ? "ok" : "FAILED") + "] "
                    + objective.done().explain(player));
        }
        if (resolved.failWhen().isPresent()) {
            StoryCondition fail = resolved.failWhen().get();
            lines.add("fail_when [" + (fail.test(player) ? "TRIGGERED" : "ok") + "] " + fail.explain(player));
        }
        return lines;
    }

    public static boolean start(ServerPlayer player, ResourceLocation quest) {
        if (status(player, quest) != QuestStatus.AVAILABLE) {
            return false;
        }
        Optional<QuestDefinition> definition = definition(player, quest);
        if (definition.isEmpty()) {
            return false;
        }
        StoryProgress.of(player).activateQuest(quest);
        StoryAction.runAll(player, definition.get().onStart());
        NeoForge.EVENT_BUS.post(new QuestStartedEvent(player, quest));
        return true;
    }

    public static boolean complete(ServerPlayer player, ResourceLocation quest) {
        StoryProgress progress = StoryProgress.of(player);
        if (!progress.isQuestActive(quest)) {
            return false;
        }
        progress.deactivateQuest(quest);
        progress.completeQuest(quest);
        definition(player, quest).ifPresent(definition -> StoryAction.runAll(player, definition.onComplete()));
        NeoForge.EVENT_BUS.post(new QuestCompletedEvent(player, quest));
        return true;
    }

    public static boolean fail(ServerPlayer player, ResourceLocation quest) {
        StoryProgress progress = StoryProgress.of(player);
        if (!progress.isQuestActive(quest)) {
            return false;
        }
        progress.deactivateQuest(quest);
        progress.failQuest(quest);
        definition(player, quest).ifPresent(definition -> StoryAction.runAll(player, definition.onFail()));
        NeoForge.EVENT_BUS.post(new QuestFailedEvent(player, quest));
        return true;
    }

    public static boolean abandon(ServerPlayer player, ResourceLocation quest) {
        StoryProgress progress = StoryProgress.of(player);
        if (!progress.isQuestActive(quest)) {
            return false;
        }
        return progress.deactivateQuest(quest);
    }

    public static boolean reset(ServerPlayer player, ResourceLocation quest) {
        StoryProgress progress = StoryProgress.of(player);
        boolean changed = progress.deactivateQuest(quest);
        changed |= progress.clearFailedQuest(quest);
        changed |= progress.revokeQuest(quest);
        return changed;
    }

    public static List<ResourceLocation> refresh(ServerPlayer player) {
        List<ResourceLocation> changed = new ArrayList<>();
        for (ResourceLocation quest : List.copyOf(StoryProgress.of(player).getActiveQuests())) {
            Optional<QuestDefinition> definition = definition(player, quest);
            if (definition.isEmpty()) {
                continue;
            }
            QuestDefinition resolved = definition.get();
            if (isFailed(player, resolved)) {
                if (fail(player, quest)) {
                    changed.add(quest);
                }
                continue;
            }
            if (isReady(player, resolved) && complete(player, quest)) {
                changed.add(quest);
            }
        }
        return changed;
    }

    private Quests() {}
}
