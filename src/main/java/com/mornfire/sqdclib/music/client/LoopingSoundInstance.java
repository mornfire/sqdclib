package com.mornfire.sqdclib.music.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class LoopingSoundInstance extends AbstractSoundInstance {
    public LoopingSoundInstance(SoundEvent sound, float volume) {
        super(sound, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.looping = true;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    @Override
    public boolean isLooping() {
        return true;
    }
}
