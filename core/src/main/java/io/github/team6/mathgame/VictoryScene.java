package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private final int levelCompleted;

    private Stage stage;
    private Stage bgStage;
    private Skin  skin;
    
    private Texture logoTexture;
    private Image logoImage;
    private Texture bgTexture;
    private Image bgImage;

    public VictoryScene(SceneManager scenes, int levelCompleted) {
        this.scenes         = scenes;
        this.levelCompleted = levelCompleted;
    }

    // -----------------------------------------------------------------------
    // Scene Lifecycle
    // -----------------------------------------------------------------------
    @Override
    public void onEnter() {
        
        // ---------- BACKGROUND SETUP ----------
        bgStage = new Stage(new ScreenViewport());
        bgTexture = new Texture(Gdx.files.internal("victory_background.png"));
        bgImage = new Image(bgTexture);

        // Set size and position
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        bgImage.setPosition(0,0);
        bgImage.setOrigin(Align.center);

        // Zoom animation adjustments
        bgImage.addAction(Actions.scaleTo(1.1f, 1.1f, 20f, Interpolation.exp5Out));
        
        // Adds zooming action
        bgStage.addActor(bgImage);

        // ---------- LOGO SETUP ----------
        logoTexture = new Texture(Gdx.files.internal("mission_success_logo.png"));
        logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        logoImage = new Image(logoTexture);

        // ---------- AUDIO SETUP ----------
        outputManager.stopBgm();
        try {
            outputManager.play(new AudioSource("gameWin.wav"));
        } catch (Exception e) {
            System.out.println("[VictoryScene] gameWin.wav not found.");
        }

        // ---------- UI SETUP ----------
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        buildUI();
    }

    @Override 
    public void update(float dt) {
        bgStage.act(dt);
        stage.act(dt);
    }
    
    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0.03f, 0.06f, 0.12f, 1f); // dark space blue
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        bgStage.draw();
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (bgStage != null) bgStage.dispose();
        if (logoTexture != null) logoTexture.dispose();
    }

    // -----------------------------------------------------------------------
    // UI
    // -----------------------------------------------------------------------

    // Builds the victory screen UI with labels and buttons.
    private void buildUI() {
        GameStateManager gsm        = GameStateManager.getInstance();
        int              finalScore = gsm.getScore();

        // Proportionally scales logo to desired width
        float screenWidth = Gdx.graphics.getWidth();
        float logoWidth = screenWidth * 0.50f;      // Adjusts logo size
        float aspect = (float) logoTexture.getHeight() / logoTexture.getWidth();
        float logoHeight = logoWidth * aspect;

        logoImage.getColor().a = 0;
        logoImage.addAction(Actions.fadeIn(1.5f));
       
        // ---------- TEXT ADJUSTMENTS ----------
        Label titleLabel  = new Label("Stage " + levelCompleted + " Secured", skin);
        titleLabel.setFontScale(2.3f);
        titleLabel.setColor(com.badlogic.gdx.graphics.Color.CYAN);

        Label scoreLabel  = new Label("Score: " + finalScore, skin);
        scoreLabel.setFontScale(1.7f);

        // Check if score qualifies for leaderboard
        LeaderboardManager lb = new LeaderboardManager();
        String lbHint = lb.isHighScore(finalScore)
            ? "NEW HIGH SCORE!"
            : "Mission logs archived. \nCheck the leaderboard.";
        
        Label hintLabel = new Label(lbHint, skin);
        hintLabel.setFontScale(1.4f);
        hintLabel.setAlignment(Align.center);
        hintLabel.setWrap(true);
        hintLabel.setColor(com.badlogic.gdx.graphics.Color.GOLD);

        // Leaderboard button
        TextButton lbBtn = new TextButton("Leaderboard", skin);
        lbBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scenes.setScene(new LeaderboardScene(scenes, finalScore, levelCompleted));
            }
        });

        // Next level button
        boolean hasNextLevel = LevelConfig.hasNextLevel(levelCompleted);
        TextButton nextBtn = new TextButton(
            hasNextLevel ? "Next Level" : "Play Again", skin);
        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (LevelConfig.hasNextLevel(levelCompleted)) {
                    // Show next level's cutscene before starting it
                    scenes.setScene(new IntroScene(scenes, levelCompleted + 1));
                } else {
                    GameStateManager.getInstance().reset();
                    scenes.setScene(new MathGameScene(scenes));
                }
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center().pad(20);

        table.add(logoImage).width(logoWidth).height(logoHeight).colspan(2).padBottom(20).row();
        table.add(titleLabel).colspan(2).padBottom(4).row();
        table.add(scoreLabel).colspan(2).padBottom(2).row();
        table.add(hintLabel).colspan(2).width(420).padBottom(14).row();
        
        
        table.defaults().width(220).height(58).pad(10);
        table.add(lbBtn);
        table.add(nextBtn);
        table.row();

        stage.addActor(table);
    }
}
