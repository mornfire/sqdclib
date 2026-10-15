package com.mornfire.sqdclib.cutscene;

import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class AnimationBridge {
    private static final String ANIMATABLE = "software.bernie.geckolib.animatable.GeoAnimatable";
    private static final String CACHE = "getAnimatableInstanceCache";

    @Nullable
    private static final Class<?> ANIMATABLE_CLASS = lookup();
    @Nullable
    private static final Method TRIGGER = method("triggerAnim", String.class, String.class);
    @Nullable
    private static final Method SPEED = method("setAnimationSpeed", float.class);

    public static boolean available() {
        return ANIMATABLE_CLASS != null && TRIGGER != null;
    }

    public static boolean trigger(Entity entity, AnimationCue cue) {
        if (!available() || !ANIMATABLE_CLASS.isInstance(entity)) {
            return false;
        }
        try {
            TRIGGER.invoke(entity, cue.controller(), cue.name());
            applySpeed(entity, cue);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    private static void applySpeed(Entity entity, AnimationCue cue) {
        if (SPEED == null || cue.speed() == 1.0F) {
            return;
        }
        try {
            Object cache = entity.getClass().getMethod(CACHE).invoke(entity);
            if (cache == null) {
                return;
            }
            Object controller = findController(cache, entity, cue.controller());
            if (controller != null) {
                SPEED.invoke(controller, cue.speed());
            }
        } catch (Throwable throwable) {
            // speed is best effort: the animation still plays at its authored rate
        }
    }

    @Nullable
    private static Object findController(Object cache, Entity entity, String name) throws Throwable {
        Object manager = cache.getClass().getMethod("getManagerForId", int.class).invoke(cache, entity.getId());
        if (manager == null) {
            return null;
        }
        Object controllers = manager.getClass().getMethod("getAnimationControllers").invoke(manager);
        if (controllers instanceof java.util.Map<?, ?> map) {
            return map.get(name);
        }
        return null;
    }

    @Nullable
    private static Class<?> lookup() {
        try {
            return Class.forName(ANIMATABLE);
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Nullable
    private static Method method(String name, Class<?>... parameters) {
        if (ANIMATABLE_CLASS == null) {
            return null;
        }
        try {
            Method found = ANIMATABLE_CLASS.getMethod(name, parameters);
            return found;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private AnimationBridge() {}
}
