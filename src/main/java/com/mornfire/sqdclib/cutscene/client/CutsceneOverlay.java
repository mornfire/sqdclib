package com.mornfire.sqdclib.cutscene.client;

import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class CutsceneOverlay {
    private static final int BAR_DIVISOR = 8;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_SPACING = 14;
    private static final int TEXT_BOTTOM_PADDING = 24;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ClientCutscenes.isPlaying()) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int bar = height / BAR_DIVISOR;

        if (ClientCutscenes.letterbox()) {
            graphics.fill(0, 0, width, bar, 0xFF000000);
            graphics.fill(0, height - bar, width, height, 0xFF000000);
        }

        List<Component> texts = ClientCutscenes.activeTexts();
        if (!texts.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            int textY = height - bar - TEXT_BOTTOM_PADDING;
            for (Component text : texts) {
                graphics.drawCenteredString(font, text, width / 2, textY, TEXT_COLOR);
                textY -= TEXT_SPACING;
            }
        }

        float fade = ClientCutscenes.fade(partialTick);
        if (fade > 0.001F) {
            int alpha = (int) (Mth.clamp(fade, 0.0F, 1.0F) * 255.0F);
            graphics.fill(0, 0, width, height, alpha << 24);
        }
    }

    private CutsceneOverlay() {}
}
