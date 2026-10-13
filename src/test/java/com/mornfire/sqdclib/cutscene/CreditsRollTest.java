package com.mornfire.sqdclib.cutscene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CreditsRollTest {
    @Test
    void contentHeightAddsLinesAndTheirGaps() {
        assertEquals(45, CreditsRoll.contentHeight(new int[] {10, 10, 10}, new int[] {5, 5, 5}));
    }

    @Test
    void contentHeightToleratesMissingGaps() {
        assertEquals(20, CreditsRoll.contentHeight(new int[] {10, 10}, new int[] {0}));
    }

    @Test
    void emptyContentHasZeroHeight() {
        assertEquals(0, CreditsRoll.contentHeight(new int[0], new int[0]));
    }

    @Test
    void startsJustBelowTheViewport() {
        assertEquals(200, CreditsRoll.offset(0.0F, 200, 50));
    }

    @Test
    void endsJustAboveTheViewport() {
        assertEquals(-50, CreditsRoll.offset(1.0F, 200, 50));
    }

    @Test
    void halfwayThrough() {
        assertEquals(75, CreditsRoll.offset(0.5F, 200, 50));
    }

    @Test
    void offsetIsClampedOutsideTheUnitRange() {
        assertEquals(200, CreditsRoll.offset(-3.0F, 200, 50));
        assertEquals(-50, CreditsRoll.offset(4.0F, 200, 50));
    }

    @Test
    void progressDoesNotStartBeforeItsStartTick() {
        assertEquals(0.0F, CreditsRoll.progress(10.0F, 50, 100, false), 1.0e-6F);
    }

    @Test
    void progressIsClampedAtBothEnds() {
        assertEquals(1.0F, CreditsRoll.progress(500.0F, 0, 100, false), 1.0e-6F);
        assertEquals(0.0F, CreditsRoll.progress(-20.0F, 0, 100, false), 1.0e-6F);
    }

    @Test
    void speedUpEasesInQuadratically() {
        assertEquals(0.25F, CreditsRoll.progress(50.0F, 0, 100, true), 1.0e-6F);
        assertEquals(0.5F, CreditsRoll.progress(50.0F, 0, 100, false), 1.0e-6F);
    }

    @Test
    void zeroDurationDoesNotDivideByZero() {
        assertEquals(1.0F, CreditsRoll.progress(10.0F, 0, 0, false), 1.0e-6F);
    }
}
