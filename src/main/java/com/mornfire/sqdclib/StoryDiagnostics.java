package com.mornfire.sqdclib;

import com.mojang.logging.LogUtils;
import com.mornfire.sqdclib.cutscene.Cutscenes;
import com.mornfire.sqdclib.dialogue.Dialogues;
import com.mornfire.sqdclib.quest.Quests;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = SQDCLib.MOD_ID)
public final class StoryDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String JSON = ".json";

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RegistryAccess access = server.overworld().registryAccess();
        ResourceManager resources = server.getResourceManager();
        report(access, resources, Quests.REGISTRY_KEY, "story_quest");
        report(access, resources, Dialogues.REGISTRY_KEY, "story_dialogue");
        report(access, resources, Cutscenes.REGISTRY_KEY, "story_cutscene");
    }

    public static String expectedPrefix(ResourceKey<? extends Registry<?>> key) {
        ResourceLocation id = key.location();
        return id.getNamespace().equals("minecraft")
                ? id.getPath()
                : id.getNamespace() + "/" + id.getPath();
    }

    private static <T> void report(RegistryAccess access, ResourceManager resources,
            ResourceKey<Registry<T>> key, String label) {
        String prefix = expectedPrefix(key);
        Map<ResourceLocation, Resource> found = resources.listResources(prefix,
                path -> path.getPath().endsWith(JSON));

        access.registry(key).ifPresentOrElse(registry -> {
            if (registry.size() == 0) {
                LOGGER.warn("[sqdclib] {} is registered but holds no entries. Datapack directory: data/<pack_namespace>/{}/",
                        label, prefix);
                explainMissing(resources, prefix, label);
            } else {
                LOGGER.info("[sqdclib] {} holds {} entries {} loaded from {} resource file(s) under data/<pack_namespace>/{}/",
                        label, registry.size(), registry.keySet(), found.size(), prefix);
            }
        }, () -> LOGGER.error("[sqdclib] {} was never created. DataPackRegistryEvent did not reach this mod, "
                + "which means the mod constructor or the mod bus listener is missing.", label));
    }

    private static void explainMissing(ResourceManager resources, String prefix, String label) {
        int slash = prefix.indexOf('/');
        if (slash < 0) {
            return;
        }
        String withoutNamespace = prefix.substring(slash + 1);
        Map<ResourceLocation, Resource> misplaced = resources.listResources(withoutNamespace,
                path -> path.getPath().endsWith(JSON));
        if (!misplaced.isEmpty()) {
            LOGGER.warn("[sqdclib] {} found {} json file(s) under '{}' instead. The datapack path is missing "
                            + "the registry namespace layer: it must be data/<pack_namespace>/{}/, not data/<pack_namespace>/{}/",
                    label, misplaced.size(), withoutNamespace, prefix, withoutNamespace);
        } else {
            LOGGER.warn("[sqdclib] {} found no json files anywhere under '{}' or '{}'. Check that the files are "
                            + "present in this jar or pack, and that this is not a stale world with a fixed pack list.",
                    label, prefix, withoutNamespace);
        }
    }

    private StoryDiagnostics() {}
}
