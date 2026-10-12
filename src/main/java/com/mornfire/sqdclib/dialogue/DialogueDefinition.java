package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public record DialogueDefinition(String start, Map<String, DialogueNode> nodes) {
    public static final Codec<DialogueDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("start").forGetter(DialogueDefinition::start),
            Codec.unboundedMap(Codec.STRING, DialogueNode.CODEC).fieldOf("nodes")
                    .forGetter(DialogueDefinition::nodes))
            .apply(instance, DialogueDefinition::new));
}
