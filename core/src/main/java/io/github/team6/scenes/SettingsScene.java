package io.github.team6.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.managers.SceneManager;

public class SettingsScene extends Scene {

    private final SceneManager scenes;

    private Stage stage;
    private Skin skin;

    // simple setting (for demo)
    private boolean testToggle = false;

    public SettingsScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Label title = new Label("Settings", skin);
        title.setAlignment(Align.center);
        title.setFontScale(1.2f);

        TextButton toggleDebugBtn = new TextButton(testButtontxt(), skin);
        TextButton backBtn = new TextButton("Back", skin);

        toggleDebugBtn.addListener(e -> {
            if (toggleDebugBtn.isPressed()) {
                testToggle = !testToggle;
                toggleDebugBtn.setText(testButtontxt());
                System.out.println("TestButtonToggle = " + testToggle);
                return true;
            }
            return false;
        });

        backBtn.addListener(e -> {
            if (backBtn.isPressed()) {
                scenes.setScene(new MainMenuScene(scenes));
                return true;
            }
            return false;
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.defaults().width(300).height(60).pad(10);

        table.add(title).padBottom(25).row();
        table.add(toggleDebugBtn).row();
        table.add(backBtn).row();

        stage.addActor(table);

        System.out.println("=== SETTINGS ===");
    }

    private String testButtontxt() {
        return "TestButtonToggle: " + (testToggle ? "ON" : "OFF");
    }

    @Override
    public void update(float dt) {
        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0.10f, 0.10f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
