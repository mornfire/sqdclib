package com.mornfire.sqdclib.dialogue.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mornfire.sqdclib.client.GlassPanel;
import com.mornfire.sqdclib.client.SqdTheme;
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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * 对话面板，两种形态：
 *
 * <ul>
 *   <li>旁白（没有说话人、也没有肖像）：一块居中的液态玻璃，文字居中。</li>
 *   <li>有人说话：左侧是人物肖像，肖像上方是身份、下方是名字；文字在右侧。
 *       身份或名字缺失时显示 ???。</li>
 * </ul>
 */
public final class DialoguePanel {
    private static final int MARGIN = 28;
    private static final int PADDING = 16;
    private static final int LINE_HEIGHT = 13;
    private static final int OPTION_HEIGHT = 15;
    private static final int OPTION_INDENT = 6;
    private static final int OPTION_GAP = 8;
    private static final int PORTRAIT_BOX = 64;
    private static final int PORTRAIT_GAP = 14;
    private static final int BOX_MAX_WIDTH = 520;
    private static final int BOX_MIN_WIDTH = 300;
    private static final int TEXT_COLOR = SqdTheme.TEXT;
    private static final int OPTION_COLOR = 0xFFC6D2E6;
    private static final int OPTION_HOVER_COLOR = 0xFFFFD98A;
    private static final int HINT_COLOR = 0xFF8A94A8;
    private static final String UNKNOWN_TEXT = "???";
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

    /** 说话人形态：有名字或肖像。两者都没有就是旁白。 */
    public boolean speakerMode() {
        return payload.speakerName().isPresent() || payload.portrait().isPresent();
    }

