package com.mornfire.sqdclib.client;

import net.minecraft.client.player.Input;

public final class ClientInputLock {
    public static void clear(Input input) {
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    private ClientInputLock() {}
}
