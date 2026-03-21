package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.Scene;

/**
 * VictoryScene
 * Shown when the rocket reaches the top of the map (collides with the planet).
 * Displays the final score, the level completed, and two options:
 *   - View Leaderboard (+ save score if it qualifies)
 *   - Next Level       (loads Level 2 if available, else shows "Coming Soon")
 *
 * OOP Concepts:
 * - Inheritance    : Extends Scene.
 * - Singleton      : Reads final score/level from GameStateManager.
 * - State Pattern  : Discrete game state managed by SceneManager.
 * - Observer Pattern: Scene2D ChangeListeners on buttons.
 */
public class VictoryScene extends Scene {

    private final SceneManager scenes;
    private final int          levelCompleted;

    private Stage stage;
    private Skin  skin;
    private int   lastW = -1, lastH = -1;

    public VictoryScene(SceneManager scenes, int levelCompleted) {
        this.scenes         = scenes;
        this.levelCompleted = levelCompleted;
    }

    // -----------------------------------------------------------------------
    // Scene Lifecycle
    // -----------------------------------------------------------------------
    @Override
    public void onEnter() {
        outputManager.stopBgm();
        try {
            outputManager.play(new AudioSource("gameWin.wav"));
        } catch (Exception e) {
            System.out.println("[VictoryScene] gameWin.wav not found.");
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        lastW = Gdx.graphics.getWidth();
        lastH = Gdx.graphics.getHeight();
        stage.getViewport().update(lastW, lastH, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        buildUI();
    }

    @Override
    public void update(float dt) { stage.act(dt); }

    @Override
    public void render(SpriteBatch batch) {
        int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        if (w != lastW || h != lastH) {
            lastW = w; lastH = h;
            stage.getViewport().update(w, h, true);
        }
        Gdx.gl.glClearColor(0.03f, 0.06f, 0.12f, 1f); // dark space blue
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }

    // -----------------------------------------------------------------------
    // UI
    // -----------------------------------------------------------------------

    // Builds the victory screen UI with labels and buttons.
    private void buildUI() {
        GameStateManager gsm        = GameStateManager.getInstance();
        int              finalScore = gsm.getScore();

        Label titleLabel  = new Label("LEVEL " + levelCompleted + " COMPLETE!", skin);
        titleLabel.setFontScale(2.2f);
        titleLabel.setAlignment(Align.center);

        Label scoreLabel  = new Label("Score: " + finalScore, skin);
        scoreLabel.setFontScale(1.6f);
        scoreLabel.setAlignment(Align.center);

        Label subLabel = new Label("You escaped and reached the planet!", skin);
        subLabel.setAlignment(Align.center);

        // Check if score qualifies for leaderboard
        LeaderboardManager lb = new LeaderboardManager();
        String lbHint = lb.isHighScore(finalScore)
            ? "New high score! Enter your name on the leaderboard."
            : "View the leaderboard to see where you rank.";
        Label hintLabel = new Label(lbHint, skin);
        hintLabel.setAlignment(Align.center);
        hintLabel.setWrap(true);

        // Leaderboard button
        TextButton lbBtn = new TextButton("Leaderboard", skin);
        lbBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scenes.setScene(new LeaderboardScene(scenes, finalScore, levelCompleted));
            }
        });

        // Next level button
        boolean hasNextLevel = levelCompleted < 2;
        TextButton nextBtn = new TextButton(
            hasNextLevel ? "Next Level" : "Play Again", skin);
        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelCompleted < 2) {
                    // Show Level 2 cutscene before starting Level 2
                    scenes.setScene(new IntroScene(scenes, 2));
                } else {
                    GameStateManager.getInstance().reset();
                    scenes.setScene(new MathGameScene(scenes));
                }
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center().pad(20);

        table.add(titleLabel).colspan(2).padBottom(8).row();
        table.add(scoreLabel).colspan(2).padBottom(4).row();
        table.add(subLabel).colspan(2).padBottom(6).row();
        table.add(hintLabel).colspan(2).width(420).padBottom(24).row();
        table.defaults().width(220).height(58).pad(10);
        table.add(lbBtn);
        table.add(nextBtn);
        table.row();

        stage.addActor(table);
    }
}
