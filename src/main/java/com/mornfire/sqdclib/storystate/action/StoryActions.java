package com.mornfire.sqdclib.storystate.action;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class StoryActions {
    private static final Map<ResourceLocation, MapCodec<? extends StoryAction>> CUSTOM = new LinkedHashMap<>();

    public static <T extends StoryAction> void register(ResourceLocation type, MapCodec<T> codec) {
        CUSTOM.put(type, codec);
    }

    public static Set<ResourceLocation> registered() {
        return Collections.unmodifiableSet(CUSTOM.keySet());
    }

    @Nullable
    static MapCodec<? extends StoryAction> lookup(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null) {
            return null;
        }
        return CUSTOM.get(id);
    }

    private StoryActions() {}
}
