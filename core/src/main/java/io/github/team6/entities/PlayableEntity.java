package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.OutputManager;

public class PlayableEntity extends Entity {
    private Texture tex;
    private OutputManager outputManager;
    private AudioSource collisionSound;

    // Constructors
    public PlayableEntity() {
    }
    
    /**
     * Parameterized Constructor.
     * Calls the super() constructor to set x, y, speed, etc in the parent Entity class.
     */
    public PlayableEntity(String fileName, float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.tex = new Texture(Gdx.files.internal(fileName));
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
        // It doesn't move automatically on its own.
        
    }

    //defines what happens when THIS specific object hits something.
    
    @Override
    public void onCollision() {
        // Play collision sound
        if (outputManager != null && collisionSound != null) {
            outputManager.play(collisionSound);
        }
        
        // When the bucket hits a droplet, it teleports back to (0,0).
        this.setX(0);
        this.setY(0);
    }
    
    // load collision sound
    public void setOutputManager(OutputManager outputManager) {
        this.outputManager = outputManager;
        try {
            collisionSound = new AudioSource("collision.wav");
            collisionSound.setVolume(0.1f); // 10% volume
            System.out.println("[DEBUG] collision.wav loaded at 10% volume");
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: collision.wav not found.");
            e.printStackTrace();
            collisionSound = null;
        }
    }
    
}

