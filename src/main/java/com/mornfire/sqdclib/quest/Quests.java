package com.mornfire.sqdclib.quest;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.quest.event.QuestCompletedEvent;
import com.mornfire.sqdclib.quest.event.QuestStartedEvent;
import com.mornfire.sqdclib.storystate.StoryProgress;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Registry;
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

    public static List<ResourceLocation> refresh(ServerPlayer player) {
        List<ResourceLocation> completed = new ArrayList<>();
        for (ResourceLocation quest : List.copyOf(StoryProgress.of(player).getActiveQuests())) {
            Optional<QuestDefinition> definition = definition(player, quest);
            if (definition.isPresent() && isReady(player, definition.get()) && complete(player, quest)) {
                completed.add(quest);
            }
        }
        return completed;
    }

    private Quests() {}
}
