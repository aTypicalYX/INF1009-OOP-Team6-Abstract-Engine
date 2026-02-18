package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;


/**
 * Class: AudioSource
 * Wrapper for sound effects (Short audio clips).
 * OOP Concept: Encapsulation.
 * * Responsibilities:
 * - Manages the lifecycle of a LibGDX 'Sound' object.
 * - Handles volume calculations relative to a global "Master Volume".
 * - Encapsulates looping logic (keeping track of loop IDs).
 */
public class AudioSource {
    // Composition: AudioSource "HAS-A" Sound object.
    private final Sound sound;

    // State variables
    private boolean looping;
    private float volume;          // Local volume (0.0 to 1.0)
    private float lastMaster = 1f; // Cache of the master volume
    private long loopId = -1;      // ID reference for controlling looping sounds

    // Constructors (Overloading for flexibility)
    public AudioSource(String internalAssetPath) {
        this(internalAssetPath, false, 1f);
    }

    public AudioSource(String internalAssetPath, boolean looping, float volume) {
        System.out.println("[DEBUG AudioSource] Loading: " + internalAssetPath);
        FileHandle file = Gdx.files.internal(internalAssetPath);
        System.out.println("[DEBUG AudioSource] File exists: " + file.exists() + ", Path: " + file.path());

        // Factory Method: Gdx.audio.newSound creates the specific implementation
        this.sound = Gdx.audio.newSound(file);
        this.looping = looping;
        this.volume = clamp01(volume);
        System.out.println("[DEBUG AudioSource] Loaded successfully with volume: " + this.volume);
    }


    /**
     * Plays the sound, adjusted by the Master Volume.
     * Logic: finalVolume = localVolume * masterVolume.
     */
    public void play(float masterVolume) {
        lastMaster = clamp01(masterVolume);
        float finalVol = clamp01(volume * lastMaster);
        
        System.out.println("[DEBUG AudioSource] Playing sound - volume: " + volume + ", masterVolume: " + masterVolume + ", finalVol: " + finalVol);

        if (looping) {
            stop(); // Prevent duplicate overlapping loops
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

    // --- Mutators for dynamic volume adjustment ---

    public void setMasterVolume(float masterVolume) {
        lastMaster = clamp01(masterVolume);

        // Real-time update: If sound is currently looping, update its volume immediately
        if (loopId != -1) {
            sound.setVolume(loopId, clamp01(this.volume * lastMaster));
        }
    }

    // Dispose native resources to prevent memory leaks
    public void dispose() {
        sound.dispose();
    }

    // Helper to keep volume between 0.0 and 1.0
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
