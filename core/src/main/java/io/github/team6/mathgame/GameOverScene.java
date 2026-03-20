package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * GameOverScene – displayed when the player runs out of lives.
 *
 * Design Patterns Used:
 * - State Pattern: a discrete game state with its own render / update logic.
 * - Singleton Pattern (GameStateManager): reads the final score directly
 *   from the Singleton, removing the need to pass it as a constructor arg.
 *   Also calls reset() so the next session starts with full lives and 0 score.
 */
public class GameOverScene extends Scene {

    private final SceneManager scenes;

    /**
     * Score is no longer passed via constructor – it is read from the
     * GameStateManager Singleton, keeping this class simpler and ensuring
     * it always shows the authoritative value.
     */
    public GameOverScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        outputManager.stopBgm();

        // play lose SFX once when this scene appears
        outputManager.play(new AudioSource("gameLose.wav"));
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // Reset Singleton so the next session starts fresh
            GameStateManager.getInstance().reset();
            scenes.setScene(new MainMenuScene(scenes));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Read authoritative score from the Singleton
        int finalScore = GameStateManager.getInstance().getScore();

        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();

        float cx = Gdx.graphics.getWidth()  / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;

        outputManager.drawText(batch, "GAME OVER",                    cx - 150, cy + 50,  3.0f);
        outputManager.drawText(batch, "FINAL SCORE: " + finalScore,   cx - 120, cy - 20,  2.0f);
        outputManager.drawText(batch, "Press ENTER to Return to Menu", cx - 220, cy - 100, 1.5f);

        batch.end();
    }

    @Override
    public void dispose() { /* nothing to dispose */ }
}
