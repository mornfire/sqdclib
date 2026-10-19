package com.mornfire.sqdclib.cutscene.client.video;

import com.mojang.logging.LogUtils;
import com.mornfire.sqdclib.SQDCLib;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = SQDCLib.MOD_ID, value = Dist.CLIENT)
public final class ClientVideos {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean playing;

    public static void play(ResourceLocation videoId, Optional<ResourceLocation> audio, boolean lockMovement) {
        Minecraft.getInstance().setScreen(new VideoCutsceneScreen(videoId, audio, lockMovement));
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static void setPlaying(boolean value) {
        playing = value;
    }

    public static void suspendBackgroundAudio() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getMusicManager().stopPlaying();
        minecraft.getSoundManager().stop(null, SoundSource.WEATHER);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (playing) {
            event.setCanceled(true);
        }
    }

    private ClientVideos() {}
}
