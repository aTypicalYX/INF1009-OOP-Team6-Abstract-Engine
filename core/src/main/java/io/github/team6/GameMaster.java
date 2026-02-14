
package io.github.team6;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.team6.managers.CollisionManager; 
import io.github.team6.managers.EntityManager;
import io.github.team6.managers.InputManager;
import io.github.team6.managers.MovementManager;
import io.github.team6.managers.OutputManager;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;

public class GameMaster extends ApplicationAdapter {

    // --- Global Managers ---
    private InputManager inputManager;
    private OutputManager outputManager;
    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;
    private SceneManager sceneManager;
    private SpriteBatch batch;

    // create() is called once when the application starts. This is used to set up the other Managers
    @Override
    public void create() {
        // Initialize Tools here so they can be used by any Scene.
        inputManager = new InputManager();
        outputManager = new OutputManager();
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();
        batch = new SpriteBatch();

        // Initialize SceneManager with tools. Pass the created managers into SceneManager.
        // This ensures SceneManager has access to all the systems it needs to pass down
        sceneManager = new SceneManager(inputManager, outputManager, entityManager, collisionManager, movementManager);

        // Start the Game (Pass control to MainScene). I.e in this case call MainScene()
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    // render() runs approximately 60 times per second.
    @Override
    public void render() {
        // Clear Screen
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Calculate Delta Time (dt)
        // dt is the time in seconds since the last frame. Used for smooth movement.
        float dt = Gdx.graphics.getDeltaTime();

        // Delegate updates and rendering to the active scene
        // GameMaster doesn't run game logic itself. It asks SceneManager to handle it.
        sceneManager.update(dt);   // Update math/positions
        sceneManager.render(batch);      // Draw images to screen
    }

    @Override
    public void dispose() {
        // Dispose global resources if any
        if (outputManager != null) outputManager.dispose();
        if (batch != null) batch.dispose();
    }
}