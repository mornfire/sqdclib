package com.mornfire.sqdclib.cutscene.client;

import com.mornfire.sqdclib.cutscene.CameraKeyframe;
import com.mornfire.sqdclib.cutscene.CreditsRoll;
import com.mornfire.sqdclib.cutscene.CutsceneDefinition;
import com.mornfire.sqdclib.cutscene.CutsceneEvent;
import com.mornfire.sqdclib.cutscene.Timeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ClientCutscenes {
    private static final List<ActiveText> TEXTS = new ArrayList<>();

    private static CutsceneDefinition definition;
    private static int tick;
    private static float fadeFrom;
    private static float fadeTo;
    private static int fadeTicks;
    private static int fadeElapsed;

    private record ActiveText(Component text, int endTick) {}

    public record CameraSample(Vec3 position, float yaw, float pitch, float roll, float fov) {}

    public static boolean isPlaying() {
        return definition != null;
    }

    public static boolean letterbox() {
        return definition != null && definition.letterbox();
    }

    public static boolean lockMovement() {
        return definition != null && definition.lockMovement();
    }

    public static boolean isSkippable() {
        return definition != null && definition.skippable();
    }

    public static Optional<CutsceneDefinition.Credits> credits() {
        return definition == null ? Optional.empty() : definition.credits();
    }

    public static void start(CutsceneDefinition cutscene, int startTick) {
        definition = cutscene;
        tick = Math.max(0, startTick);
        TEXTS.clear();
        fadeFrom = 0.0F;
        fadeTo = 0.0F;
        fadeTicks = 0;
        fadeElapsed = 0;
    }

    public static void seekTo(int target) {
        if (definition == null) {
            return;
        }
        tick = Math.max(0, target);
        TEXTS.clear();
        fadeFrom = 0.0F;
        fadeTo = 0.0F;
        fadeTicks = 0;
        fadeElapsed = 0;
        ClientQte.clear();
    }

    public static void stop() {
        definition = null;
        tick = 0;
        TEXTS.clear();
        fadeFrom = 0.0F;
        fadeTo = 0.0F;
        fadeTicks = 0;
        fadeElapsed = 0;
        ClientQte.clear();
        ClientStandIn.clear();
    }

    public static void clientTick() {
        if (definition == null) {
            return;
        }
        tick++;
        for (CutsceneEvent event : definition.events()) {
            if (event.time() != tick) {
                continue;
            }
            event.text().ifPresent(text -> TEXTS.add(new ActiveText(text, tick + event.duration())));
            event.fade().ifPresent(fade -> {
                fadeFrom = fade(0.0F);
                fadeTo = fade.to();
                fadeTicks = fade.ticks();
                fadeElapsed = 0;
            });
            event.sound().ifPresent(ClientCutscenes::playCue);
        }
        TEXTS.removeIf(text -> tick >= text.endTick());
        if (fadeElapsed < fadeTicks) {
            fadeElapsed++;
        }
        if (tick >= definition.duration()) {
            stop();
        }
    }

    public static List<Component> activeTexts() {
        return TEXTS.stream().map(ActiveText::text).toList();
    }

    public static float fade(float partialTick) {
        if (fadeTicks <= 0) {
            return fadeTo;
        }
        float progress = Math.min(1.0F, (fadeElapsed + partialTick) / fadeTicks);
        return Mth.lerp(progress, fadeFrom, fadeTo);
    }

    public static float creditsProgress(float partialTick) {
        if (definition == null || definition.credits().isEmpty()) {
            return -1.0F;
        }
        CutsceneDefinition.Credits spec = definition.credits().get();
        float elapsed = tick + partialTick;
        if (elapsed < spec.start()) {
            return -1.0F;
        }
        return CreditsRoll.progress(elapsed, spec.start(), spec.duration(), spec.speedUp());
    }

    public static CameraSample camera(float partialTick) {
        if (definition == null) {
            return null;
        }
        return sample(definition.camera(), tick + partialTick);
    }

    public static void applyToCamera(Camera camera, float partialTick) {
        CameraSample sample = camera(partialTick);
        if (sample == null) {
            return;
        }
        camera.setPosition(sample.position());
        camera.setRotation(sample.yaw(), sample.pitch(), sample.roll());
    }

    private static void playCue(CutsceneEvent.SoundCue cue) {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(cue.sound().value(), cue.pitch(), cue.volume()));
    }

    private static CameraSample sample(List<CameraKeyframe> frames, float time) {
        if (frames.isEmpty()) {
            return null;
        }
        int[] times = new int[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            times[i] = frames.get(i).time();
        }
        Timeline.Segment segment = Timeline.locate(times, time);
        if (segment == null) {
            return null;
        }
        CameraKeyframe from = frames.get(segment.from());
        CameraKeyframe to = frames.get(segment.to());
        if (from == to) {
            return exact(from);
        }
        float eased = to.easing().apply(segment.progress());
        float fromYaw = from.resolvedYaw();
        float toYaw = to.resolvedYaw();
        float fromPitch = from.resolvedPitch();
        float toPitch = to.resolvedPitch();
        return new CameraSample(
                from.position().lerp(to.position(), eased),
                Mth.rotLerp(eased, fromYaw, toYaw),
                Mth.lerp(eased, fromPitch, toPitch),
                Mth.lerp(eased, from.roll(), to.roll()),
                Mth.lerp(eased, from.fov(), to.fov()));
    }

    private static CameraSample exact(CameraKeyframe frame) {
        return new CameraSample(frame.position(), frame.resolvedYaw(), frame.resolvedPitch(),
                frame.roll(), frame.fov());
    }

    private ClientCutscenes() {}
}
