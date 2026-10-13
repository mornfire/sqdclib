package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientDialogues {
    private static boolean lockMovement;

    public static boolean lockMovement() {
        return lockMovement;
    }

    public static void open(DialogueOpenPayload payload) {
        lockMovement = payload.lockMovement();
        Minecraft.getInstance().setScreen(new DialogueScreen(payload));
    }

    public static void close() {
        lockMovement = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof DialogueScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void dismiss(Screen screen) {
        screen.getMinecraft().setScreen(null);
    }

    private ClientDialogues() {}
}
