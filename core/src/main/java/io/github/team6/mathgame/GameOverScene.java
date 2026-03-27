package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

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

    private Stage stage;
    private Stage bgStage;
    private Skin  skin;
 
    private Texture logoTexture;
    private Image logoImage;
    private Texture bgTexture;
    private Image bgImage;

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

        //  BACKGROUND SETUP 
        bgStage = new Stage(new ScreenViewport());
        bgTexture = new Texture(Gdx.files.internal("defeat_background.png"));
        bgImage = new Image(bgTexture);

        // Set size and position
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        bgImage.setPosition(0,0);
        bgImage.setOrigin(Align.center);

        // Zoom animation adjustments
        bgImage.addAction(Actions.scaleTo(1.1f, 1.1f, 20f, Interpolation.exp5Out));
        
        // Adds zooming action
        bgStage.addActor(bgImage);

        //  LOGO SETUP 
        logoTexture = new Texture(Gdx.files.internal("mission_failed_logo.png"));
        logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        logoImage = new Image(logoTexture);

        //  AUDIO SETUP 
        outputManager.stopBgm();

        // play lose SFX once when this scene appears
        try {
            AudioSource loseSfx = new AudioSource("gameLose.wav");
            loseSfx.setVolume(0.2f);
            outputManager.play(loseSfx);
        } catch (Exception e) {
            System.out.println("[DEBUG] gameLose.wav not found.");
        }

        //  UI SETUP 
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        buildUI();
    }

    @Override
    public void update(float dt) {
        bgStage.act(dt);
        stage.act(dt);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            try {
                AudioSource clickSfx = new AudioSource("buttonClick.wav");
                clickSfx.setVolume(0.3f);
                outputManager.play(clickSfx);
            } catch (Exception e) {
                System.out.println("[DEBUG] buttonClick.wav not found.");
            }

            // Reset Singleton so the next session starts fresh
            GameStateManager.getInstance().reset();
            scenes.setScene(new MainMenuScene(scenes));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0.05f, 0.02f, 0.02f, 1f); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        bgStage.draw();
        stage.draw();
    }

    @Override
    public void dispose() { 
        if (stage != null) stage.dispose();
        if (bgStage != null) bgStage.dispose();
        if (skin != null) skin.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
    }

    // UI Construction

    // Builds the defeat screen UI with labels and buttons.
    private void buildUI() {
        // Authoritative score from Singleton
        int finalScore = GameStateManager.getInstance().getScore();

        // Logo Sizing
        float screenWidth = Gdx.graphics.getWidth();
        float logoWidth = screenWidth * 0.50f;
        float aspect = (float) logoTexture.getHeight() / logoTexture.getWidth();
        float logoHeight = logoWidth * aspect;

        logoImage.getColor().a = 0;
        logoImage.addAction(Actions.fadeIn(1.5f));
       
        //  TEXT SETUP 
        Label scoreLabel = new Label("Final Score: " + finalScore, skin);
        scoreLabel.setFontScale(1.7f);

        Label enterLabel = new Label("Press ENTER to Return to Menu", skin);
        enterLabel.setFontScale(1.3f);
        
        // Adds blinking animation
        enterLabel.addAction(Actions.forever(Actions.sequence(
        Actions.fadeOut(0.8f),
        Actions.fadeIn(0.8f)
    )));

        //  TABLE LAYOUT 
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(logoImage).size(logoWidth, logoHeight).padBottom(25).row();
        table.add(scoreLabel).padBottom(35).row();
        table.add(enterLabel);

        stage.addActor(table);
    }

    
}
