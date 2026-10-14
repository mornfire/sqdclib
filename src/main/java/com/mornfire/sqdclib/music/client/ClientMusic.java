package com.mornfire.sqdclib.music.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public final class ClientMusic {
    @Nullable
    private static SoundInstance current;

    public static boolean isPlaying() {
        return current != null;
    }

    public static void play(Holder<SoundEvent> sound, float volume) {
        stop();
        current = new LoopingSoundInstance(sound.value(), volume);
        Minecraft.getInstance().getSoundManager().play(current);
    }

    public static void stop() {
        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
            current = null;
        }
    }

    private ClientMusic() {}
}
