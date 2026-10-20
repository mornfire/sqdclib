package com.mornfire.sqdclib.client;

import com.mornfire.sqdclib.SQDCLib;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/** 过场 / 对话共用的字体与配色。 */
public final class SqdTheme {
    /** assets/sqdclib/font/rounded.json 里的圆体（ZCOOL KuaiLe）。 */
    public static final ResourceLocation ROUNDED =
            ResourceLocation.fromNamespaceAndPath(SQDCLib.MOD_ID, "rounded");

    public static final int TEXT = 0xFFF2F7FF;
    public static final int TEXT_DIM = 0xFFAFC0DA;
    public static final int ACCENT = 0xFF9FD8FF;
    public static final int NAME = 0xFFE8D8A8;
    public static final int UNKNOWN = 0xFF9AA6BA;

    /** 把整段文本切到圆体；缺字由 unifont 兜底。 */
    public static Component rounded(Component text) {
        return text.copy().withStyle(Style.EMPTY.withFont(ROUNDED));
    }

    public static Component rounded(String literal) {
        return Component.literal(literal).withStyle(Style.EMPTY.withFont(ROUNDED));
    }

    private SqdTheme() {}
}
