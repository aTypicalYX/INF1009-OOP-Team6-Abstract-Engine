package io.github.team6.scenes;

import com.badlogic.gdx.Gdx;
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

/*
SettingsScene is a simple UI scene that allows the player to adjust game settings, such as master volume.
It is launched from the main menu and has a "Back" button to return to the main menu. The master volume slider updates the OutputManager's master volume in real-time, and the current percentage is displayed in a label above the slider.
OOP Concepts:
- Inheritance: Extends Scene, so it follows the same lifecycle (onEnter, update, render, dispose).
- Composition: Owns a Stage and Skin for UI management.
- Observer Pattern: Uses ChangeListeners to react to UI interactions (slider changes and button clicks).
*/
public class SettingsScene extends Scene {
    // Reference to SceneManager (used for scene switching)
    private final SceneManager scenes;
    // Scene2D UI components
    private Stage stage; // To handle the UI input + rendering
    private Skin skin;  // Styles UI elements

    // Constructor receives SceneManager so this scene can change scenes
    public SettingsScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    // --- NEW: Tell GameMaster to draw the global scrolling space background ---
    @Override
    public boolean isBackgroundVisible() {
        return true;
    }

    // Tells GameMaster to play background SFX
    @Override 
    public boolean isAmbientAudioEnabled() {
        return true;
    }
    // ------------------------------------------------------------------------

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
        title.setFontScale(1.8f); // Slightly larger title for visual hierarchy
        
        // Master Volume Controls
        final Label masterLabel = new Label("Master Volume: " + (int)(outputManager.getMasterVolume() * 100) + "%", skin);
        masterLabel.setAlignment(Align.center);
        
        final Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(outputManager.getMasterVolume());

        // --- Quick Preset Buttons ---
        TextButton muteBtn = new TextButton("Mute", skin);
        TextButton halfBtn = new TextButton("50%", skin);
        TextButton fullBtn = new TextButton("100%", skin);

        // Preset Listeners (Observer Pattern) - These update the slider, which in turn updates the volume
        muteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                masterSlider.setValue(0f);
            }
        });
        halfBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                masterSlider.setValue(0.5f);
            }
        });
        fullBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                masterSlider.setValue(1f);
            }
        });

        // Group the preset buttons into their own mini-table horizontally
        Table presetTable = new Table();
        presetTable.add(muteBtn).width(90).height(40).padRight(10);
        presetTable.add(halfBtn).width(90).height(40).padRight(10);
        presetTable.add(fullBtn).width(90).height(40);
        // ----------------------------------

        TextButton backBtn = new TextButton("Back to Menu", skin);

        // SLIDER LISTENER
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.setMasterVolume(masterSlider.getValue());  // Update global volume value
                masterLabel.setText("Master Volume: " + (int)(outputManager.getMasterVolume() * 100) + "%"); 
            }
        });

        // BACK BUTTON LISTENER
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                scenes.setScene(new MainMenuScene(scenes));
            }
        });

        // Main Layout Table
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        // Add UI elements vertically
        table.add(title).padBottom(40).row();
        table.add(masterLabel).padBottom(10).row();
        table.add(masterSlider).width(300).padBottom(15).row();
        table.add(presetTable).padBottom(40).row(); // Insert the new preset buttons
        table.add(backBtn).width(280).height(60).row();
        
        stage.addActor(table); 
    }

    @Override
    public void update(float dt) { 
        stage.act(dt); // Update UI logic and animations of the stage
    }

    @Override
    public void render(SpriteBatch batch) {
        stage.draw(); // Draw all UI actors on top of the GameMaster's background
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}