package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSelectPayload;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class DialogueScreen extends Screen {
    private static final int MARGIN = 40;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 12;
    private static final int OPTION_HEIGHT = 14;
    private static final int OPTION_INDENT = 6;
    private static final int BACKGROUND = 0xD0000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SPEAKER_COLOR = 0xFFFFD24A;
    private static final int OPTION_COLOR = 0xFFBFBFBF;
    private static final int OPTION_HOVER_COLOR = 0xFFFFD24A;

    private final DialogueOpenPayload payload;
    private final List<Component> options;
    private int boxLeft;
    private int boxRight;
    private int hovered = -1;

    public DialogueScreen(DialogueOpenPayload payload) {
        super(Component.empty());
        this.payload = payload;
        this.options = payload.options();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = this.font;
        int boxWidth = this.width - MARGIN * 2;
        int textWidth = boxWidth - PADDING * 2;

        List<FormattedCharSequence> lines = font.split(payload.text(), textWidth);
        int speakerHeight = payload.speaker().isPresent() ? LINE_HEIGHT + 4 : 0;
        int optionsHeight = options.isEmpty() ? 0 : options.size() * OPTION_HEIGHT + OPTION_INDENT;
        int boxHeight = PADDING * 2 + speakerHeight + lines.size() * LINE_HEIGHT + optionsHeight;

        this.boxLeft = MARGIN;
        this.boxRight = MARGIN + boxWidth;
        int boxTop = this.height - MARGIN - boxHeight;

        graphics.fill(boxLeft, boxTop, boxRight, boxTop + boxHeight, BACKGROUND);

        int y = boxTop + PADDING;
        if (payload.speaker().isPresent()) {
            graphics.drawString(font, payload.speaker().get(), boxLeft + PADDING, y, SPEAKER_COLOR, true);
            y += speakerHeight;
        }
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, boxLeft + PADDING, y, TEXT_COLOR, true);
            y += LINE_HEIGHT;
        }

        int optionTop = y + OPTION_INDENT;
        this.hovered = -1;
        for (int i = 0; i < options.size(); i++) {
            int optionY = optionTop + i * OPTION_HEIGHT;
            boolean isHovered = mouseX >= boxLeft + PADDING
                    && mouseX <= boxRight - PADDING
                    && mouseY >= optionY
                    && mouseY < optionY + OPTION_HEIGHT;
            if (isHovered) {
                hovered = i;
            }
            graphics.drawString(font, options.get(i), boxLeft + PADDING + OPTION_INDENT, optionY,
                    isHovered ? OPTION_HOVER_COLOR : OPTION_COLOR, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hovered >= 0) {
            PacketDistributor.sendToServer(new DialogueSelectPayload(hovered));
            this.minecraft.setScreen(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            PacketDistributor.sendToServer(new DialogueSelectPayload(Dialogues.CLOSE_INDEX));
            ClientDialogues.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
