package com.mornfire.sqdclib.cutscene;

public final class CreditsRoll {
    public static int contentHeight(int[] lineHeights, int[] gaps) {
        int total = 0;
        for (int i = 0; i < lineHeights.length; i++) {
            total += lineHeights[i];
            if (i < gaps.length) {
                total += gaps[i];
            }
        }
        return total;
    }

    public static int offset(float progress, int viewportHeight, int contentHeight) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        return (int) (viewportHeight - clamped * (viewportHeight + contentHeight));
    }

    public static float progress(float elapsedTicks, int start, int duration, boolean speedUp) {
        float raw = (elapsedTicks - start) / (float) Math.max(1, duration);
        float clamped = Math.max(0.0F, Math.min(1.0F, raw));
        return speedUp ? clamped * clamped : clamped;
    }

    private CreditsRoll() {}
}
