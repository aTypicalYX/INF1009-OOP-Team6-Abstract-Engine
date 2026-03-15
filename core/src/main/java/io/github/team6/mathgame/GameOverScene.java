package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * GameOverScene represents the end-state of the game.
 * Displays the final score and waits for user input to restart or exit.
 */
public class GameOverScene extends Scene {

    private final SceneManager scenes;
    private final int finalScore;

    public GameOverScene(SceneManager scenes, int finalScore) {
        this.scenes = scenes;
        this.finalScore = finalScore;
    }

    @Override
    public void onEnter() {
        // Stop the background music when the player loses
        outputManager.stopBgm();
    }

    @Override
    public void update(float dt) {
        // Wait for the player to press ENTER to go back to the main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            scenes.setScene(new MainMenuScene(scenes));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Set projection to draw UI directly to the screen coordinates
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        
        batch.begin();
        
        // Render the Game Over text using the abstract engine's OutputManager
        outputManager.drawText(batch, "GAME OVER", Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f + 50, 3.0f);
        outputManager.drawText(batch, "FINAL SCORE: " + finalScore, Gdx.graphics.getWidth() / 2f - 120, Gdx.graphics.getHeight() / 2f - 20, 2.0f);
        outputManager.drawText(batch, "Press ENTER to Return to Menu", Gdx.graphics.getWidth() / 2f - 220, Gdx.graphics.getHeight() / 2f - 100, 1.5f);
        
        batch.end();
    }

    @Override
    public void dispose() {
        // Nothing to dispose here, fonts are managed by OutputManager
    }
}