    private int[] portraitSize(ResourceLocation texture) {
        int[] cached = SIZE_CACHE.get(texture);
        if (cached != null) {
            return cached;
        }
        int[] size = {PORTRAIT_BOX, PORTRAIT_BOX};
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture);
            try (NativeImage image = NativeImage.read(resource.open())) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width > 0 && height > 0) {
                    float scale = PORTRAIT_BOX / (float) Math.max(width, height);
                    size = new int[] {Math.max(1, (int) (width * scale)), Math.max(1, (int) (height * scale))};
                }
            }
        } catch (Throwable throwable) {
            size = new int[] {PORTRAIT_BOX, PORTRAIT_BOX};
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
        boolean speaker = speakerMode();
        ResourceLocation portrait = portraitTexture();
        int[] portraitDims = portrait == null ? new int[] {0, 0} : portraitSize(portrait);
        int portraitWidth = portraitDims[0];
        int portraitHeight = portraitDims[1];

        Component shownText = typing ? truncate(payload.text(), typed) : payload.text();
        Component rounded = SqdTheme.rounded(shownText);

        int identityHeight = speaker ? LINE_HEIGHT : 0;
        int nameHeight = speaker ? LINE_HEIGHT : 0;
        int portraitBlock = speaker ? PORTRAIT_BOX : 0;
        int leftColumn = identityHeight + 4 + portraitBlock + 6 + nameHeight;

        int maxPanelWidth = Math.min(BOX_MAX_WIDTH, width - MARGIN * 2);
        int panelWidth;
        int textWidth;
        int textColumnOffset = 0;
        if (speaker) {
            panelWidth = maxPanelWidth;
            textColumnOffset = (portraitWidth > 0 ? PORTRAIT_BOX : 48) + PORTRAIT_GAP;
            textWidth = panelWidth - PADDING * 2 - textColumnOffset;
        } else {
            textWidth = Math.max(120, maxPanelWidth - PADDING * 2);
            panelWidth = maxPanelWidth;
        }

        List<FormattedCharSequence> fullLines = font.split(rounded, Math.max(60, textWidth));
        if (!speaker) {
            int measured = 0;
            for (FormattedCharSequence line : fullLines) {
                measured = Math.max(measured, font.width(line));
            }
            panelWidth = Mth.clamp(measured + PADDING * 2, Math.min(BOX_MIN_WIDTH, maxPanelWidth),
                    maxPanelWidth);
            textWidth = panelWidth - PADDING * 2;
            fullLines = font.split(rounded, Math.max(60, textWidth));
        }

        int optionsHeight = options.isEmpty() ? 0 : OPTION_GAP + options.size() * OPTION_HEIGHT;
        int bodyHeight = Math.max(fullLines.size() * LINE_HEIGHT, speaker ? leftColumn : 0);
        int panelHeight = PADDING * 2 + bodyHeight + optionsHeight;
        panelHeight = Math.min(panelHeight, height - MARGIN * 2);

        this.boxLeft = (width - panelWidth) / 2;
        this.boxRight = boxLeft + panelWidth;
        int boxTop;
        switch (cinematic() ? DialoguePlacement.BOTTOM : payload.placement()) {
            case CENTER -> boxTop = (height - panelHeight) / 2;
            case TOP -> boxTop = MARGIN;
            default -> boxTop = height - MARGIN - panelHeight;
        }

        if (cinematic()) {
            graphics.fill(0, 0, width, height, 0x40000000);
        }
        GlassPanel.draw(graphics, boxLeft, boxTop, panelWidth, panelHeight, 12);

        int x = boxLeft + PADDING;
        int y = boxTop + PADDING;

        if (speaker) {
            int columnCenter = x + (portraitWidth > 0 ? PORTRAIT_BOX : 48) / 2;
            Component identity = payload.identity()
                    .map(SqdTheme::rounded)
                    .orElseGet(() -> SqdTheme.rounded(UNKNOWN_TEXT));
            int identityColor = payload.identity().isPresent() ? SqdTheme.ACCENT : SqdTheme.UNKNOWN;
            drawCentered(graphics, font, identity, columnCenter, y, identityColor);

            int portraitTop = y + identityHeight + 4;
            if (portrait != null) {
                int portraitX = x + (Math.max(PORTRAIT_BOX, portraitWidth) - portraitWidth) / 2;
                graphics.blit(portrait, portraitX, portraitTop + (PORTRAIT_BOX - portraitHeight) / 2,
                        0, 0, portraitWidth, portraitHeight, portraitWidth, portraitHeight);
            } else {
                GraphicsInset.draw(graphics, x, portraitTop, PORTRAIT_BOX, PORTRAIT_BOX);
                Component question = SqdTheme.rounded("?");
                drawCentered(graphics, font, question, x + PORTRAIT_BOX / 2,
                        portraitTop + PORTRAIT_BOX / 2 - LINE_HEIGHT / 2, SqdTheme.UNKNOWN);
            }

            Component name = payload.speakerName()
                    .map(SqdTheme::rounded)
                    .orElseGet(() -> SqdTheme.rounded(UNKNOWN_TEXT));
            int nameColor = payload.speakerName().isPresent() ? SqdTheme.NAME : SqdTheme.UNKNOWN;
            drawCentered(graphics, font, name, columnCenter,
                    y + identityHeight + 4 + PORTRAIT_BOX + 6, nameColor);

            x += textColumnOffset;
            y = boxTop + PADDING;
        }

        this.textLeft = x;
        int textTop = y;
        if (speaker) {
            for (FormattedCharSequence line : fullLines) {
                graphics.drawString(font, line, x, y, TEXT_COLOR, false);
                y += LINE_HEIGHT;
            }
        } else {
            for (FormattedCharSequence line : fullLines) {
                graphics.drawString(font, line, boxLeft + (panelWidth - font.width(line)) / 2, y,
                        TEXT_COLOR, false);
                y += LINE_HEIGHT;
            }
        }

        this.spans.clear();
        this.hoveredSpan = -1;
        if (!typing) {
            collectSpans(font, textTop, x, Math.max(60, textWidth), mouseX, mouseY);
        }

        this.optionLeft = x;
        int optionTopY = boxTop + PADDING + bodyHeight + OPTION_GAP;
        this.optionTop = optionTopY;
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
            graphics.drawString(font, SqdTheme.rounded(options.get(i)), optionLeft + OPTION_INDENT,
                    optionY, isHovered ? OPTION_HOVER_COLOR : OPTION_COLOR, false);
        }

        if (!cinematic() && options.isEmpty() && !typing) {
            graphics.drawString(font, Component.translatable("sqdclib.dialogue.esc"),
                    boxRight - PADDING - 30, boxTop + PADDING, HINT_COLOR, false);
        }
    }

    private static void drawCentered(GuiGraphics graphics, Font font, Component text, int centerX,
                                     int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
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
                (codePoint, style) -> font.width(Component.literal(new String(Character.toChars(codePoint)))
                        .withStyle(style)));
        List<FormattedText> wrapped = splitter.splitLines(SqdTheme.rounded(payload.text()), width, Style.EMPTY);
        for (int lineIndex = 0; lineIndex < wrapped.size(); lineIndex++) {
            int y = top + lineIndex * LINE_HEIGHT;
            int[] cursor = {left};
            wrapped.get(lineIndex).visit((style, text) -> {
                int spanWidth = font.width(Component.literal(text).withStyle(style));
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

    /** 肖像缺失时用的占位玻璃方块。 */
    private static final class GraphicsInset {
        private static void draw(GuiGraphics graphics, int x, int y, int width, int height) {
            GlassPanel.draw(graphics, x, y, width, height, 8);
        }
    }
}
