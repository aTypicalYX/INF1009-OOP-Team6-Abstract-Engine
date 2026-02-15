package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class AudioSource {
    private final Sound sound;
    private boolean looping;
    private float volume;          // 0..1 (per-sound)
    private float lastMaster = 1f; 
    private long loopId = -1;      // for looping purposes

    public AudioSource(String internalAssetPath) {
        this(internalAssetPath, false, 1f);
    }

    public AudioSource(String internalAssetPath, boolean looping, float volume) {
        System.out.println("[DEBUG AudioSource] Loading: " + internalAssetPath);
        FileHandle file = Gdx.files.internal(internalAssetPath);
        System.out.println("[DEBUG AudioSource] File exists: " + file.exists() + ", Path: " + file.path());
        this.sound = Gdx.audio.newSound(file);
        this.looping = looping;
        this.volume = clamp01(volume);
        System.out.println("[DEBUG AudioSource] Loaded successfully with volume: " + this.volume);
    }

    public void play(float masterVolume) {
        lastMaster = clamp01(masterVolume);
        float finalVol = clamp01(volume * lastMaster);
        
        System.out.println("[DEBUG AudioSource] Playing sound - volume: " + volume + ", masterVolume: " + masterVolume + ", finalVol: " + finalVol);

        if (looping) {
            stop();
            loopId = sound.loop(finalVol);
        } else {
            sound.play(finalVol);
            System.out.println("[DEBUG AudioSource] sound.play() called with volume: " + finalVol);
        }
    }

    public void stop() {
        sound.stop();
        loopId = -1;
    }

    public void setLooping(boolean looping) {
        // if switching to looping while playing, simplest is restart on next play()
        this.looping = looping;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setVolume(float volume) {
        this.volume = clamp01(volume);

        // If currently looping, update the active loop volume
        if (loopId != -1) {
            sound.setVolume(loopId, clamp01(this.volume * lastMaster));
        }
    }

    public void setMasterVolume(float masterVolume) {
        lastMaster = clamp01(masterVolume);
        if (loopId != -1) {
            sound.setVolume(loopId, clamp01(this.volume * lastMaster));
        }
    }

    public void dispose() {
        sound.dispose();
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
