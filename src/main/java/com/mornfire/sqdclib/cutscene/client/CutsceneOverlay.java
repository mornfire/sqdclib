package com.mornfire.sqdclib.cutscene.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mornfire.sqdclib.cutscene.CreditsRoll;
import com.mornfire.sqdclib.cutscene.CutsceneDefinition;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class CutsceneOverlay {
    private static final int BAR_DIVISOR = 6;   // 每片 ≈16.7% 屏高
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_SPACING = 14;
    private static final int TEXT_BOTTOM_PADDING = 24;
    private static final int CREDIT_COLOR = 0xFFFFFF;

    private static boolean loggedOnce;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ClientCutscenes.isPlaying()) {
            if (loggedOnce) {
                loggedOnce = false;
                org.slf4j.LoggerFactory.getLogger("ymondf-cinematic")
                        .info("[ymondf-cinematic] overlay stopped");
            }
            return;
        }
        if (!loggedOnce) {
            loggedOnce = true;
            org.slf4j.LoggerFactory.getLogger("ymondf-cinematic").info(
                    "[ymondf-cinematic] overlay rendering, letterbox={}",
                    ClientCutscenes.letterbox());
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int bar = height / BAR_DIVISOR;

        if (ClientCutscenes.letterbox()) {
            graphics.fill(0, 0, width, bar, 0xFF000000);
            graphics.fill(0, height - bar, width, height, 0xFF000000);
        }

        renderTexts(graphics, width, height, bar);

        float fade = ClientCutscenes.fade(partialTick);
        if (fade > 0.001F) {
            int alpha = (int) (Mth.clamp(fade, 0.0F, 1.0F) * 255.0F);
            graphics.fill(0, 0, width, height, alpha << 24);
        }

        renderCredits(graphics, partialTick, width, height);
    }

    private static void renderTexts(GuiGraphics graphics, int width, int height, int bar) {
        List<Component> texts = ClientCutscenes.activeTexts();
        if (texts.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int textY = height - bar - TEXT_BOTTOM_PADDING;
        for (Component text : texts) {
            graphics.drawCenteredString(font, text, width / 2, textY, TEXT_COLOR);
            textY -= TEXT_SPACING;
        }
    }

    private static void renderCredits(GuiGraphics graphics, float partialTick, int width, int height) {
        float progress = ClientCutscenes.creditsProgress(partialTick);
        if (progress < 0.0F) {
            return;
        }
        Optional<CutsceneDefinition.Credits> spec = ClientCutscenes.credits();
        if (spec.isEmpty() || spec.get().lines().isEmpty()) {
            return;
        }
        List<CutsceneDefinition.Credits.Line> lines = spec.get().lines();
        Font font = Minecraft.getInstance().font;

        int[] heights = new int[lines.size()];
        int[] gaps = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            heights[i] = lineHeight(font, lines.get(i));
            gaps[i] = lines.get(i).gap();
        }
        int totalHeight = CreditsRoll.contentHeight(heights, gaps);

        int y = CreditsRoll.offset(progress, height, totalHeight);
        PoseStack pose = graphics.pose();
        for (int i = 0; i < lines.size(); i++) {
            CutsceneDefinition.Credits.Line line = lines.get(i);
            int lineHeight = heights[i];
            if (y + lineHeight >= 0 && y <= height) {
                pose.pushPose();
                pose.translate(width / 2.0F, y, 0.0F);
                pose.scale(line.scale(), line.scale(), 1.0F);
                graphics.drawCenteredString(font, line.text(), 0, 0, line.color().orElse(CREDIT_COLOR));
                pose.popPose();
            }
            y += lineHeight + gaps[i];
        }
    }

    private static int lineHeight(Font font, CutsceneDefinition.Credits.Line line) {
        return (int) (font.lineHeight * line.scale());
    }

    private CutsceneOverlay() {}
}
