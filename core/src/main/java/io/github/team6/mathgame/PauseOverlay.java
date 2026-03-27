package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.managers.OutputManager;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;

/**
 * PauseOverlay
 * Rendered ON TOP of MathGameScene when the player presses P or ESC.
 * NOT a Scene - it is owned by MathGameScene so the game world stays visible behind it.
 *
 * Contains three actions:
 *   Resume   - unpauses and returns input to the game
 *   Settings - volume slider (inline, no scene transition needed)
 *   Quit     - returns to MainMenuScene
 *
 * OOP Concepts:
 * - Composition     : Owned by MathGameScene ("has-a").
 * - Encapsulation   : pause state and all UI are private.
 * - Single Responsibility: Only handles pause overlay rendering.
 * - Observer Pattern: Scene2D ChangeListeners on buttons and slider.
 */
public class PauseOverlay {

    private static final float DIM_ALPHA = 0.60f;

    private final SceneManager   scenes;
    private final OutputManager  outputManager;

    private boolean paused = false;
    private com.badlogic.gdx.InputProcessor fallbackProcessor = null;

    private final ShapeRenderer shapeRenderer;
    private Stage  stage;
    private Skin   skin;

    private int lastW = -1, lastH = -1;

    public PauseOverlay(SceneManager scenes, OutputManager outputManager) {
        this.scenes        = scenes;
        this.outputManager = outputManager;
        shapeRenderer      = new ShapeRenderer();
        buildUI();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Toggles pause state and swaps the input processor. */
    public void toggle() {
        paused = !paused;
        if (paused) {
            Gdx.input.setInputProcessor(stage);
        } else {
            // Restore the caller's input processor if one was registered
            if (fallbackProcessor != null) {
                Gdx.input.setInputProcessor(fallbackProcessor);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }
    }

    /**
     * Registers an input processor to restore when the player unpauses.
     * Call this after building the scene's Stage so button clicks keep working.
     * @param processor The Stage or InputProcessor to restore on resume.
     */
    public void setFallbackProcessor(com.badlogic.gdx.InputProcessor processor) {
        this.fallbackProcessor = processor;
    }

    public boolean isPaused() { return paused; }

    /**
     * Draws the dim layer and pause UI on top of the game world.
     * Call at the END of MathGameScene.render(), after all world drawing.
     * Is a no-op when not paused.
     */
    public void render(SpriteBatch batch) {
        if (!paused) return;

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        if (w != lastW || h != lastH) {
            lastW = w; lastH = h;
            stage.getViewport().update(w, h, true);
        }

        // Flush any open SpriteBatch before ShapeRenderer takes over
        if (batch.isDrawing()) batch.end();

        // Semi-transparent dim layer
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, DIM_ALPHA));
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Pause UI
        stage.act();
        stage.draw();
    }

    /** Release resources. Call from MathGameScene.dispose(). */
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }

    // -----------------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------------

    private void buildUI() {
        stage = new Stage(new ScreenViewport());
        skin  = new Skin(Gdx.files.internal("uiskin.json"));

        lastW = Gdx.graphics.getWidth();
        lastH = Gdx.graphics.getHeight();
        stage.getViewport().update(lastW, lastH, true);

        // Title
        Label title = new Label("PAUSED", skin);
        title.setFontScale(2.5f);
        title.setAlignment(Align.center);

        // --- Volume slider (inline settings) ---
        Label volLabel = new Label("Master Volume: "
            + (int)(outputManager.getMasterVolume() * 100) + "%", skin);
        volLabel.setAlignment(Align.center);

        Slider volSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volSlider.setValue(outputManager.getMasterVolume());
        volSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.setMasterVolume(volSlider.getValue());
                volLabel.setText("Master Volume: "
                    + (int)(outputManager.getMasterVolume() * 100) + "%");
            }
        });

        // --- Buttons ---
        TextButton resumeBtn = new TextButton("Resume",    skin);
        TextButton quitBtn   = new TextButton("Main Menu", skin);

        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggle(); // unpauses, restores input
            }
        });

        // Quit button resets game state and returns to main menu scene
        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameStateManager.getInstance().reset();
                scenes.setScene(new MainMenuScene(scenes));
            }
        });

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(title).padBottom(24).row();
        table.add(volLabel).padBottom(4).row();
        table.add(volSlider).width(300).padBottom(20).row();
        table.defaults().width(260).height(58).pad(8);
        table.add(resumeBtn).row();
        table.add(quitBtn).row();

        stage.addActor(table);
    }
}
