package com.mornfire.sqdclib.quest.client;

import com.mornfire.sqdclib.quest.QuestDefinition;
import com.mornfire.sqdclib.quest.QuestObjective;
import com.mornfire.sqdclib.quest.QuestStatus;
import com.mornfire.sqdclib.quest.Quests;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuestScreen extends Screen {
    private static final int MARGIN = 30;
    private static final int CARD_PADDING = 10;
    private static final int LINE = 11;
    private static final int CARD_GAP = 8;
    private static final int MAX_WIDTH = 420;
    private static final int BACKGROUND = 0xE0101010;
    private static final int CARD_BACKGROUND = 0x80000000;
    private static final int DESC_COLOR = 0xFF9A9A9A;
    private static final int ACTIVE_COLOR = 0xFFFFD24A;
    private static final int DONE_COLOR = 0xFF7FE07F;
    private static final int FAILED_COLOR = 0xFFFF7F7F;
    private static final int AVAILABLE_COLOR = 0xFFBFBFBF;

    private record Line(Component text, int color) {}

    private record Card(Component title, int color, List<Line> lines, QuestStatus status) {}

    private final List<Card> cards = new ArrayList<>();
    private int scroll;

    public QuestScreen() {
        super(Component.translatable("sqdclib.quest.title"));
    }

    @Override
    protected void init() {
        buildCards();
    }

    private void buildCards() {
        cards.clear();
        LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
        if (player == null) {
            return;
        }
        List<Card> built = new ArrayList<>();
        for (ResourceLocation id : Quests.allIds(player)) {
            Optional<QuestDefinition> entry = Quests.definition(player, id);
            if (entry.isEmpty() || !Quests.isVisible(player, id)) {
                continue;
            }
            QuestStatus status = Quests.status(player, id);
            if (status == QuestStatus.LOCKED) {
                continue;
            }
            QuestDefinition definition = entry.get();
            List<Line> lines = new ArrayList<>();
            definition.description().ifPresent(text -> lines.add(new Line(text, DESC_COLOR)));
            for (QuestObjective objective : definition.objectives()) {
                boolean done = objective.done().test(player);
                Component text = Component.literal(done ? "[x] " : "[ ] ")
                        .append(Quests.objectiveTitle(player, objective));
                lines.add(new Line(text, done ? DONE_COLOR : ACTIVE_COLOR));
            }
            built.add(new Card(definition.title(), colorFor(status), lines, status));
        }
        built.sort(Comparator.comparingInt(card -> card.status().ordinal()));
        cards.addAll(built);
    }

    private static int colorFor(QuestStatus status) {
        return switch (status) {
            case ACTIVE -> ACTIVE_COLOR;
            case COMPLETED -> DONE_COLOR;
            case FAILED -> FAILED_COLOR;
            default -> AVAILABLE_COLOR;
        };
    }

    private static int cardHeight(Card card) {
        return CARD_PADDING * 2 + LINE * (1 + card.lines().size());
    }

    private int maxScroll() {
        int total = MARGIN * 2;
        for (Card card : cards) {
            total += cardHeight(card) + CARD_GAP;
        }
        return Math.max(0, total - this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, BACKGROUND);
        int cardWidth = Math.min(MAX_WIDTH, this.width - MARGIN * 2);
        int left = (this.width - cardWidth) / 2;
        int y = MARGIN - scroll;

        if (cards.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("sqdclib.quest.empty"),
                    this.width / 2, this.height / 2, DESC_COLOR);
        }

        for (Card card : cards) {
            int height = cardHeight(card);
            if (y + height >= MARGIN && y <= this.height - MARGIN) {
                renderCard(graphics, card, left, y, cardWidth);
            }
            y += height + CARD_GAP;
        }

        graphics.drawCenteredString(this.font, this.title, this.width / 2, MARGIN / 2, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(GuiGraphics graphics, Card card, int left, int top, int width) {
        graphics.fill(left, top, left + width, top + cardHeight(card), CARD_BACKGROUND);
        int x = left + CARD_PADDING;
        int y = top + CARD_PADDING;
        graphics.drawString(this.font, card.title(), x, y, card.color(), true);
        y += LINE;
        for (Line line : card.lines()) {
            graphics.drawString(this.font, line.text(), x + 4, y, line.color(), true);
            y += LINE;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = (int) Math.max(0, Math.min(maxScroll(), scroll - scrollY * LINE * 2));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
