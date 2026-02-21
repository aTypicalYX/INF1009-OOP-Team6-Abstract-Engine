package io.github.team6.scenes;

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


/**
/**
 * Class: MainMenuScene
 * Handles the user interface for the Start Screen.
 * Uses LibGDX Scene2D library (Stage, Actors, Tables) for UI layout.
 * * OOP Concepts & Design Patterns:
 * - Inheritance: Extends the abstract Scene.java class, automatically inheriting references to all global managers without needing to re-declare them.
 * - Observer Pattern: Utilizes ChangeListener on UI buttons to trigger asynchronous events (like scene transitions) 
 * without the need for continuous polling in the update() loop.
 * - Encapsulation: Manages its own UI Stage and viewport scaling, keeping presentation logic completely isolated from the core game logic.
 */
public class MainMenuScene extends Scene {

    private final SceneManager scenes;

    private Stage stage; // Scene2D container for UI elements
    private Skin skin;   // JSON style definitions for buttons/fonts

    // Track window size so we can update the Stage viewport on resize (e.g. maximize)
    private int lastWidth = -1;
    private int lastHeight = -1;

    public MainMenuScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        // Stop any background music from previous scene
        outputManager.stopBgm();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);   // Divert input from game logic to UI logic

        // Ensure viewport matches the current window size at entry
        lastWidth = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
        stage.getViewport().update(lastWidth, lastHeight, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // UI Element Creation
        Label title = new Label("Team 06 OOP Part 1", skin);
        title.setAlignment(Align.center);
        title.setFontScale(2f);

        TextButton startBtn = new TextButton("Start Game", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton exitBtn = new TextButton("Exit", skin);

        // Event Listener (Observer Pattern): React to button clicks
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                // Switch State: Load the Main Game
                scenes.setScene(new MainScene(scenes));
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                // Switch State: Load Settings
                scenes.setScene(new SettingsScene(scenes));
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        //Configure Menu positions
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(title).padBottom(30).row();

        table.defaults().width(260).height(60).pad(10);
        table.add(startBtn).row();
        table.add(settingsBtn).row();
        table.add(exitBtn).row();

        stage.addActor(table);

        System.out.println("=== MAIN MENU ===");
    }

    @Override
    public void update(float dt) {
        stage.act(dt);  // Update UI animations
    }

    @Override
    public void render(SpriteBatch batch) {
        // If window size changed (maximize / resize), update Stage viewport
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            stage.getViewport().update(width, height, true);
        }

        // background colour
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render the UI
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
