package com.mornfire.sqdclib.cutscene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EasingTest {
    @Test
    void linearIsIdentity() {
        assertEquals(0.25F, Easing.LINEAR.apply(0.25F), 1.0e-6F);
        assertEquals(0.75F, Easing.LINEAR.apply(0.75F), 1.0e-6F);
    }

    @Test
    void everyCurveAnchorsAtBothEnds() {
        for (Easing easing : Easing.values()) {
            assertEquals(0.0F, easing.apply(0.0F), 1.0e-6F, easing.name() + " start");
            assertEquals(1.0F, easing.apply(1.0F), 1.0e-6F, easing.name() + " end");
        }
    }

    @Test
    void everyCurveIsMonotonic() {
        for (Easing easing : Easing.values()) {
            float previous = -1.0F;
            for (int step = 0; step <= 40; step++) {
                float value = easing.apply(step / 40.0F);
                assertTrue(value >= previous, easing.name() + " went backwards at step " + step);
                previous = value;
            }
        }
    }

    @Test
    void inputIsClampedToTheUnitRange() {
        for (Easing easing : Easing.values()) {
            assertEquals(0.0F, easing.apply(-5.0F), 1.0e-6F, easing.name() + " below zero");
            assertEquals(1.0F, easing.apply(5.0F), 1.0e-6F, easing.name() + " above one");
        }
    }

    @Test
    void easeInLagsAndEaseOutLeads() {
        assertTrue(Easing.EASE_IN.apply(0.5F) < 0.5F, "ease_in should lag at the midpoint");
        assertTrue(Easing.EASE_OUT.apply(0.5F) > 0.5F, "ease_out should lead at the midpoint");
        assertEquals(0.5F, Easing.EASE_IN_OUT.apply(0.5F), 1.0e-6F);
    }
}
