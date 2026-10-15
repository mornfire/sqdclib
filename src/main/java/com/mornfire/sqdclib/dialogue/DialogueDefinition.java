package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public record DialogueDefinition(
        int schema,
        String start,
        DialogueMode mode,
        boolean lockMovement,
        DialoguePlacement placement,
        int textSpeed,
        int speakerColor,
        Map<String, DialogueNode> nodes) {

    public static final int CURRENT_SCHEMA = 3;

    public static final Codec<DialogueDefinition> CODEC = RecordCodecBuilder.<DialogueDefinition>create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(DialogueDefinition::schema),
            Codec.STRING.fieldOf("start").forGetter(DialogueDefinition::start),
            DialogueMode.CODEC.optionalFieldOf("mode", DialogueMode.AMBIENT).forGetter(DialogueDefinition::mode),
            Codec.BOOL.optionalFieldOf("lock_movement", false).forGetter(DialogueDefinition::lockMovement),
            DialoguePlacement.CODEC.optionalFieldOf("placement", DialoguePlacement.BOTTOM)
                    .forGetter(DialogueDefinition::placement),
            Codec.INT.optionalFieldOf("text_speed", 0).forGetter(DialogueDefinition::textSpeed),
            Codec.INT.optionalFieldOf("speaker_color", 0xFFFFD24A).forGetter(DialogueDefinition::speakerColor),
            Codec.unboundedMap(Codec.STRING, DialogueNode.CODEC).fieldOf("nodes")
                    .forGetter(DialogueDefinition::nodes))
            .apply(instance, DialogueDefinition::new))
            .validate(DialogueDefinition::validateSchema);

    public boolean cinematic() {
        return mode == DialogueMode.CINEMATIC;
    }

    private static DataResult<DialogueDefinition> validateSchema(DialogueDefinition definition) {
        if (definition.schema() > CURRENT_SCHEMA) {
            return DataResult.error(() -> "dialogue schema " + definition.schema()
                    + " is newer than this build supports (" + CURRENT_SCHEMA + ")");
        }
        return DataResult.success(definition);
    }
}
