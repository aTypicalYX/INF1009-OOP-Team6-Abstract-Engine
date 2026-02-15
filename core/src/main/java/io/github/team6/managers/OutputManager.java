package io.github.team6.managers;

import java.util.ArrayList;
import java.util.List;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.inputoutput.MusicSource;

/**
 * Handles all audio output (Sound Effects + Background Music)
 */
public class OutputManager {

    // ---------- Sound Effects ----------
    private final List<AudioSource> activeSfx;

    // ---------- Background Music ----------
    private MusicSource bgm;

    // ---------- Volume Controls (0.0f to 1.0f) ----------
    private float masterVolume;
    private float sfxVolume;
    private float musicVolume;

    public OutputManager() {
        activeSfx = new ArrayList<>();
        masterVolume = 1f;
        sfxVolume = 1.0f;
        musicVolume = 0.3f;  // Softer background music
    }

    // =========================
    // SOUND EFFECTS
    // =========================
    public void play(AudioSource sfx) {
        if (sfx == null) return;

        if (!activeSfx.contains(sfx)) {
            activeSfx.add(sfx);
        }

        float finalVolume = clamp(masterVolume * sfxVolume);
        System.out.println("[DEBUG] Playing SFX - masterVolume: " + masterVolume + ", sfxVolume: " + sfxVolume + ", finalVolume: " + finalVolume);
        sfx.play(finalVolume);
    }

    public void stopAllSfx() {
        for (AudioSource s : activeSfx) {
            s.stop();
        }
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
    }

    // =========================
    // BACKGROUND MUSIC
    // =========================
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

    // =========================
    // MASTER VOLUME
    // =========================
    public void setMasterVolume(float volume) {
        masterVolume = clamp(volume);

        if (bgm != null) {
            bgm.setVolume(clamp(masterVolume * musicVolume));
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    // =========================
    // CLEANUP
    // =========================
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

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
