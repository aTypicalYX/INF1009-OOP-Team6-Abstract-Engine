package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;


/**
* Class: MusicSource
 * A wrapper around the LibGDX Music API, for streaming large background tracks.
 * * OOP Concepts & Design Patterns:
 * - Single Responsibility Principle: By splitting AudioSource (short loaded sounds) and 
 * MusicSource (long streamed music) into two different classes, the engine respects how the underlying hardware handles audio memory differently.
 * - Encapsulation: Hides the LibGDX framework implementation details. It enforces safe volume boundaries using clamp01() and exposes only necessary playback controls (play, stop, setVolume) to the OutputManager.
 */
public class MusicSource {
    private final Music music;

    public MusicSource(String internalAssetPath) {
        // load music from internal assets
        this.music = Gdx.audio.newMusic(Gdx.files.internal(internalAssetPath));
    }

    /**
     * start playback, looping can be enabled depending on the scene's needs.
     */
    public void play(boolean looping) {
        music.setLooping(looping);
        music.play();
    }

    public void stop() {
        music.stop();
    }
    // checking if currently playing
    public boolean isPlaying() {
        return music.isPlaying();
    }

    // set volume (clamped between 0.0 and 1.0)
    public void setVolume(float volume01) {
        music.setVolume(clamp01(volume01));
    }

    public void dispose() {
        music.dispose();
    }

    // ensure volume stays within valid range
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
