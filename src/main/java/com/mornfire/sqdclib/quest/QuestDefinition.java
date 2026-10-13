package com.mornfire.sqdclib.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import com.mornfire.sqdclib.storystate.condition.StoryCondition;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public record QuestDefinition(
        Component title,
        Optional<Component> description,
        QuestKind kind,
        List<ResourceLocation> prerequisites,
        Optional<StoryCondition> visibleWhen,
        List<QuestObjective> objectives,
        Optional<StoryCondition> failWhen,
        List<StoryAction> onStart,
        List<StoryAction> onComplete,
        List<StoryAction> onFail) {

    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("title").forGetter(QuestDefinition::title),
            ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(QuestDefinition::description),
            QuestKind.CODEC.optionalFieldOf("kind", QuestKind.MAIN).forGetter(QuestDefinition::kind),
            ResourceLocation.CODEC.listOf().optionalFieldOf("prerequisites", List.of())
                    .forGetter(QuestDefinition::prerequisites),
            StoryCondition.CODEC.optionalFieldOf("visible_when").forGetter(QuestDefinition::visibleWhen),
            QuestObjective.CODEC.listOf().optionalFieldOf("objectives", List.of())
                    .forGetter(QuestDefinition::objectives),
            StoryCondition.CODEC.optionalFieldOf("fail_when").forGetter(QuestDefinition::failWhen),
            StoryAction.CODEC.listOf().optionalFieldOf("on_start", List.of()).forGetter(QuestDefinition::onStart),
            StoryAction.CODEC.listOf().optionalFieldOf("on_complete", List.of())
                    .forGetter(QuestDefinition::onComplete),
            StoryAction.CODEC.listOf().optionalFieldOf("on_fail", List.of()).forGetter(QuestDefinition::onFail))
            .apply(instance, QuestDefinition::new));
}
