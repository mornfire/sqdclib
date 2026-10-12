package com.mornfire.sqdclib.storystate;

import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.storystate.event.StoryFlagChangedEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public final class StoryProgress implements INBTSerializable<CompoundTag> {
    private static final String FLAGS_KEY = "flags";
    private static final String COUNTERS_KEY = "counters";
    private static final String QUESTS_KEY = "quests";
    private static final String ACTIVE_KEY = "active_quests";

    public static final AttachmentSyncHandler<StoryProgress> SYNC_HANDLER = new AttachmentSyncHandler<>() {
        @Override
        public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, StoryProgress attachment, boolean initialSync) {
            ByteBufCodecs.COMPOUND_TAG.encode(buf, attachment.toTag());
        }

        @Override
        public StoryProgress read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                @Nullable StoryProgress previousValue) {
            CompoundTag tag = ByteBufCodecs.COMPOUND_TAG.decode(buf);
            Player owner = holder instanceof Player player ? player : null;
            return tag == null ? new StoryProgress(owner) : fromTag(owner, tag);
        }
    };

    @Nullable
    private final Player owner;
    private final Set<String> flags = new HashSet<>();
    private final Map<String, Integer> counters = new HashMap<>();
    private final Set<ResourceLocation> completedQuests = new HashSet<>();
    private final Set<ResourceLocation> activeQuests = new HashSet<>();

    public StoryProgress(@Nullable Player owner) {
        this.owner = owner;
    }

    public static StoryProgress of(Player player) {
        return player.getData(SQDCLib.STORY_PROGRESS);
    }

    public static StoryProgress fromTag(@Nullable Player owner, CompoundTag tag) {
        StoryProgress progress = new StoryProgress(owner);
        progress.readFrom(tag);
        return progress;
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
        boolean changed = value ? flags.add(flag) : flags.remove(flag);
        if (changed) {
            afterChange();
            if (owner != null) {
                NeoForge.EVENT_BUS.post(StoryFlagChangedEvent.player(owner, flag, !value, value));
            }
        }
        return changed;
    }

    public int getCounter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public int setCounter(String key, int value) {
        int previous = getCounter(key);
        if (previous != value) {
            counters.put(key, value);
            afterChange();
        }
        return previous;
    }

    public int addCounter(String key, int delta) {
        int updated = getCounter(key) + delta;
        if (delta != 0) {
            counters.put(key, updated);
            afterChange();
        }
        return updated;
    }

    public boolean isQuestCompleted(ResourceLocation quest) {
        return completedQuests.contains(quest);
    }

    public boolean completeQuest(ResourceLocation quest) {
        boolean changed = completedQuests.add(quest);
        if (changed) {
            afterChange();
        }
        return changed;
    }

    public boolean revokeQuest(ResourceLocation quest) {
        boolean changed = completedQuests.remove(quest);
        if (changed) {
            afterChange();
        }
        return changed;
    }

    public Set<String> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public boolean isQuestActive(ResourceLocation quest) {
        return activeQuests.contains(quest);
    }

    public boolean activateQuest(ResourceLocation quest) {
        boolean changed = activeQuests.add(quest);
        if (changed) {
            afterChange();
        }
        return changed;
    }

    public boolean deactivateQuest(ResourceLocation quest) {
        boolean changed = activeQuests.remove(quest);
        if (changed) {
            afterChange();
        }
        return changed;
    }

    public Set<ResourceLocation> getActiveQuests() {
        return Collections.unmodifiableSet(activeQuests);
    }

    public Set<ResourceLocation> getCompletedQuests() {
        return Collections.unmodifiableSet(completedQuests);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return toTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        readFrom(tag);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        ListTag flagList = new ListTag();
        for (String flag : flags) {
            flagList.add(StringTag.valueOf(flag));
        }
        tag.put(FLAGS_KEY, flagList);

        CompoundTag counterTag = new CompoundTag();
        counters.forEach(counterTag::putInt);
        tag.put(COUNTERS_KEY, counterTag);

        ListTag questList = new ListTag();
        for (ResourceLocation quest : completedQuests) {
            questList.add(StringTag.valueOf(quest.toString()));
        }
        tag.put(QUESTS_KEY, questList);

        ListTag activeList = new ListTag();
        for (ResourceLocation quest : activeQuests) {
            activeList.add(StringTag.valueOf(quest.toString()));
        }
        tag.put(ACTIVE_KEY, activeList);

        return tag;
    }

    private void readFrom(CompoundTag tag) {
        flags.clear();
        counters.clear();
        completedQuests.clear();
        activeQuests.clear();

        ListTag flagList = tag.getList(FLAGS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < flagList.size(); i++) {
            flags.add(flagList.getString(i));
        }

        CompoundTag counterTag = tag.getCompound(COUNTERS_KEY);
        for (String key : counterTag.getAllKeys()) {
            counters.put(key, counterTag.getInt(key));
        }

        ListTag questList = tag.getList(QUESTS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < questList.size(); i++) {
            completedQuests.add(ResourceLocation.parse(questList.getString(i)));
        }

        ListTag activeList = tag.getList(ACTIVE_KEY, Tag.TAG_STRING);
        for (int i = 0; i < activeList.size(); i++) {
            activeQuests.add(ResourceLocation.parse(activeList.getString(i)));
        }
    }

    private void afterChange() {
        if (owner instanceof ServerPlayer serverPlayer) {
            serverPlayer.syncData(SQDCLib.STORY_PROGRESS);
        }
    }
}
