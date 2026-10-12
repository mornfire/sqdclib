package com.mornfire.sqdclib.storystate;

import com.mornfire.sqdclib.storystate.event.StoryFlagChangedEvent;
import com.mornfire.sqdclib.storystate.event.StoryPhaseChangedEvent;
import com.mornfire.sqdclib.storystate.net.WorldStatePayload;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class WorldState extends SavedData {
    public static final String DATA_ID = "sqdclib_story_state";
    public static final String EMPTY_PHASE = "";

    private static final String FLAGS_KEY = "flags";
    private static final String PHASE_KEY = "phase";
    private static final WorldState CLIENT_MIRROR = new WorldState(null);

    @Nullable
    private final MinecraftServer server;
    private final Set<String> flags = new HashSet<>();
    private String phase = EMPTY_PHASE;

    private WorldState(@Nullable MinecraftServer server) {
        this.server = server;
    }

    public static WorldState get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return get(serverLevel.getServer());
        }
        return CLIENT_MIRROR;
    }

    public static WorldState get(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(() -> new WorldState(server), (tag, provider) -> fromTag(server, tag)),
                DATA_ID);
    }

    public static void applyClientSnapshot(CompoundTag tag) {
        CLIENT_MIRROR.readFrom(tag);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean getFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean setFlag(String flag) {
        return setFlag(flag, true);
    }

    public boolean setFlag(String flag, boolean value) {
        requireServer();
        boolean changed = value ? flags.add(flag) : flags.remove(flag);
        if (changed) {
            afterChange();
            NeoForge.EVENT_BUS.post(StoryFlagChangedEvent.world(server.overworld(), flag, !value, value));
        }
        return changed;
    }

    public Set<String> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public String getPhase() {
        return phase;
    }

    public boolean setPhase(String newPhase) {
        requireServer();
        String target = newPhase == null ? EMPTY_PHASE : newPhase;
        if (Objects.equals(phase, target)) {
            return false;
        }
        String oldPhase = phase;
        phase = target;
        afterChange();
        NeoForge.EVENT_BUS.post(new StoryPhaseChangedEvent(server.overworld(), oldPhase, target));
        return true;
    }

    public boolean isServerSide() {
        return server != null;
    }

    public CompoundTag snapshot() {
        CompoundTag tag = new CompoundTag();

        ListTag flagList = new ListTag();
        for (String flag : flags) {
            flagList.add(StringTag.valueOf(flag));
        }
        tag.put(FLAGS_KEY, flagList);

        tag.putString(PHASE_KEY, phase);
        return tag;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        return snapshot();
    }

    private static WorldState fromTag(MinecraftServer server, CompoundTag tag) {
        WorldState state = new WorldState(server);
        state.readFrom(tag);
        return state;
    }

    private void readFrom(CompoundTag tag) {
        flags.clear();
        ListTag flagList = tag.getList(FLAGS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < flagList.size(); i++) {
            flags.add(flagList.getString(i));
        }
        phase = tag.getString(PHASE_KEY);
    }

    private void afterChange() {
        setDirty();
        PacketDistributor.sendToAllPlayers(new WorldStatePayload(snapshot()));
    }

    private void requireServer() {
        if (server == null) {
            throw new UnsupportedOperationException("WorldState is read-only on the client");
        }
    }
}
