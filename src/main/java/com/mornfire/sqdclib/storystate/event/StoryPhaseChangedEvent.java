package com.mornfire.sqdclib.storystate.event;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class StoryPhaseChangedEvent extends Event {
    private final Level level;
    private final String oldPhase;
    private final String newPhase;

    public StoryPhaseChangedEvent(Level level, String oldPhase, String newPhase) {
        this.level = level;
        this.oldPhase = oldPhase;
        this.newPhase = newPhase;
    }

    public Level getLevel() {
        return level;
    }

    public String getOldPhase() {
        return oldPhase;
    }

    public String getNewPhase() {
        return newPhase;
    }
}
