package io.github.team6.managers;

import java.util.ArrayList;
import java.util.List;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.inputoutput.MusicSource;

/**
 * OutputManager:
 * centralizes all audio control for the game
 * handles sound effects, background music, and volume management
 */
public class OutputManager {

    // active sound effects
    private final List<AudioSource> activeSfx;

    // current background music track
    private MusicSource bgm;

    // volume controls (0.0f to 1.0f)
    private float masterVolume;
    private float sfxVolume;
    private float musicVolume;

    public OutputManager() {
        activeSfx = new ArrayList<>();
        masterVolume = 1f;
        sfxVolume = 1.0f;
        musicVolume = 0.3f;  // softer bgm by default
    }

    // ---- Sound Effects ----

    /**
     * play a sound effect using (master × SFX volume)
     */
    public void play(AudioSource sfx) {
        if (sfx == null) return;

        // track sound so we can stop/cleanup later
        if (!activeSfx.contains(sfx)) {
            activeSfx.add(sfx);
        }

        float finalVolume = clamp(masterVolume * sfxVolume);
        System.out.println("[DEBUG] Playing SFX - masterVolume: " + masterVolume + ", sfxVolume: " + sfxVolume + ", finalVolume: " + finalVolume);
        sfx.play(finalVolume);
    }

    /**
     * stop all currently tracked sound effects
     */

    public void stopAllSfx() {
        for (AudioSource s : activeSfx) {
            s.stop();
        }
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
    }

    // ---- Background Music ----

    /**
     * set a new background track
     * existing track is stopped and disposed before replacement
     */
    public void setBgm(MusicSource newBgm) {
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
        }

        bgm = newBgm;

        if (bgm != null) {
            bgm.setVolume(clamp(masterVolume * musicVolume));
        }
    }

    /**
     * start background music playback.
     */

    public void playBgm(boolean looping) {
        if (bgm == null) return;

        bgm.setVolume(clamp(masterVolume * musicVolume));
        bgm.play(looping);
    }

    public void stopBgm() {
        if (bgm != null) {
            bgm.stop();
        }
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);

        if (bgm != null) {
            bgm.setVolume(clamp(masterVolume * musicVolume));
        }
    }

    // ---- Master Volume ----

    /**
     * update global volume and apply to current BGM
     */
    public void setMasterVolume(float volume) {
        masterVolume = clamp(volume);

        if (bgm != null) {
            bgm.setVolume(clamp(masterVolume * musicVolume));
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }

   // ---- Cleanup ----

    /**
     * release all audio resources
     * called during scene transitions or shutdown
     */
    public void dispose() {
        stopAllSfx();

        for (AudioSource s : activeSfx) {
            s.dispose();
        }

        activeSfx.clear();

        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
            bgm = null;
        }
    }
    // keep volume within valid range
    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
