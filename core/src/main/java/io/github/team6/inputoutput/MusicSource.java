package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicSource {
    private final Music music;

    public MusicSource(String internalAssetPath) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal(internalAssetPath));
    }

    public void play(boolean looping) {
        music.setLooping(looping);
        music.play();
    }

    public void stop() {
        music.stop();
    }

    public boolean isPlaying() {
        return music.isPlaying();
    }

    public void setVolume(float volume01) {
        music.setVolume(clamp01(volume01));
    }

    public void dispose() {
        music.dispose();
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
