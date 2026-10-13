package com.mornfire.sqdclib.cutscene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimelineTest {
    @Test
    void emptyTimelineHasNoSegment() {
        assertNull(Timeline.locate(new int[0], 5.0F));
    }

    @Test
    void singleKeyframeHoldsItsOwnValue() {
        Timeline.Segment segment = Timeline.locate(new int[] {40}, 999.0F);
        assertEquals(0, segment.from());
        assertEquals(0, segment.to());
    }

    @Test
    void beforeFirstKeyframeClampsToStart() {
        Timeline.Segment segment = Timeline.locate(new int[] {10, 50}, 0.0F);
        assertEquals(0, segment.from());
        assertEquals(0, segment.to());
        assertEquals(0.0F, segment.progress(), 1.0e-6F);
    }

    @Test
    void afterLastKeyframeClampsToEnd() {
        Timeline.Segment segment = Timeline.locate(new int[] {10, 50}, 50.0F);
        assertEquals(1, segment.from());
        assertEquals(1, segment.to());
        assertEquals(1.0F, segment.progress(), 1.0e-6F);
    }

    @Test
    void picksTheSpanContainingTheTime() {
        Timeline.Segment segment = Timeline.locate(new int[] {0, 20, 60, 100}, 40.0F);
        assertEquals(1, segment.from());
        assertEquals(2, segment.to());
        assertEquals(0.5F, segment.progress(), 1.0e-6F);
    }

    @Test
    void sharedBoundaryPicksTheEarlierSpan() {
        Timeline.Segment segment = Timeline.locate(new int[] {0, 20, 60}, 20.0F);
        assertEquals(0, segment.from());
        assertEquals(1, segment.to());
        assertEquals(1.0F, segment.progress(), 1.0e-6F);
    }

    @Test
    void everyProgressValueStaysInsideTheUnitRange() {
        int[] times = {0, 15, 15, 40, 90};
        for (int step = 0; step <= 200; step++) {
            Timeline.Segment segment = Timeline.locate(times, step * 0.5F);
            assertTrue(segment.progress() >= 0.0F && segment.progress() <= 1.0F,
                    "progress out of range at step " + step);
            assertTrue(segment.from() <= segment.to(), "from must not exceed to");
        }
    }
}
