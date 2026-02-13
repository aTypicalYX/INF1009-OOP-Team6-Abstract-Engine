/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.github.team6.scenes;

import io.github.team6.managers.CollisionManager;
import io.github.team6.managers.EntityManager;
import io.github.team6.managers.InputManager;
import io.github.team6.managers.MovementManager;
import io.github.team6.managers.OutputManager;

public abstract class Scene {

    // Protected so subclasses (MainScene, SettingsScene) can use them
    protected InputManager inputManager;
    protected OutputManager outputManager;
    protected EntityManager entityManager;
    protected CollisionManager collisionManager;
    protected MovementManager movementManager;

    // Called automatically by SceneManager
    public void initialize(InputManager input, OutputManager output, EntityManager entity, CollisionManager collision, MovementManager movement) {
        this.inputManager = input;
        this.outputManager = output;
        this.entityManager = entity;
        this.collisionManager = collision;
        this.movementManager = movement;
        
        onEnter(); // Trigger the specific setup for this scene
    }

    // Abstract methods the specific scenes MUST implement
    public abstract void onEnter();      // Setup (Spawn entities)
    public abstract void update(float dt); // Logic (Win condition, spawning)
    public abstract void render();       // Draw (Calls entityManager.draw)
    public abstract void dispose();      // Cleanup

}
