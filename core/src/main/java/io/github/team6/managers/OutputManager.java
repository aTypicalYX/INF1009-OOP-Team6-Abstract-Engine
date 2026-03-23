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

    // shared UI click sound effect
    private AudioSource uiClickSfx;

    // volume controls (0.0f to 1.0f)
    private float masterVolume;
    private float sfxVolume;
    private float musicVolume;

    // NEW: Text Rendering Engine Component
    private BitmapFont font;

    /*
    OutputManager constructor initializes the audio system and the font engine.
    */
    public OutputManager() {
        activeSfx = new ArrayList<>();
        masterVolume = 1f;
        sfxVolume = 1.0f;
        musicVolume = 0.3f;  // softer bgm by default
        uiClickSfx = null;
        
        // Initialize the default font engine
        font = new BitmapFont(); 
    }

    /**
     * Draws white text to the screen using the provided SpriteBatch.
     * This abstracts the font scaling and color settings away from the Scene.
     */
    public void drawText(SpriteBatch batch, String text, float x, float y, float scale) {
        if (font != null && batch != null) {
            font.setColor(1, 1, 1, 1); // Default to white text
            font.getData().setScale(scale);
            font.draw(batch, text, x, y);
        }
    }

    /*
    Overloaded method to allow specifying text color, providing more flexibility for different UI elements or game states.
    Can be used for things like red damage numbers, green healing text, or any other color-coded information.
    */
    public void drawText(SpriteBatch batch, String text, float x, float y, float scale, com.badlogic.gdx.graphics.Color color) {
        if (font != null && batch != null) {
            font.setColor(color); 
            font.getData().setScale(scale);
            font.draw(batch, text, x, y);
        }
    }

    // ---- Sound Effects ----

    // Play a sound effect, ensuring it is tracked for volume control and cleanup
    public void play(AudioSource sfx) {
        if (sfx == null) return;

        if (!activeSfx.contains(sfx)) {
            activeSfx.add(sfx);
        }

        float finalVolume = clamp(masterVolume * sfxVolume);
        sfx.play(finalVolume);
    }

    // Stop all currently playing sound effects
    public void stopAllSfx() {
        for (AudioSource s : activeSfx) {
            s.stop();
        }
    }

    // Set the volume for sound effects
    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
    }

    public void playUiClick() {
        if (uiClickSfx == null) {
            try {
                uiClickSfx = new AudioSource("buttonClick.wav");
                uiClickSfx.setVolume(0.3f);
            } catch (Exception e) {
                return;
            }
        }

        play(uiClickSfx);
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

   // Dispose of all audio resources and the font engine when no longer needed
    public void dispose() {
        stopAllSfx();

        for (AudioSource s : activeSfx) {
            s.dispose();
        }

        activeSfx.clear();

        if (uiClickSfx != null) {
            uiClickSfx.dispose();
            uiClickSfx = null;
        }

        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
            bgm = null;
        }

        // Dispose of the font resource
        if (font != null) {
            font.dispose();
            font = null;
        }
    }

    // Utility method to ensure volume values are within the valid range of 0.0f to 1.0f
    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}