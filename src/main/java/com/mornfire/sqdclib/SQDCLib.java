package com.mornfire.sqdclib;

import com.mojang.logging.LogUtils;
import com.mornfire.sqdclib.cutscene.CutsceneDefinition;
import com.mornfire.sqdclib.cutscene.Cutscenes;
import com.mornfire.sqdclib.cutscene.net.CutsceneSeekPayload;
import com.mornfire.sqdclib.cutscene.net.CutsceneSkipPayload;
import com.mornfire.sqdclib.cutscene.net.CutsceneStartPayload;
import com.mornfire.sqdclib.cutscene.net.CutsceneStopPayload;
import com.mornfire.sqdclib.cutscene.net.QteResultPayload;
import com.mornfire.sqdclib.cutscene.net.QteStartPayload;
import com.mornfire.sqdclib.cutscene.net.StandInPayload;
import com.mornfire.sqdclib.dialogue.DialogueDefinition;
import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.dialogue.net.DialogueClosePayload;
import com.mornfire.sqdclib.dialogue.net.DialogueOpenPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSelectPayload;
import com.mornfire.sqdclib.dialogue.net.DialogueSubmitPayload;
import com.mornfire.sqdclib.music.net.MusicStartPayload;
import com.mornfire.sqdclib.music.net.MusicStopPayload;
import com.mornfire.sqdclib.quest.QuestDefinition;
import com.mornfire.sqdclib.quest.Quests;
import com.mornfire.sqdclib.storystate.StoryProgress;
import com.mornfire.sqdclib.storystate.action.StoryBridge;
import com.mornfire.sqdclib.storystate.net.WorldStatePayload;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

@Mod(SQDCLib.MOD_ID)
@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class SQDCLib {
    public static final String MOD_ID = "sqdclib";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StoryProgress>> STORY_PROGRESS =
            ATTACHMENT_TYPES.register("story_progress", () -> AttachmentType
                    .serializable(holder -> new StoryProgress(holder instanceof Player player ? player : null))
                    .copyOnDeath()
                    .sync(StoryProgress.SYNC_HANDLER)
                    .build());

    public SQDCLib(IEventBus modEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerDataPackRegistries);

        StoryBridge.questStarter(Quests::start);
        StoryBridge.questCompleter(Quests::complete);
        StoryBridge.dialogueStarter(Dialogues::start);
        StoryBridge.cutsceneStarter(Cutscenes::play);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToClient(WorldStatePayload.TYPE, WorldStatePayload.STREAM_CODEC, WorldStatePayload::handle);
        registrar.playToClient(DialogueOpenPayload.TYPE, DialogueOpenPayload.STREAM_CODEC,
                DialogueOpenPayload::handle);
        registrar.playToClient(DialogueClosePayload.TYPE, DialogueClosePayload.STREAM_CODEC,
                DialogueClosePayload::handle);
        registrar.playToServer(DialogueSelectPayload.TYPE, DialogueSelectPayload.STREAM_CODEC,
                DialogueSelectPayload::handle);
        registrar.playToClient(CutsceneStartPayload.TYPE, CutsceneStartPayload.STREAM_CODEC,
                CutsceneStartPayload::handle);
        registrar.playToClient(CutsceneStopPayload.TYPE, CutsceneStopPayload.STREAM_CODEC,
                CutsceneStopPayload::handle);
        registrar.playToClient(CutsceneSeekPayload.TYPE, CutsceneSeekPayload.STREAM_CODEC,
                CutsceneSeekPayload::handle);
        registrar.playToServer(CutsceneSkipPayload.TYPE, CutsceneSkipPayload.STREAM_CODEC,
                CutsceneSkipPayload::handle);
        registrar.playToServer(DialogueSubmitPayload.TYPE, DialogueSubmitPayload.STREAM_CODEC,
                DialogueSubmitPayload::handle);
        registrar.playToClient(MusicStartPayload.TYPE, MusicStartPayload.STREAM_CODEC, MusicStartPayload::handle);
        registrar.playToClient(MusicStopPayload.TYPE, MusicStopPayload.STREAM_CODEC, MusicStopPayload::handle);
        registrar.playToClient(QteStartPayload.TYPE, QteStartPayload.STREAM_CODEC, QteStartPayload::handle);
        registrar.playToServer(QteResultPayload.TYPE, QteResultPayload.STREAM_CODEC, QteResultPayload::handle);
        registrar.playToClient(StandInPayload.TYPE, StandInPayload.STREAM_CODEC, StandInPayload::handle);
    }

    private void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Quests.REGISTRY_KEY, QuestDefinition.CODEC, QuestDefinition.CODEC);
        event.dataPackRegistry(Dialogues.REGISTRY_KEY, DialogueDefinition.CODEC, DialogueDefinition.CODEC);
        event.dataPackRegistry(Cutscenes.REGISTRY_KEY, CutsceneDefinition.CODEC, CutsceneDefinition.CODEC);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        RegistryAccess access = event.getServer().overworld().registryAccess();
        logRegistry(access, Quests.REGISTRY_KEY, "story_quest");
        logRegistry(access, Dialogues.REGISTRY_KEY, "story_dialogue");
        logRegistry(access, Cutscenes.REGISTRY_KEY, "story_cutscene");
    }

    private static <T> void logRegistry(RegistryAccess access, ResourceKey<Registry<T>> key, String label) {
        access.registry(key).ifPresentOrElse(
                registry -> LOGGER.info("[sqdclib] {} = {} entries {}", label, registry.size(), registry.keySet()),
                () -> LOGGER.warn("[sqdclib] {} registry is missing entirely", label));
    }
}
