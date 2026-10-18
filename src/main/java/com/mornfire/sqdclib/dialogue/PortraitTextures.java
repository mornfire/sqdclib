package com.mornfire.sqdclib.dialogue;

import net.minecraft.resources.ResourceLocation;

public final class PortraitTextures {
    public static final String PREFIX = "textures/gui/portrait/";
    public static final String SUFFIX = ".png";

    public static ResourceLocation resolve(ResourceLocation id) {
        String path = id.getPath();
        if (path.startsWith("textures/") || path.endsWith(SUFFIX)) {
            return id;
        }
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), PREFIX + path + SUFFIX);
    }

    private PortraitTextures() {}
}
