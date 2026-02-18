package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.OutputManager;


/**
 * Class: PlayableEntity
 * Represents the player-controlled character.
 * OOP Concept: Dependency Injection & Composition.
 * * This class demonstrates Constructor Injection, where external dependencies 
 * (OutputManager, AudioSource, Behaviors) are passed in rather than hardcoded.
 */
public class PlayableEntity extends Entity {
    private Texture tex;
    private OutputManager outputManager;
    private AudioSource collisionSound;

    // COMPOSITION: PlayableEntity "HAS A" CollisionBehavior.
    // This allows us to change how the player reacts to collisions dynamically.
    private CollisionBehavior collisionBehavior;

    // Constructors
    public PlayableEntity() {
        super();
    }
    
    /**
     * Parameterized Constructor.
     * Fully injects all dependencies.
     * Class does not rely on global state or hardcoded file paths.
     * @param outputManager Used to play sounds via the audio system.
     * @param behavior      Defines what happens when the player is hit.
     */
    public PlayableEntity(String texturePath, String soundPath, OutputManager outputManager, CollisionBehavior behavior, float x, float y, float speed, float width, float height, String tag) {
        super(x, y, speed, width, height, tag);
        this.tex = new Texture(Gdx.files.internal(texturePath));
        this.outputManager = outputManager;
        this.collisionBehavior = behavior; // Assign the injected behavior

        // Load sound dynamically if path is provided
        if (soundPath != null && outputManager != null) {
            try {
                this.collisionSound = new AudioSource(soundPath);
                this.collisionSound.setVolume(0.1f);
            } catch (Exception e) {
                System.out.println("[WARNING] Could not load sound: " + soundPath);
            }
        }
    }

    // getter
    // public Texture getTexture() { return tex; }

    //setter
    // public void setTexture(Texture tex) { this.tex = tex; }

    // --- Implementing Abstract Methods from Entity/Interfaces ---

    @Override
    public void draw(SpriteBatch batch) {
        // Draw the texture at the entity's current X and Y coordinates
        batch.draw(tex, getX(), getY());
    }


    /**
     * movement()
     * Intentionally empty for PlayableEntity.
     * Reason: The player is controlled by the InputManager (Keyboard), not by an automated algorithm.
     * This method exists to satisfy the 'Movable' interface contract.
     */
    @Override
    public void movement() {
    }

    /**
     * onCollision()
     * Triggered by CollisionManager.
     * 1. Plays a sound effect (Audio Feedback).
     * 2. Delegates the logic response to collisionBehavior (Strategy Pattern).
     */
    @Override
    public void onCollision(Entity other) {
        // Play collision sound
        if (outputManager != null && collisionSound != null) {
            outputManager.play(collisionSound);
        }
        
        // Delegate Logic
        if (collisionBehavior != null) {
            collisionBehavior.onCollision(this, other);
        }
    }
    
    // Helper method to set OutputManager if not done via constructor
    public void setOutputManager(OutputManager outputManager) {
        this.outputManager = outputManager;
        try {
            collisionSound = new AudioSource("collision.wav");
            collisionSound.setVolume(0.1f); 
            System.out.println("[DEBUG] collision.wav loaded at 10% volume");
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: collision.wav not found.");
            e.printStackTrace();
            collisionSound = null;
        }
    }
    
}

