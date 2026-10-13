package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public record DialogueDefinition(int schema, String start, boolean lockMovement, Map<String, DialogueNode> nodes) {
    public static final int CURRENT_SCHEMA = 1;

    public static final Codec<DialogueDefinition> CODEC = RecordCodecBuilder.<DialogueDefinition>create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(DialogueDefinition::schema),
            Codec.STRING.fieldOf("start").forGetter(DialogueDefinition::start),
            Codec.BOOL.optionalFieldOf("lock_movement", false).forGetter(DialogueDefinition::lockMovement),
            Codec.unboundedMap(Codec.STRING, DialogueNode.CODEC).fieldOf("nodes")
                    .forGetter(DialogueDefinition::nodes))
            .apply(instance, DialogueDefinition::new))
            .validate(DialogueDefinition::validateSchema);

    private static DataResult<DialogueDefinition> validateSchema(DialogueDefinition definition) {
        if (definition.schema() > CURRENT_SCHEMA) {
            return DataResult.error(() -> "dialogue schema " + definition.schema()
                    + " is newer than this build supports (" + CURRENT_SCHEMA + ")");
        }
        return DataResult.success(definition);
    }
}
