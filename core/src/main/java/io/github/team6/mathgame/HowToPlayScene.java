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

import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * HowToPlayScene
 * * A static UI screen that explains the controls and objectives to the player.
 * Accessible from the MainMenuScene.
 */
public class HowToPlayScene extends Scene {

    private final SceneManager scenes;

    private Stage stage; 
    private Skin skin;   

    private int lastWidth = -1;
    private int lastHeight = -1;

    public HowToPlayScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);   

        lastWidth = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
        stage.getViewport().update(lastWidth, lastHeight, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Build the UI Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center().pad(20);

        // Title
        Label title = new Label("HOW TO PLAY", skin);
        title.setAlignment(Align.center);
        title.setFontScale(2.0f);
        table.add(title).padBottom(30).row();

        // Instructions
        String instructionsText = 
            "[CONTROLS]\n" +
            "Use the Arrow Keys or WASD to steer your rocket.\n\n" +
            "[OBJECTIVE]\n" +
            "A math equation will appear at the top of the screen.\n" +
            "Fly into the asteroid that has the CORRECT answer.\n\n" +
            "[DANGER]\n" +
            "Hitting a wrong answer costs you a life!\n" +
            "If you lose all your lives, the game is over!\n" +
            "Do NOT let the Black Hole swallow you from below! It is instant death!\n\n" +
            "[POWER-UPS]\n" +
            "Collect floating items for Extra Time, Extra Lives, and 2X Points!\n" +
            "\n" +
            "God Speed, Captain!\n";

        Label instructions = new Label(instructionsText, skin);
        instructions.setAlignment(Align.center);
        instructions.setFontScale(1.2f);
        // Allows the text to wrap cleanly if the window is small
        instructions.setWrap(true); 
        
        table.add(instructions).width(600).padBottom(40).row();

        // Back Button
        TextButton backBtn = new TextButton("Back to Menu", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Play the centralized UI click sound your team made!
                outputManager.playUiClick();
                scenes.setScene(new MainMenuScene(scenes));
            }
        });

        table.add(backBtn).width(280).height(60).row();

        stage.addActor(table);
    }

    @Override
    public void update(float dt) {
        stage.act(dt); 
    }

    @Override
    public void render(SpriteBatch batch) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            stage.getViewport().update(width, height, true);
        }

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}