package com.mornfire.sqdclib.storystate;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

public final class StoryText {
    public static final char OPEN = '%';

    public static Component resolve(Player player, Component source) {
        if (!mayContainPlaceholder(source)) {
            return source;
        }
        MutableComponent result = Component.empty();
        source.visit((style, text) -> {
            result.append(Component.literal(interpolate(player, text)).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    public static String interpolate(Player player, String text) {
        if (text.indexOf(OPEN) < 0) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int cursor = 0;
        while (cursor < text.length()) {
            int start = text.indexOf(OPEN, cursor);
            if (start < 0) {
                builder.append(text, cursor, text.length());
                break;
            }
            int end = text.indexOf(OPEN, start + 1);
            if (end < 0) {
                builder.append(text, cursor, text.length());
                break;
            }
            builder.append(text, cursor, start);
            builder.append(Story.getString(player, text.substring(start + 1, end)));
            cursor = end + 1;
        }
        return builder.toString();
    }

    private static boolean mayContainPlaceholder(Component source) {
        if (source.getString().indexOf(OPEN) >= 0) {
            return true;
        }
        for (Component sibling : source.getSiblings()) {
            if (mayContainPlaceholder(sibling)) {
                return true;
            }
        }
        return false;
    }

    private StoryText() {}
}
