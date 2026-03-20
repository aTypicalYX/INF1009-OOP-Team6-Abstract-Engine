package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * WinScene – displayed when the player correctly answers all 5 equations.
 *
 * Design Patterns Used:
 * - State Pattern: WinScene is a distinct game state with its own
 *   render/update behaviour, cleanly separated from MathGameScene.
 * - Singleton Pattern (GameStateManager): reads the final score from the
 *   single shared GameStateManager instance without needing it injected
 *   via the constructor, demonstrating the Singleton's global accessibility.
 */
public class WinScene extends Scene {

    private final SceneManager scenes;

    /**
     * @param scenes SceneManager used to transition back to the main menu.
     */
    public WinScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        outputManager.stopBgm();

        // play win SFX once when this scene appears
        outputManager.play(new AudioSource("gameWin.wav"));
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // Reset Singleton state before starting a new session
            GameStateManager.getInstance().reset();
            scenes.setScene(new MainMenuScene(scenes));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Access Singleton directly – no need to pass score through constructor
        int finalScore = GameStateManager.getInstance().getScore();

        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();

        float cx = Gdx.graphics.getWidth() / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;

        outputManager.drawText(batch, "YOU WIN!",           cx - 130, cy + 80,  3.0f);
        outputManager.drawText(batch, "All 5 equations solved!", cx - 200, cy + 20, 1.8f);
        outputManager.drawText(batch, "FINAL SCORE: " + finalScore, cx - 140, cy - 40, 2.0f);
        outputManager.drawText(batch, "Press ENTER to Return to Menu", cx - 230, cy - 110, 1.5f);

        batch.end();
    }

    @Override
    public void dispose() { /* nothing to dispose */ }
}
