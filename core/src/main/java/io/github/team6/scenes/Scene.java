package io.github.team6.scenes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.managers.CollisionManager;
import io.github.team6.managers.EntityManager;
import io.github.team6.managers.InputManager;
import io.github.team6.managers.MovementManager;
import io.github.team6.managers.OutputManager;

// Scene is an abstract class, used to define the Blueprint for what a game level must be.
// It holds the references to the Managers so that child classes can access them without having to create them.

public abstract class Scene {

    // Protected so subclasses can use them
    protected InputManager inputManager;
    protected OutputManager outputManager;
    protected EntityManager entityManager;
    protected CollisionManager collisionManager;
    protected MovementManager movementManager;

    // Called automatically by SceneManager
    // SceneManager calls this method to inject the global managers into this scene
    public void initialize(InputManager input, OutputManager output, EntityManager entity, CollisionManager collision, MovementManager movement) {
        this.inputManager = input;
        this.outputManager = output;
        this.entityManager = entity;
        this.collisionManager = collision;
        this.movementManager = movement;
        
        onEnter(); // Trigger the specific setup for this scene. I.e triggers the MainScene to load the bucket
    }

    // Abstract methods the specific scenes MUST implement
    public abstract void onEnter();      // Setup (Spawn entities)
    public abstract void update(float dt); // Logic (Win condition, spawning)
    //public abstract void render();       // Draw (Calls entityManager.draw)
    public abstract void dispose();      // Cleanup
    public abstract void render(SpriteBatch batch);

}
