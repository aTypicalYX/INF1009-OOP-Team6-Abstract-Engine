package io.github.team6.scenes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.managers.CollisionManager;
import io.github.team6.managers.EntityManager;
import io.github.team6.managers.InputManager;
import io.github.team6.managers.MovementManager;
import io.github.team6.managers.OutputManager;

public abstract class Scene {

    // Protected Access: Allows subclasses (MainScene, MainMenuScene) to access these managers directly.
    protected InputManager inputManager;
    protected OutputManager outputManager;
    protected EntityManager entityManager;
    protected CollisionManager collisionManager;
    protected MovementManager movementManager;

    //Injects the single instances of the managers so state is preserved across scenes.

    public void initialize(InputManager input, OutputManager output, EntityManager entity, CollisionManager collision, MovementManager movement) {
        this.inputManager = input;
        this.outputManager = output;
        this.entityManager = entity;
        this.collisionManager = collision;
        this.movementManager = movement;
        
        onEnter(); // Trigger the specific setup for this scene. I.e triggers the MainScene to load the bucket
    }

    protected void updateCamera(OrthographicCamera camera, float centerX, float centerY, float mapPixelWidth,
            float mapPixelHeight) {

        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;

        float clampedX = Math.max(halfW, Math.min(centerX, mapPixelWidth - halfW));
        float clampedY = Math.max(halfH, Math.min(centerY, mapPixelHeight - halfH));

        camera.position.set(clampedX, clampedY, 0);
        camera.update();
    }

    // Abstract methods the specific scenes MUST implement

    public abstract void onEnter();      // Called once when the scene starts (load assets, spawn entities)
    public abstract void update(float dt); // Called every frame (handle logic, win conditions)

    public abstract void dispose();       // Called when leaving the scene (unload assets)
    public abstract void render(SpriteBatch batch); // Called every frame (draw images)

    public void postUpdate(float dt) { // Optional lifecycle hook for scene-specific post-update logic
    }

}
