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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.managers.SceneManager;


public class GameOverScene extends Scene {

    private final SceneManager scenes;
    private Stage stage;
    private Skin skin;

    public GameOverScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        
    }

    

    @Override
    public void update(float dt) {
        stage.act(dt);
    }

    public void render(SpriteBatch batch) {
        // Reddish background for dramatic effect
        Gdx.gl.glClearColor(0.2f, 0.05f, 0.05f, 1f); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();
    }
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
    
    
    
}
