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

/**
 * GameMaster is the entry point of the application. It initializes all the global managers and starts the first scene.
 * It delegates all game logic and rendering to the active scene via the SceneManager.
 */
public class GameMaster extends ApplicationAdapter {

    private InputManager     inputManager;
    private OutputManager    outputManager;
    private EntityManager    entityManager;
    private CollisionManager collisionManager;
    private MovementManager  movementManager;
    private SceneManager     sceneManager;
    private SpriteBatch      batch;
    // create() is called once when the application starts. This is used to set up the other Managers
    @Override
    public void create() {
        // Crash logger
        // Catches any unhandled exception on any thread and prints the
        // full stack trace so you can see exactly what caused the crash.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("=== CRASH on thread: " + thread.getName() + " ===");
            throwable.printStackTrace(System.err);
        });

        //  Maximise window on launch
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        inputManager     = new InputManager();
        outputManager    = new OutputManager();
        entityManager    = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager  = new MovementManager();
        batch            = new SpriteBatch();

        // Initialize SceneManager with tools. Pass the created managers into SceneManager.
        // This ensures SceneManager has access to all the systems it needs to pass down
        sceneManager = new SceneManager(inputManager, outputManager, entityManager, collisionManager, movementManager);

        // NOTE: To change back to the original MainMenuScene from Abstract Engine Part 1, just replace MathGameScene with MainMenuScene here.
        // Start the Game (Pass control to MainScene). I.e in this case call MainScene()
        
        // sceneManager.setScene(new MainMenuScene(sceneManager));
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    // render() runs approximately 60 times per second.
    @Override
    public void render() {
        try {
            ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
            float dt = Gdx.graphics.getDeltaTime();
            sceneManager.update(dt);
            sceneManager.render(batch);
        } catch (Exception e) {
            System.err.println("=== CRASH in render loop ===");
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void dispose() {
        // Dispose global resources if any to prevent memory leaks
        if (outputManager != null) outputManager.dispose();
        if (batch != null) batch.dispose();
    }
}