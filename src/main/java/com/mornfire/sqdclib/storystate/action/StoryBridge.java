package com.mornfire.sqdclib.storystate.action;

import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class StoryBridge {
    private static BiConsumer<ServerPlayer, ResourceLocation> dialogueStarter = (player, id) -> {};
    private static BiConsumer<ServerPlayer, ResourceLocation> cutsceneStarter = (player, id) -> {};
    private static BiConsumer<ServerPlayer, ResourceLocation> questStarter = (player, id) -> {};
    private static BiConsumer<ServerPlayer, ResourceLocation> questCompleter = (player, id) -> {};

    public static void dialogueStarter(BiConsumer<ServerPlayer, ResourceLocation> handler) {
        dialogueStarter = handler;
    }

    public static void cutsceneStarter(BiConsumer<ServerPlayer, ResourceLocation> handler) {
        cutsceneStarter = handler;
    }

    public static void questStarter(BiConsumer<ServerPlayer, ResourceLocation> handler) {
        questStarter = handler;
    }

    public static void questCompleter(BiConsumer<ServerPlayer, ResourceLocation> handler) {
        questCompleter = handler;
    }

    public static void startDialogue(ServerPlayer player, ResourceLocation dialogue) {
        dialogueStarter.accept(player, dialogue);
    }

    public static void playCutscene(ServerPlayer player, ResourceLocation cutscene) {
        cutsceneStarter.accept(player, cutscene);
    }

    public static void startQuest(ServerPlayer player, ResourceLocation quest) {
        questStarter.accept(player, quest);
    }

    public static void completeQuest(ServerPlayer player, ResourceLocation quest) {
        questCompleter.accept(player, quest);
    }

    private StoryBridge() {}
}
