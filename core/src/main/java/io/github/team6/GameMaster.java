package io.github.team6;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.team6.managers.CollisionManager;
import io.github.team6.managers.EntityManager;
import io.github.team6.managers.InputManager;
import io.github.team6.managers.MovementManager;
import io.github.team6.managers.OutputManager;
import io.github.team6.managers.SceneManager;



public class GameMaster extends ApplicationAdapter{

    // Declare Manager variables
    private InputManager inputManager;
    private OutputManager outputManager;
    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;
    private SceneManager sceneManager;

    private boolean running;

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

        running = true;
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
