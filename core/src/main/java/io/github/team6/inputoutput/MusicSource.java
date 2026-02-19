package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;


/**
 * MusicSource:
 * wrapper around LibGDX Music for background tracks.
 * keeps music handling separate from the rest of the game logic.
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
