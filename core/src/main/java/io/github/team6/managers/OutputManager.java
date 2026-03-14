package io.github.team6.managers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.inputoutput.MusicSource;

/**
 * OutputManager:
 * Centralizes all audio and basic text output control for the game.
 * * * OOP Concepts & Design Patterns:
 * - Facade Pattern: Wraps the low-level LibGDX audio and font systems into a simplified interface.
 * - Separation of Concerns: Isolates output (sound/text) from game logic.
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

    // --- NEW: Text Rendering Engine Component ---
    private BitmapFont font;

    public OutputManager() {
        activeSfx = new ArrayList<>();
        masterVolume = 1f;
        sfxVolume = 1.0f;
        musicVolume = 0.3f;  // softer bgm by default
        
        // Initialize the default font engine
        font = new BitmapFont(); 
    }

    // --- NEW: Text Rendering Method ---

    /**
     * Draws text to the screen using the provided SpriteBatch.
     * This abstracts the font scaling and color settings away from the Scene.
     */
    public void drawText(SpriteBatch batch, String text, float x, float y, float scale) {
        if (font != null && batch != null) {
            font.setColor(1, 1, 1, 1); // Default to white text
            font.getData().setScale(scale);
            font.draw(batch, text, x, y);
        }
    }

    // ---- Sound Effects ----

    public void play(AudioSource sfx) {
        if (sfx == null) return;

        if (!activeSfx.contains(sfx)) {
            activeSfx.add(sfx);
        }

        float finalVolume = clamp(masterVolume * sfxVolume);
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

    // ---- Background Music ----

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

    // ---- Master Volume ----

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

        // --- NEW: Dispose of the font resource ---
        if (font != null) {
            font.dispose();
            font = null;
        }
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}