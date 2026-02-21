package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;


/**
/**
 * Class: AudioSource
 * Wraps a LibGDX Sound object for short sound effects.
 * * OOP Concepts & Design Patterns:
 * - Encapsulation: Protects the native LibGDX Sound object and internal loop IDs. It exposes 
 * safe methods for volume control and prevents invalid volume states using the clamp01 helper.
 * - Resource Management: Centralizes the dispose() call, ensuring that native C++ audio resources are safely released from memory when no longer needed.
 */

public class AudioSource {
    private final Sound sound;

    private boolean looping;
    private float volume;          // local volume (0.0 to 1.0)
    private float lastMaster = 1f; // cache of the master volume
    private long loopId = -1;      // active loop ID (used to control looping sound)

    // Constructors (Overloading for flexibility)
    public AudioSource(String internalAssetPath) {
        this(internalAssetPath, false, 1f);
    }

    public AudioSource(String internalAssetPath, boolean looping, float volume) {
        System.out.println("[DEBUG AudioSource] Loading: " + internalAssetPath);
        FileHandle file = Gdx.files.internal(internalAssetPath);
        System.out.println("[DEBUG AudioSource] File exists: " + file.exists() + ", Path: " + file.path());

        // Load sound from internal assets
        this.sound = Gdx.audio.newSound(file);
        this.looping = looping;
        this.volume = clamp01(volume);
        System.out.println("[DEBUG AudioSource] Loaded successfully with volume: " + this.volume);
    }


    /**
     * play sound using (local volume × master volume)
    */

    public void play(float masterVolume) {
        lastMaster = clamp01(masterVolume);
        float finalVol = clamp01(volume * lastMaster);
        
        System.out.println("[DEBUG AudioSource] Playing sound - volume: " + volume + ", masterVolume: " + masterVolume + ", finalVol: " + finalVol);

        if (looping) {
            stop(); // restart loop to avoid overlapping
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

        // update volume immediately if looping
        if (loopId != -1) {
            sound.setVolume(loopId, clamp01(this.volume * lastMaster));
        }
    }

    // --- Mutators for dynamic volume adjustment ---

    public void setMasterVolume(float masterVolume) {
        lastMaster = clamp01(masterVolume);

        // adjust active loop volume if playing
        if (loopId != -1) {
            sound.setVolume(loopId, clamp01(this.volume * lastMaster));
        }
    }

    // dispose native resources to prevent memory leaks
    public void dispose() {
        sound.dispose();
    }

    // helper to keep volume between 0.0 and 1.0
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
