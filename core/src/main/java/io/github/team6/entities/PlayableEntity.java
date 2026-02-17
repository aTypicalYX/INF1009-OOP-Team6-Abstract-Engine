package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.OutputManager;

public class PlayableEntity extends Entity {
    private Texture tex;
    private OutputManager outputManager;
    private AudioSource collisionSound;

    // COMPOSITION: PlayableEntity "HAS A" CollisionBehavior.
    // This is more flexible than inheritance.
    private CollisionBehavior collisionBehavior;

    // Constructors
    public PlayableEntity() {
        super();
    }
    
    /**
     * Parameterized Constructor.
     * Calls the super() constructor to set x, y, speed, etc in the parent Entity class.
     */
    /**
     * Parameterized Constructor.
     * Now accepts: 
     * 1. soundPath (String) -> Decouples the specific sound file.
     * 2. outputManager (Manager) -> Required to play the sound.
     * 3. behavior (CollisionBehavior) -> Decouples the game logic (Reset vs Die vs Bounce).
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

    @Override
    public void movement() {
        // This is empty because PlayableEntity is moved by InputManager/Keyboard.
    }

    /**
     * Triggered by Collision.java when a hit is detected.
     * @param other The object we hit.
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
    
    // load collision sound
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

