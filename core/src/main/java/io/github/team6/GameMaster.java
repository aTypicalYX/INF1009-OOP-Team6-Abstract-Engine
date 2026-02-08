package io.github.team6;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.team6.collision.CollisionManager;
import io.github.team6.entities.EntityManager;
import io.github.team6.input.InputManager;
import io.github.team6.movement.MovementManager;
import io.github.team6.output.OutputManager;
import io.github.team6.scenes.SceneManager;


public class GameMaster extends  ApplicationAdapter{

    private InputManager inputManager;
    private OutputManager outputManager;
    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;
    private SceneManager sceneManager;

    private SpriteBatch batch;
    private Texture image, image2;

    @Override
    public void create() {

        // Initialize all managers
        inputManager = new InputManager();
        outputManager = new OutputManager();
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();

        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        image2 = new Texture("droplet.png"); // added droplet test!
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.draw(image2, 100, 150);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
    
}
