package com.mornfire.sqdclib.cutscene.client.video;

import com.mojang.blaze3d.platform.NativeImage;
import com.mornfire.sqdclib.SQDCLib;
import com.mornfire.sqdclib.cutscene.net.VideoFinishedPayload;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class VideoCutsceneScreen extends Screen {
    private static final int FAIL_TICKS = 60;
    private static final double TAIL_SECONDS = 0.4D;
    private static final int BACKGROUND = 0xFF000000;

    private final ResourceLocation videoId;
    private final Optional<ResourceLocation> audio;
    private final boolean lockMovement;
    private final ResourceLocation textureId;

    @Nullable
    private VideoDecoder decoder;
    @Nullable
    private DynamicTexture texture;
    @Nullable
    private NativeImage displayed;
    private double startedAt;
    private double lastTimestamp;
    private int waitTicks;
    private int frameWidth;
    private int frameHeight;
    private boolean finished;

    public VideoCutsceneScreen(ResourceLocation videoId, Optional<ResourceLocation> audio, boolean lockMovement) {
        super(Component.empty());
        this.videoId = videoId;
        this.audio = audio;
        this.lockMovement = lockMovement;
        this.textureId = ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "video_frame");
    }

    @Override
    protected void init() {
        ClientVideos.setPlaying(true);
        ClientVideos.suspendBackgroundAudio();
        Optional<VideoDecoder> opened = VideoDecoder.tryOpen(videoId);
        if (opened.isEmpty()) {
            ClientVideos.LOGGER.warn("[sqdclib] video '{}' could not be opened, skipping in 3 seconds",
                    VideoDecoder.videoFile(videoId));
            return;
        }
        decoder = opened.get();
        startedAt = System.nanoTime() / 1_000_000_000.0D;
        audio.ifPresent(this::playAudio);
    }

    private void playAudio(ResourceLocation soundId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Holder.Reference<SoundEvent> holder = minecraft.level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.SOUND_EVENT)
                .getHolder(soundId)
                .orElse(null);
        if (holder == null) {
            ClientVideos.LOGGER.warn("[sqdclib] video audio '{}' is not a registered sound event", soundId);
            return;
        }
        minecraft.getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(holder.value(), 1.0F, 1.0F));
    }

    @Override
    public void tick() {
        if (finished) {
            return;
        }
        if (decoder == null) {
            if (++waitTicks >= FAIL_TICKS) {
                finish(false);
            }
            return;
        }
        double now = System.nanoTime() / 1_000_000_000.0D;
        double elapsed = now - startedAt;
        VideoDecoder.Frame frame = decoder.poll(elapsed);
        if (frame != null) {
            lastTimestamp = frame.timestamp();
            upload(frame.image());
        }
        if (decoder.failed()) {
            ClientVideos.LOGGER.error("[sqdclib] video '{}' failed while decoding", videoId, decoder.failure());
            finish(false);
            return;
        }
        if (decoder.finished() && elapsed > lastTimestamp + TAIL_SECONDS) {
            finish(true);
        }
    }

    private void upload(NativeImage image) {
        this.frameWidth = image.getWidth();
        this.frameHeight = image.getHeight();
        if (texture == null) {
            texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(textureId, texture);
        } else {
            texture.setPixels(image);
            texture.upload();
        }
        if (displayed != null && displayed != image) {
            displayed.close();
        }
        displayed = image;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, BACKGROUND);
        if (texture != null && frameWidth > 0 && frameHeight > 0) {
            float scale = Math.min(this.width / (float) frameWidth, this.height / (float) frameHeight);
            int drawWidth = Math.max(1, (int) (frameWidth * scale));
            int drawHeight = Math.max(1, (int) (frameHeight * scale));
            int x = (this.width - drawWidth) / 2;
            int y = (this.height - drawHeight) / 2;
            graphics.blit(textureId, x, y, 0, 0, drawWidth, drawHeight, frameWidth, frameHeight);
        }
    }

    public boolean locksMovement() {
        return lockMovement;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void finish(boolean success) {
        if (finished) {
            return;
        }
        finished = true;
        PacketDistributor.sendToServer(new VideoFinishedPayload(videoId, success));
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void removed() {
        super.removed();
        ClientVideos.setPlaying(false);
        if (decoder != null) {
            decoder.close();
            decoder = null;
        }
        if (displayed != null) {
            displayed.close();
            displayed = null;
        }
        texture = null;
    }
}
