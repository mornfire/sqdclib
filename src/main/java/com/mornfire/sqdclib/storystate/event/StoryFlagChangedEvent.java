package com.mornfire.sqdclib.storystate.event;

import com.mornfire.sqdclib.storystate.FlagScope;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

public class StoryFlagChangedEvent extends Event {
    private final FlagScope scope;
    private final String flag;
    private final boolean oldValue;
    private final boolean newValue;
    @Nullable
    private final Player player;
    @Nullable
    private final Level level;

    private StoryFlagChangedEvent(FlagScope scope, String flag, boolean oldValue, boolean newValue,
            @Nullable Player player, @Nullable Level level) {
        this.scope = scope;
        this.flag = flag;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.player = player;
        this.level = level;
    }

    public static StoryFlagChangedEvent player(Player player, String flag, boolean oldValue, boolean newValue) {
        return new StoryFlagChangedEvent(FlagScope.PLAYER, flag, oldValue, newValue, player, player.level());
    }

    public static StoryFlagChangedEvent world(Level level, String flag, boolean oldValue, boolean newValue) {
        return new StoryFlagChangedEvent(FlagScope.WORLD, flag, oldValue, newValue, null, level);
    }

    public FlagScope getScope() {
        return scope;
    }

    public String getFlag() {
        return flag;
    }

    public boolean getOldValue() {
        return oldValue;
    }

    public boolean getNewValue() {
        return newValue;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @Nullable
    public Level getLevel() {
        return level;
    }
}
