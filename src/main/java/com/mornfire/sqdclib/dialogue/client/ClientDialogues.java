package com.mornfire.sqdclib.dialogue.client;

import com.mornfire.sqdclib.dialogue.DialogueMode;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSelectPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class ClientDialogues {
    @Nullable
    private static DialoguePanel panel;
    private static boolean lockMovement;

    public static boolean lockMovement() {
        return lockMovement;
    }

    @Nullable
    public static DialoguePanel panel() {
        return panel;
    }

    public static void open(DialogueOpenPayload payload) {
        boolean cinematic = payload.mode() == DialogueMode.CINEMATIC;
        boolean blocking = cinematic || payload.input().isPresent();
        lockMovement = cinematic || payload.lockMovement();
        Minecraft minecraft = Minecraft.getInstance();
        if (blocking) {
            panel = null;
            minecraft.setScreen(new DialogueScreen(payload));
            return;
        }
        panel = new DialoguePanel(payload);
        if (minecraft.screen instanceof DialogueScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void close() {
        panel = null;
        lockMovement = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof DialogueScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void tick() {
        if (panel != null) {
            panel.tick();
        }
    }

    public static void select(int index) {
        PacketDistributor.sendToServer(new DialogueSelectPayload(index));
    }

    private ClientDialogues() {}
}
