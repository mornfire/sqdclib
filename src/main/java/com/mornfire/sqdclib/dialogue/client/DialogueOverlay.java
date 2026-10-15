package com.mornfire.sqdclib.dialogue.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DialogueOverlay {
    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        DialoguePanel panel = ClientDialogues.panel();
        if (panel == null) {
            return;
        }
        panel.render(graphics, Minecraft.getInstance().font, graphics.guiWidth(), graphics.guiHeight(), -1, -1);
    }

    private DialogueOverlay() {}
}
