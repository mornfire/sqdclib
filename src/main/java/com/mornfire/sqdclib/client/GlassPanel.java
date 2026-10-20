package com.mornfire.sqdclib.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * 液态玻璃面板：圆角 + 垂直渐变 + 顶部高光 + 底部冷色描边。
 *
 * <p>Minecraft 没有真正的背景模糊，所以这里用半透明深蓝底、内高光和一条很淡的斜向流光
 * 去模拟玻璃的厚度；阴影往右下偏 3px，避免面板像贴纸一样浮在画面上。
 */
public final class GlassPanel {
    private static final int BODY_TOP = 0xE0222C4C;
    private static final int BODY_BOTTOM = 0xE00A0D18;
    private static final int SHADOW = 0x4A000000;
    private static final int EDGE_LIGHT = 0x77DCEBFF;
    private static final int EDGE_DARK = 0x66000000;

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height, int radius) {
        if (width < 2 || height < 2) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        // 阴影
        gradient(graphics, x - 2, y + 3, width + 4, height + 2, r + 2, SHADOW, 0x1A000000);
        // 玻璃本体
        gradient(graphics, x, y, width, height, r, BODY_TOP, BODY_BOTTOM);
        // 顶部内高光：一小段柔和的亮带
        for (int i = 0; i < Math.min(6, height / 3); i++) {
            int inset = edgeInset(i, height, r) + 1;
            int alpha = (int) (0x1E * (1.0F - i / 6.0F));
            if (alpha <= 0) {
                continue;
            }
            graphics.fill(x + inset, y + i, x + width - inset, y + i + 1, alpha << 24 | 0xFFFFFF);
        }
        // 斜向流光
        int sheenEnd = Math.max(1, (int) (width * 0.62F));
        for (int row = 0; row < height; row++) {
            int inset = edgeInset(row, height, r);
            int bandX = x + inset + (int) (row / (float) Math.max(1, height) * sheenEnd);
            int bandRight = Math.min(x + width - inset, bandX + 3);
            if (bandRight > bandX) {
                graphics.fill(bandX, y + row, bandRight, y + row + 1, 0x08FFFFFF);
            }
        }
        // 描边
        graphics.fill(x + r, y, x + width - r, y + 1, EDGE_LIGHT);
        graphics.fill(x + r, y + height - 1, x + width - r, y + height, EDGE_DARK);
        graphics.fill(x, y + r, x + 1, y + height - r, 0x44FFFFFF);
        graphics.fill(x + width - 1, y + r, x + width, y + height - r, 0x33000000);
        // 底部冷色细线
        int accentLeft = x + width / 3;
        int accentRight = x + width * 2 / 3;
        graphics.fill(accentLeft, y + height - 2, accentRight, y + height - 1, 0x66A8D8FF);
    }

    private static void gradient(GuiGraphics graphics, int x, int y, int width, int height,
                                 int radius, int top, int bottom) {
        for (int row = 0; row < height; row++) {
            int inset = edgeInset(row, height, radius);
            int color = lerp(top, bottom, height <= 1 ? 0.0F : row / (float) (height - 1));
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    private static int edgeInset(int row, int height, int radius) {
        if (radius <= 0) {
            return 0;
        }
        double dy;
        if (row < radius) {
            dy = radius - row - 0.5D;
        } else if (row >= height - radius) {
            dy = row - (height - radius) + 0.5D;
        } else {
            return 0;
        }
        double clamped = Mth.clamp(dy, 0.0D, radius);
        return radius - (int) Math.round(Math.sqrt(Math.max(0.0D, radius * radius - clamped * clamped)));
    }

    private static int lerp(int a, int b, float t) {
        int aa = (a >>> 24) & 0xFF;
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int ca = Mth.lerpInt(t, aa, ba);
        int cr = Mth.lerpInt(t, ar, br);
        int cg = Mth.lerpInt(t, ag, bg);
        int cb = Mth.lerpInt(t, ab, bb);
        return ca << 24 | cr << 16 | cg << 8 | cb;
    }

    private GlassPanel() {}
}
