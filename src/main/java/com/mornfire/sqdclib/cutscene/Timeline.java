package com.mornfire.sqdclib.cutscene;

import org.jetbrains.annotations.Nullable;

public final class Timeline {
    public record Segment(int from, int to, float progress) {}

    @Nullable
    public static Segment locate(int[] times, float time) {
        if (times.length == 0) {
            return null;
        }
        int last = times.length - 1;
        if (last == 0 || time <= times[0]) {
            return new Segment(0, 0, 0.0F);
        }
        if (time >= times[last]) {
            return new Segment(last, last, 1.0F);
        }
        for (int i = 0; i < last; i++) {
            int from = times[i];
            int to = times[i + 1];
            if (time >= from && time <= to) {
                int span = to - from;
                return new Segment(i, i + 1, span <= 0 ? 1.0F : (time - from) / span);
            }
        }
        return new Segment(last, last, 1.0F);
    }

    private Timeline() {}
}
