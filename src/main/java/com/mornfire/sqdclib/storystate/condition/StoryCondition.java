package com.mornfire.sqdclib.storystate.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.FlagScope;
import com.mornfire.sqdclib.storystate.Story;
import com.mornfire.sqdclib.storystate.WorldState;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public sealed interface StoryCondition {
    boolean test(Player player);

    String type();

    String explain(Player player);

    Codec<StoryCondition> CODEC = Codec.recursive("StoryCondition", StoryCondition::buildCodec);

    private static Codec<StoryCondition> buildCodec(Codec<StoryCondition> self) {
        return Codec.STRING.partialDispatch(
                "type",
                condition -> DataResult.success(condition.type()),
                name -> switch (name) {
                    case Flag.TYPE -> DataResult.success(Flag.codec());
                    case Quest.TYPE -> DataResult.success(Quest.codec());
                    case Counter.TYPE -> DataResult.success(Counter.codec());
                    case Phase.TYPE -> DataResult.success(Phase.codec());
                    case Negate.TYPE -> DataResult.success(Negate.codec(self));
                    case All.TYPE -> DataResult.success(All.codec(self));
                    case Any.TYPE -> DataResult.success(Any.codec(self));
                    default -> DataResult.error(() -> "Unknown story condition type: " + name);
                });
    }

    record Flag(String flag, FlagScope scope, boolean value) implements StoryCondition {
        public static final String TYPE = "flag";

        public static MapCodec<Flag> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("flag").forGetter(Flag::flag),
                    FlagScope.CODEC.optionalFieldOf("scope", FlagScope.ANY).forGetter(Flag::scope),
                    Codec.BOOL.optionalFieldOf("value", true).forGetter(Flag::value))
                    .apply(instance, Flag::new));
        }

        @Override
        public boolean test(Player player) {
            return Story.hasFlag(player, scope, flag) == value;
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            return "flag \"" + flag + "\" (" + scope.getSerializedName() + ") is "
                    + Story.hasFlag(player, scope, flag) + ", expected " + value;
        }
    }

    record Quest(ResourceLocation quest) implements StoryCondition {
        public static final String TYPE = "quest";

        public static MapCodec<Quest> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("quest").forGetter(Quest::quest))
                    .apply(instance, Quest::new));
        }

        @Override
        public boolean test(Player player) {
            return Story.isQuestCompleted(player, quest);
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            return "quest " + quest + " completed=" + Story.isQuestCompleted(player, quest);
        }
    }

    record Counter(String key, int atLeast) implements StoryCondition {
        public static final String TYPE = "counter";

        public static MapCodec<Counter> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("key").forGetter(Counter::key),
                    Codec.INT.optionalFieldOf("at_least", 1).forGetter(Counter::atLeast))
                    .apply(instance, Counter::new));
        }

        @Override
        public boolean test(Player player) {
            return Story.getCounter(player, key) >= atLeast;
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            return "counter \"" + key + "\" is " + Story.getCounter(player, key) + ", need >= " + atLeast;
        }
    }

    record Phase(String phase) implements StoryCondition {
        public static final String TYPE = "phase";

        public static MapCodec<Phase> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("phase").forGetter(Phase::phase))
                    .apply(instance, Phase::new));
        }

        @Override
        public boolean test(Player player) {
            return WorldState.get(player.level()).getPhase().equals(phase);
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            return "phase is \"" + WorldState.get(player.level()).getPhase() + "\", expected \"" + phase + "\"";
        }
    }

    record Negate(StoryCondition value) implements StoryCondition {
        public static final String TYPE = "not";

        public static MapCodec<Negate> codec(Codec<StoryCondition> self) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    self.fieldOf("value").forGetter(Negate::value))
                    .apply(instance, Negate::new));
        }

        @Override
        public boolean test(Player player) {
            return !value.test(player);
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            return "not( " + value.explain(player) + " )";
        }
    }

    record All(List<StoryCondition> values) implements StoryCondition {
        public static final String TYPE = "all";

        public static MapCodec<All> codec(Codec<StoryCondition> self) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    self.listOf().fieldOf("values").forGetter(All::values))
                    .apply(instance, All::new));
        }

        @Override
        public boolean test(Player player) {
            for (StoryCondition condition : values) {
                if (!condition.test(player)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            StringBuilder builder = new StringBuilder("all(");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                StoryCondition condition = values.get(i);
                boolean ok = condition.test(player);
                builder.append(i).append(':').append(ok ? "ok" : "fail");
                if (!ok) {
                    builder.append(" [").append(condition.explain(player)).append(']');
                }
            }
            return builder.append(')').toString();
        }
    }

    record Any(List<StoryCondition> values) implements StoryCondition {
        public static final String TYPE = "any";

        public static MapCodec<Any> codec(Codec<StoryCondition> self) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    self.listOf().fieldOf("values").forGetter(Any::values))
                    .apply(instance, Any::new));
        }

        @Override
        public boolean test(Player player) {
            for (StoryCondition condition : values) {
                if (condition.test(player)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String type() {
            return TYPE;
        }

        @Override
        public String explain(Player player) {
            StringBuilder builder = new StringBuilder("any(");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                StoryCondition condition = values.get(i);
                boolean ok = condition.test(player);
                builder.append(i).append(':').append(ok ? "ok" : "fail");
                if (!ok) {
                    builder.append(" [").append(condition.explain(player)).append(']');
                }
            }
            return builder.append(')').toString();
        }
    }
}
