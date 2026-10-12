package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import net.minecraft.client.Minecraft;

public final class ClientDialogues {
    public static void open(DialogueOpenPayload payload) {
        Minecraft.getInstance().setScreen(new DialogueScreen(payload));
    }

    public static void close() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof DialogueScreen) {
            minecraft.setScreen(null);
        }
    }

    private ClientDialogues() {}
}
