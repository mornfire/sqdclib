package com.mornfire.sqdclib.storystate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class Story {
    private Story() {}

    public static boolean hasFlag(Player player, String flag) {
        return hasFlag(player, FlagScope.ANY, flag);
    }

    public static boolean hasFlag(Player player, FlagScope scope, String flag) {
        return switch (scope) {
            case PLAYER -> StoryProgress.of(player).hasFlag(flag);
            case WORLD -> WorldState.get(player.level()).hasFlag(flag);
            case ANY -> StoryProgress.of(player).hasFlag(flag)
                    || WorldState.get(player.level()).hasFlag(flag);
        };
    }

    public static boolean setFlag(Player player, String flag) {
        return StoryProgress.of(player).setFlag(flag);
    }

    public static boolean setFlag(Player player, String flag, boolean value) {
        return StoryProgress.of(player).setFlag(flag, value);
    }

    public static boolean clearFlag(Player player, String flag) {
        return StoryProgress.of(player).setFlag(flag, false);
    }

    public static int getCounter(Player player, String key) {
        return StoryProgress.of(player).getCounter(key);
    }

    public static int setCounter(Player player, String key, int value) {
        return StoryProgress.of(player).setCounter(key, value);
    }

    public static int addCounter(Player player, String key, int delta) {
        return StoryProgress.of(player).addCounter(key, delta);
    }

    public static boolean completeQuest(Player player, ResourceLocation quest) {
        return StoryProgress.of(player).completeQuest(quest);
    }

    public static boolean isQuestCompleted(Player player, ResourceLocation quest) {
        return StoryProgress.of(player).isQuestCompleted(quest);
    }

    public static String getString(Player player, String key) {
        return StoryProgress.of(player).getString(key);
    }

    public static boolean setString(Player player, String key, String value) {
        return StoryProgress.of(player).setString(key, value);
    }

    public static boolean clearString(Player player, String key) {
        return StoryProgress.of(player).clearString(key);
    }
}
