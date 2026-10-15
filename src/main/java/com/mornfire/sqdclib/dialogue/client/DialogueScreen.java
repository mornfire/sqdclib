package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.dialogue.InputSpec;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSelectPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSubmitPayload;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class DialogueScreen extends Screen {
    private static final int INPUT_HEIGHT = 18;

    private final DialoguePanel panel;
    @Nullable
    private EditBox inputBox;

    public DialogueScreen(DialogueOpenPayload payload) {
        super(Component.empty());
        this.panel = new DialoguePanel(payload);
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
    protected void init() {
        Optional<InputSpec> spec = panel.payload().input();
        if (spec.isEmpty() || !panel.options().isEmpty()) {
            return;
        }
        EditBox box = new EditBox(this.font, 0, 0, 100, INPUT_HEIGHT, Component.empty());
        box.setMaxLength(Math.max(1, spec.get().maxLength()));
        spec.get().placeholder().ifPresent(box::setHint);
        this.inputBox = this.addRenderableWidget(box);
        this.setInitialFocus(box);
    }

    private void submit(String value) {
        PacketDistributor.sendToServer(new DialogueSubmitPayload(value));
        this.minecraft.setScreen(null);
    }

    @Override
    public void tick() {
        super.tick();
        panel.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        panel.render(graphics, this.font, this.width, this.height, mouseX, mouseY);
        if (inputBox != null) {
            inputBox.setX(panel.inputLeft());
            inputBox.setY(panel.inputTop());
            inputBox.setWidth(panel.inputWidth());
        }
        super.render(graphics, mouseX, mouseY, partialTick);

        Style hovered = panel.hoveredStyle();
        if (hovered != null) {
            Component tooltip = DialoguePanel.hoverTooltip(hovered);
            if (tooltip != null) {
                graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (panel.isTyping()) {
            panel.revealAll();
            return true;
        }
        DialoguePanel.ClickableSpan span = panel.spanAt(mouseX, mouseY);
        if (span != null) {
            this.handleComponentClicked(span.style());
            return true;
        }
        if (inputBox != null && inputBox.isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int index = panel.optionAt(mouseX, mouseY);
        if (index >= 0) {
            PacketDistributor.sendToServer(new DialogueSelectPayload(index));
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
        if (inputBox != null && inputBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submit(inputBox.getValue());
                return true;
            }
        } else if (panel.isTyping() && keyCode == GLFW.GLFW_KEY_SPACE) {
            panel.revealAll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
