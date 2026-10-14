package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.dialogue.InputSpec;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSelectPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSubmitPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class DialogueScreen extends Screen {
    private static final int MARGIN = 40;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 12;
    private static final int OPTION_HEIGHT = 14;
    private static final int OPTION_INDENT = 6;
    private static final int INPUT_HEIGHT = 18;
    private static final int BOX_MAX_WIDTH = 420;
    private static final int BACKGROUND = 0xD0000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int OPTION_COLOR = 0xFFBFBFBF;
    private static final int OPTION_HOVER_COLOR = 0xFFFFD24A;
    private static final int LINK_HOVER_COLOR = 0xFF88CCFF;

    private record ClickableSpan(int left, int top, int right, int bottom, Style style) {
        private boolean contains(double x, double y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }

    private final DialogueOpenPayload payload;
    private final List<Component> options;
    private final List<ClickableSpan> spans = new ArrayList<>();

    private int typed;
    private int elapsed;
    private boolean typing;
    private int hovered = -1;
    private int hoveredSpan = -1;
    private int boxLeft;
    private int boxRight;
    private int optionTop;
    @Nullable
    private EditBox inputBox;

    public DialogueScreen(DialogueOpenPayload payload) {
        super(Component.empty());
        this.payload = payload;
        this.options = payload.options();
        this.typing = payload.textSpeed() > 0;
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
        Optional<InputSpec> spec = payload.input();
        if (spec.isEmpty() || !options.isEmpty()) {
            return;
        }
        int boxWidth = Math.min(BOX_MAX_WIDTH, this.width - MARGIN * 2);
        int x = (this.width - boxWidth) / 2 + PADDING;
        int y = this.height - MARGIN - PADDING - INPUT_HEIGHT;
        EditBox box = new EditBox(this.font, x, y, boxWidth - PADDING * 2, INPUT_HEIGHT, Component.empty());
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
        if (!typing) {
            return;
        }
        elapsed++;
        int total = payload.text().getString().length();
        typed = Math.min(total, elapsed * Math.max(1, payload.textSpeed()));
        if (typed >= total) {
            typing = false;
        }
    }

    private void revealAll() {
        typed = payload.text().getString().length();
        typing = false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = this.font;
        int boxWidth = Math.min(BOX_MAX_WIDTH, this.width - MARGIN * 2);
        int textWidth = boxWidth - PADDING * 2;

        List<FormattedCharSequence> fullLines = font.split(payload.text(), textWidth);
        int speakerHeight = payload.speaker().isPresent() ? LINE_HEIGHT + 4 : 0;
        int inputHeight = inputBox != null ? INPUT_HEIGHT + 6 : 0;
        int optionsHeight = options.isEmpty() ? 0 : options.size() * OPTION_HEIGHT + OPTION_INDENT;
        int boxHeight = PADDING * 2 + speakerHeight + fullLines.size() * LINE_HEIGHT
                + optionsHeight + inputHeight;

        this.boxLeft = (this.width - boxWidth) / 2;
        this.boxRight = boxLeft + boxWidth;
        int boxTop = switch (payload.placement()) {
            case BOTTOM -> this.height - MARGIN - boxHeight;
            case CENTER -> (this.height - boxHeight) / 2;
            case TOP -> MARGIN;
        };

        graphics.fill(boxLeft, boxTop, boxRight, boxTop + boxHeight, BACKGROUND);

        int y = boxTop + PADDING;
        if (payload.speaker().isPresent()) {
            graphics.drawString(font, payload.speaker().get(), boxLeft + PADDING, y, payload.speakerColor(), true);
            y += speakerHeight;
        }

        Component shown = typing ? truncate(payload.text(), typed) : payload.text();
        List<FormattedCharSequence> shownLines = font.split(shown, textWidth);
        for (FormattedCharSequence line : shownLines) {
            graphics.drawString(font, line, boxLeft + PADDING, y, TEXT_COLOR, true);
            y += LINE_HEIGHT;
        }

        this.spans.clear();
        this.hoveredSpan = -1;
        if (!typing) {
            collectSpans(fullLines, font, boxLeft + PADDING, boxTop + PADDING + speakerHeight, mouseX, mouseY);
        }

        int optionTopY = boxTop + PADDING + speakerHeight + fullLines.size() * LINE_HEIGHT;
        if (!options.isEmpty()) {
            this.optionTop = optionTopY + OPTION_INDENT;
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
        } else {
            this.hovered = -1;
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (hoveredSpan >= 0) {
            Component tooltip = hoverTooltip(spans.get(hoveredSpan).style());
            if (tooltip != null) {
                graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Nullable
    private static Component hoverTooltip(Style style) {
        HoverEvent event = style.getHoverEvent();
        return event == null ? null : event.getValue(HoverEvent.Action.SHOW_TEXT);
    }

    private void collectSpans(List<FormattedCharSequence> lines, Font font, int left, int top,
            int mouseX, int mouseY) {
        StringSplitter splitter = new StringSplitter(
                (codePoint, style) -> font.width(String.valueOf((char) codePoint)));
        List<FormattedText> wrapped = splitter.splitLines(payload.text(),
                boxRight - boxLeft - PADDING * 2, Style.EMPTY);
        for (int lineIndex = 0; lineIndex < wrapped.size(); lineIndex++) {
            int y = top + lineIndex * LINE_HEIGHT;
            int[] cursor = {left};
            wrapped.get(lineIndex).visit((style, text) -> {
                int width = font.width(text);
                if (style.getClickEvent() != null || style.getHoverEvent() != null) {
                    ClickableSpan span = new ClickableSpan(cursor[0], y, cursor[0] + width, y + LINE_HEIGHT, style);
                    if (span.contains(mouseX, mouseY)) {
                        hoveredSpan = spans.size();
                    }
                    spans.add(span);
                }
                cursor[0] += width;
                return Optional.empty();
            }, Style.EMPTY);
        }
    }

    private static Component truncate(Component source, int limit) {
        MutableComponent result = Component.empty();
        int[] remaining = {limit};
        source.visit((style, text) -> {
            if (remaining[0] <= 0) {
                return Optional.of(Boolean.TRUE);
            }
            String piece = text.length() <= remaining[0] ? text : text.substring(0, remaining[0]);
            remaining[0] -= piece.length();
            result.append(Component.literal(piece).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (typing) {
            revealAll();
            return true;
        }
        for (ClickableSpan span : spans) {
            if (span.contains(mouseX, mouseY)) {
                this.handleComponentClicked(span.style());
                return true;
            }
        }
        if (inputBox != null && inputBox.isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (hovered >= 0) {
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
        if (inputBox != null && inputBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submit(inputBox.getValue());
                return true;
            }
        } else if (typing && keyCode == GLFW.GLFW_KEY_SPACE) {
            revealAll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
