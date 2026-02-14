package io.github.team6.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.scenes.Scene;


/**
 * SceneManager 
 * 1. It holds the "State" of the game (which scene is active?).
 * 2. It holds the "Tools" (Managers) passed down from GameMaster.
 * 3. It handles the transition logic (Closing old scene, opening new one).
 */
public class SceneManager {
    private Scene activeScene;

    // References to the global managers
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final MovementManager movementManager;

    // SceneManager Constructor: Receives tools from GameMaster via Dependency Injection
    public SceneManager(InputManager input, OutputManager output, EntityManager entity, CollisionManager collision, MovementManager movement) {
        this.inputManager = input;
        this.outputManager = output;
        this.entityManager = entity;
        this.collisionManager = collision;
        this.movementManager = movement;
    }

    // setScene method to delete old scene, and then create a new scene
    public void setScene(Scene newScene) {
        // Cleanup old scene
        if (activeScene != null) {
            activeScene.dispose();
        }
        
        // Set the pointer to the new scene
        activeScene = newScene;
        
        // Inject Dependencies for the new scene
        if (activeScene != null) {
            activeScene.initialize(inputManager, outputManager, entityManager, collisionManager, movementManager);
        }
    }

    // Delegates the update loop to the currently active scene
    public void update(float dt) {
        if (activeScene != null) {
            activeScene.update(dt);
        }
    }

    // Delegates the render loop to the currently active scene
    public void render(SpriteBatch batch) {
        if (activeScene != null) {
            activeScene.render(batch);
        }
    }
}