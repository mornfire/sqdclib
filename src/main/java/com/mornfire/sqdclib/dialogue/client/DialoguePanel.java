package com.mornfire.sqdclib.dialogue.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mornfire.sqdclib.dialogue.DialogueMode;
import com.mornfire.sqdclib.dialogue.DialoguePlacement;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public final class DialoguePanel {
    private static final int MARGIN = 40;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 12;
    private static final int OPTION_HEIGHT = 14;
    private static final int OPTION_INDENT = 6;
    private static final int PORTRAIT_SIZE = 48;
    private static final int PORTRAIT_GAP = 10;
    private static final int BOX_MAX_WIDTH = 460;
    private static final int CINEMATIC_DIVISOR = 3;
    private static final int AMBIENT_BACKGROUND = 0xD0000000;
    private static final int DIMMER = 0x80000000;
    private static final int CINEMATIC_BACKGROUND = 0xF0000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int OPTION_COLOR = 0xFFBFBFBF;
    private static final int OPTION_HOVER_COLOR = 0xFFFFD24A;
    private static final int HINT_COLOR = 0xFF8A8A8A;
    private static final Map<ResourceLocation, int[]> SIZE_CACHE = new HashMap<>();

    public record ClickableSpan(int left, int top, int right, int bottom, Style style) {
        public boolean contains(double x, double y) {
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
    private int optionLeft;
    private int textLeft;

    public int inputLeft() {
        return boxLeft + PADDING;
    }

    public int inputTop() {
        return optionTop - OPTION_INDENT + LINE_HEIGHT;
    }

    public int inputWidth() {
        return Math.max(60, boxRight - boxLeft - PADDING * 2);
    }

    public DialoguePanel(DialogueOpenPayload payload) {
        this.payload = payload;
        this.options = payload.options();
        this.typing = payload.textSpeed() > 0;
    }

    public DialogueOpenPayload payload() {
        return payload;
    }

    public List<Component> options() {
        return options;
    }

    public boolean cinematic() {
        return payload.mode() == DialogueMode.CINEMATIC;
    }

    public boolean isTyping() {
        return typing;
    }

    public void revealAll() {
        typed = payload.text().getString().length();
        typing = false;
    }

    public void tick() {
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

    @Nullable
    public ResourceLocation portraitTexture() {
        return payload.portrait().orElse(null);
    }

    private int[] portraitSize(ResourceLocation texture) {
        int[] cached = SIZE_CACHE.get(texture);
        if (cached != null) {
            return cached;
        }
        int[] size = {PORTRAIT_SIZE, PORTRAIT_SIZE};
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture);
            try (NativeImage image = NativeImage.read(resource.open())) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width > 0 && height > 0) {
                    float scale = PORTRAIT_SIZE / (float) Math.max(width, height);
                    size = new int[] {Math.max(1, (int) (width * scale)), Math.max(1, (int) (height * scale))};
                }
            }
        } catch (Throwable throwable) {
            size = new int[] {PORTRAIT_SIZE, PORTRAIT_SIZE};
        }
        SIZE_CACHE.put(texture, size);
        return size;
    }

    @Nullable
    public ClickableSpan spanAt(double x, double y) {
        for (ClickableSpan span : spans) {
            if (span.contains(x, y)) {
                return span;
            }
        }
        return null;
    }

    public int optionAt(double x, double y) {
        if (options.isEmpty() || y < optionTop) {
            return -1;
        }
        int index = (int) ((y - optionTop) / OPTION_HEIGHT);
        if (index < 0 || index >= options.size()) {
            return -1;
        }
        return x >= optionLeft - OPTION_INDENT && x <= boxRight - PADDING ? index : -1;
    }

    public void render(GuiGraphics graphics, Font font, int width, int height, int mouseX, int mouseY) {
        boolean cinematic = cinematic();
        int boxWidth = cinematic ? width : Math.min(BOX_MAX_WIDTH, width - MARGIN * 2);
        int textWidth = boxWidth - PADDING * 2;
        ResourceLocation portrait = portraitTexture();
        int portraitWidth = 0;
        int portraitHeight = 0;
        if (portrait != null) {
            int[] size = portraitSize(portrait);
            portraitWidth = size[0];
            portraitHeight = size[1];
        }
        int portraitColumn = portrait == null ? 0 : portraitWidth + PORTRAIT_GAP;
        int contentWidth = textWidth - portraitColumn;

        List<FormattedCharSequence> fullLines = font.split(payload.text(), Math.max(20, contentWidth));
        int nameHeight = payload.speakerName().isPresent() ? LINE_HEIGHT + 4 : 0;
        int optionsHeight = options.isEmpty() ? 0 : options.size() * OPTION_HEIGHT + OPTION_INDENT;
        int portraitOverflow = portrait == null ? 0 : Math.max(0, portraitHeight - LINE_HEIGHT * 2);
        int contentHeight = PADDING * 2 + portraitOverflow + nameHeight
                + fullLines.size() * LINE_HEIGHT + optionsHeight;

        this.boxLeft = cinematic ? 0 : (width - boxWidth) / 2;
        this.boxRight = cinematic ? width : boxLeft + boxWidth;
        int boxTop;
        int boxBottom;
        if (cinematic) {
            int bar = Math.max(height / CINEMATIC_DIVISOR, contentHeight);
            boxTop = height - bar;
            boxBottom = height;
            graphics.fill(0, 0, width, height, DIMMER);
            graphics.fill(0, boxTop, width, boxBottom, CINEMATIC_BACKGROUND);
        } else {
            boxTop = switch (payload.placement()) {
                case BOTTOM -> height - MARGIN - contentHeight;
                case CENTER -> (height - contentHeight) / 2;
                case TOP -> MARGIN;
            };
            boxBottom = boxTop + contentHeight;
            graphics.fill(boxLeft, boxTop, boxRight, boxBottom, AMBIENT_BACKGROUND);
        }

        int x = boxLeft + PADDING;
        int y = boxTop + PADDING;
        if (portrait != null) {
            graphics.blit(portrait, x, y, 0, 0, portraitWidth, portraitHeight, portraitWidth, portraitHeight);
            x += portraitColumn;
        }
        this.textLeft = x;

        if (payload.speakerName().isPresent()) {
            graphics.drawString(font, payload.speakerName().get(), x, y, payload.speakerColor(), true);
            y += nameHeight;
        }

        Component shown = typing ? truncate(payload.text(), typed) : payload.text();
        List<FormattedCharSequence> shownLines = font.split(shown, Math.max(20, contentWidth));
        int textTop = y;
        for (FormattedCharSequence line : shownLines) {
            graphics.drawString(font, line, x, y, TEXT_COLOR, true);
            y += LINE_HEIGHT;
        }

        this.spans.clear();
        this.hoveredSpan = -1;
        if (!typing) {
            collectSpans(font, textTop, x, Math.max(20, contentWidth), mouseX, mouseY);
        }

        this.optionLeft = textLeft;
        int optionTopY = boxTop + PADDING + nameHeight + fullLines.size() * LINE_HEIGHT + portraitOverflow;
        this.optionTop = optionTopY + OPTION_INDENT;
        this.hovered = -1;
        for (int i = 0; i < options.size(); i++) {
            int optionY = optionTop + i * OPTION_HEIGHT;
            boolean isHovered = mouseX >= 0
                    && mouseX >= optionLeft - OPTION_INDENT
                    && mouseX <= boxRight - PADDING
                    && mouseY >= optionY
                    && mouseY < optionY + OPTION_HEIGHT;
            if (isHovered) {
                hovered = i;
            }
            graphics.drawString(font, options.get(i), optionLeft + OPTION_INDENT, optionY,
                    isHovered ? OPTION_HOVER_COLOR : OPTION_COLOR, true);
        }

        if (!cinematic && options.isEmpty() && !typing) {
            graphics.drawString(font, Component.translatable("sqdclib.dialogue.esc"), boxRight - PADDING - 40,
                    boxTop + PADDING, HINT_COLOR, true);
        }
    }

    public int hoveredOption() {
        return hovered;
    }

    @Nullable
    public Style hoveredStyle() {
        return hoveredSpan >= 0 ? spans.get(hoveredSpan).style() : null;
    }

    private void collectSpans(Font font, int top, int left, int width, int mouseX, int mouseY) {
        StringSplitter splitter = new StringSplitter(
                (codePoint, style) -> font.width(String.valueOf((char) codePoint)));
        List<FormattedText> wrapped = splitter.splitLines(payload.text(), width, Style.EMPTY);
        for (int lineIndex = 0; lineIndex < wrapped.size(); lineIndex++) {
            int y = top + lineIndex * LINE_HEIGHT;
            int[] cursor = {left};
            wrapped.get(lineIndex).visit((style, text) -> {
                int spanWidth = font.width(text);
                if (style.getClickEvent() != null || style.getHoverEvent() != null) {
                    ClickableSpan span = new ClickableSpan(cursor[0], y, cursor[0] + spanWidth,
                            y + LINE_HEIGHT, style);
                    if (mouseX >= 0 && span.contains(mouseX, mouseY)) {
                        hoveredSpan = spans.size();
                    }
                    spans.add(span);
                }
                cursor[0] += spanWidth;
                return Optional.empty();
            }, Style.EMPTY);
        }
    }

    public static Component truncate(Component source, int limit) {
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

    @Nullable
    public static Component hoverTooltip(Style style) {
        HoverEvent event = style.getHoverEvent();
        return event == null ? null : event.getValue(HoverEvent.Action.SHOW_TEXT);
    }
}
