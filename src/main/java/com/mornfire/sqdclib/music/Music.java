package com.mornfire.sqdclib.music;

import com.mornfire.sqdclib.music.net.MusicStartPayload;
import com.mornfire.sqdclib.music.net.MusicStopPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class Music {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, ResourceLocation> PLAYING = new HashMap<>();

    public static void play(ServerPlayer player, ResourceLocation sound) {
        play(player, sound, 1.0F);
    }

    public static boolean play(ServerPlayer player, ResourceLocation sound, float volume) {
        Holder.Reference<SoundEvent> holder = player.level().registryAccess()
                .registryOrThrow(Registries.SOUND_EVENT)
                .getHolder(sound)
                .orElse(null);
        if (holder == null) {
            LOGGER.warn("[sqdclib] music '{}' is not a registered sound event. "
                    + "The id must match an entry in a sounds.json, not an audio file path.", sound);
            return false;
        }
        PLAYING.put(player.getUUID(), sound);
        PacketDistributor.sendToPlayer(player, new MusicStartPayload(holder, volume));
        return true;
    }

    public static void stop(ServerPlayer player) {
        if (PLAYING.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayer(player, new MusicStopPayload());
        }
    }

    public static void forget(ServerPlayer player) {
        PLAYING.remove(player.getUUID());
    }

    public static boolean isPlaying(ServerPlayer player) {
        return PLAYING.containsKey(player.getUUID());
    }

    @Nullable
    public static ResourceLocation current(ServerPlayer player) {
        return PLAYING.get(player.getUUID());
    }

    private Music() {}
}
