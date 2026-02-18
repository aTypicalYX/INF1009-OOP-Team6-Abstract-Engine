package io.github.team6.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

import io.github.team6.managers.SceneManager;


/**
 * Class: SettingsScene
 * Allows modification of global game settings (e.g., Volume).
 * Logic: Interacts with the global OutputManager to persist changes.
 */
public class SettingsScene extends Scene {
    // Reference to SceneManager (used for scene switching)
    private final SceneManager scenes;
    // Scene2D UI components
    private Stage stage; // To handle the UI input + rendering
    private Skin skin;  // Styles UI elements

    // simple setting (for demo)
    private boolean testToggle = false;

    // Constructor receives SceneManager so this scene can change scenes
    public SettingsScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        // Create Stage for UI and set it as active input processor
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // Load default UI skin (buttons, fonts, sliders, etc.)
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        // Title label
        Label title = new Label("Settings", skin);
        title.setAlignment(Align.center);
        title.setFontScale(1.2f);
        
        // Various buttons and sliders for settings
        TextButton toggleDebugBtn = new TextButton(testButtontxt(), skin);
        TextButton backBtn = new TextButton("Back", skin);
        Label masterLabel = new Label("Master Volume: " + (int)(outputManager.getMasterVolume() * 100) + "%", skin);
        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(outputManager.getMasterVolume());

        // BUTTON LISTENERS
        toggleDebugBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleDebugBtn.setText(testButtontxt());
                System.out.println("TestButtonToggle = " + testToggle);
            }
        });
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scenes.setScene(new MainMenuScene(scenes));
            }
        });
        // Volume slider updates master volume in OutputManager
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.setMasterVolume(masterSlider.getValue());  // Update global volume value
                System.out.println("Master = " + outputManager.getMasterVolume()); // Print for debugging
                masterLabel.setText("Master Volume: " + (int)(outputManager.getMasterVolume() * 100) + "%"); // Update UI label to reflect new percentage
            }
        });

        // Table-based UI layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.defaults().width(300).height(60).pad(10);
        // Add UI elements vertically
        table.add(title).padBottom(25).row();
        table.add(toggleDebugBtn).row();
        table.add(backBtn).row();
        table.add(masterLabel).row();
        table.add(masterSlider).width(320).row();
        stage.addActor(table); // Add table to stage

        System.out.println("=== SETTINGS ===");
    }

    private String testButtontxt() {
        return "TestButtonToggle: " + (testToggle ? "ON" : "OFF"); // Helper method to format toggle button text
    }

    @Override
    public void update(float dt) { 
        stage.act(dt); // Update UI logic and animations of the stage
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0.10f, 0.10f, 0.14f, 1f); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw(); // Draw all UI actors
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
