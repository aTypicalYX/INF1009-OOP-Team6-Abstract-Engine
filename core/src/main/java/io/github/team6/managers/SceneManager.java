

package io.github.team6.managers;

import io.github.team6.scenes.Scene;


public class SceneManager {
    // Create new child class activeScene from abstract class Scene in Scene.java
    private Scene activeScene;

    // References to the global managers
    private InputManager inputManager;
    private OutputManager outputManager;
    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;

    // Constructor: Receives tools from GameMaster
    public SceneManager(InputManager input, OutputManager output, EntityManager entity, CollisionManager collision, MovementManager movement) {
        this.inputManager = input;
        this.outputManager = output;
        this.entityManager = entity;
        this.collisionManager = collision;
        this.movementManager = movement;
    }

    public void setScene(Scene newScene) {
        
    }

    public void update() {
        
    }

    public void render() {
        
    }


}
