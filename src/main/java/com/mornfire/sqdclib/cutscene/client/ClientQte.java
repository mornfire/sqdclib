package com.mornfire.sqdclib.cutscene.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mornfire.sqdclib.cutscene.QteCue;
import com.mornfire.sqdclib.cutscene.net.QteResultPayload;
import com.mornfire.sqdclib.cutscene.net.QteStartPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class ClientQte {
    private static final int MASH_TARGET = 5;
    private static final int BOX_WIDTH = 150;
    private static final int BOX_HEIGHT = 42;
    private static final int BACKGROUND = 0xC0000000;
    private static final int BAR_BACKGROUND = 0xFF303030;
    private static final int BAR_COLOR = 0xFFFFD24A;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private record Active(int id, QteCue.Kind kind, String key, int duration, Component prompt,
            int elapsed, int presses) {}

    @Nullable
    private static Active active;

    public static boolean isActive() {
        return active != null;
    }

    public static void start(QteStartPayload payload) {
        Component prompt = payload.prompt().orElseGet(() -> Component.translatable("sqdclib.qte.prompt"));
        active = new Active(payload.id(), payload.kind(), payload.key(), payload.duration(), prompt, 0, 0);
    }

    public static void tick() {
        Active current = active;
        if (current == null) {
            return;
        }
        if (current.elapsed() + 1 >= current.duration()) {
            finish(false);
            return;
        }
        active = new Active(current.id(), current.kind(), current.key(), current.duration(), current.prompt(),
                current.elapsed() + 1, current.presses());
    }

    public static void onKey(int keyCode) {
        Active current = active;
        if (current == null || keyCode != targetKey(current.key())) {
            return;
        }
        int presses = current.presses() + 1;
        if (current.kind() == QteCue.Kind.MASH && presses < MASH_TARGET) {
            active = new Active(current.id(), current.kind(), current.key(), current.duration(), current.prompt(),
                    current.elapsed(), presses);
            return;
        }
        finish(true);
    }

    public static void clear() {
        active = null;
    }

    private static int targetKey(String name) {
        try {
            return InputConstants.getKey(name).getValue();
        } catch (Throwable throwable) {
            return InputConstants.UNKNOWN.getValue();
        }
    }

    private static void finish(boolean success) {
        Active current = active;
        if (current == null) {
            return;
        }
        active = null;
        PacketDistributor.sendToServer(new QteResultPayload(current.id(), success));
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Active current = active;
        if (current == null) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int left = (width - BOX_WIDTH) / 2;
        int top = height * 2 / 3;
        graphics.fill(left, top, left + BOX_WIDTH, top + BOX_HEIGHT, BACKGROUND);

        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, current.prompt(), width / 2, top + 6, TEXT_COLOR);

        Component keyName = keyLabel(current);
        graphics.drawCenteredString(font, keyName, width / 2, top + 18, BAR_COLOR);

        int barLeft = left + 10;
        int barWidth = BOX_WIDTH - 20;
        float remaining = 1.0F - Math.min(1.0F, current.elapsed() / (float) Math.max(1, current.duration()));
        graphics.fill(barLeft, top + BOX_HEIGHT - 8, barLeft + barWidth, top + BOX_HEIGHT - 5, BAR_BACKGROUND);
        graphics.fill(barLeft, top + BOX_HEIGHT - 8,
                barLeft + Math.max(1, (int) (barWidth * remaining)), top + BOX_HEIGHT - 5, BAR_COLOR);
    }

    private static Component keyLabel(Active current) {
        Component keyName;
        try {
            keyName = InputConstants.getKey(current.key()).getDisplayName();
        } catch (Throwable throwable) {
            keyName = Component.literal(current.key());
        }
        if (current.kind() == QteCue.Kind.MASH) {
            return Component.translatable("sqdclib.qte.mash", keyName, MASH_TARGET - current.presses());
        }
        return keyName;
    }

    private ClientQte() {}
}
