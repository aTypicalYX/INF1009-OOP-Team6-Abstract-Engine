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
    private int lives;

    private CollisionBehavior collisionBehavior;

    // --- I-Frame Timer ---
    private float invulnerableTimer = 0f;

    // Constructors
    public PlayableEntity() {
        super();
    }
    
    /**
     * @param outputManager //Used to play sounds via the audio system.
     * @param behavior      //Defines what happens when the player is hit.
     */
    public PlayableEntity(String texturePath, String soundPath, OutputManager outputManager, CollisionBehavior behavior, float x, float y, float speed, float width, float height, String tag, int lives) {
        super(x, y, speed, width, height, tag);
        this.tex = new Texture(Gdx.files.internal(texturePath));
        this.outputManager = outputManager;
        this.collisionBehavior = behavior; 
        this.lives = lives;

        if (soundPath != null && outputManager != null) {
            try {
                this.collisionSound = new AudioSource(soundPath);
                this.collisionSound.setVolume(0.1f);
            } catch (Exception e) {
                System.out.println("[WARNING] Could not load sound: " + soundPath);
            }
        }
    }

    // --- I-Frame Methods ---
    public boolean isInvulnerable() {
        return invulnerableTimer > 0;
    }

    public void setInvulnerable(float time) {
        this.invulnerableTimer = time;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // --- I-Frame: Blinking Effect ---
        if (isInvulnerable()) {
            // Rapidly blink visibility every 0.1 seconds
            if ((int)(invulnerableTimer * 10) % 2 == 0) return; 
        }

        if (tex != null) batch.draw(tex, getX(), getY());
    }

    @Override
    public void movement() {
        // --- I-Frame: Tick down the timer ---
        if (invulnerableTimer > 0) {
            invulnerableTimer -= Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void onCollision(Entity other) {
        if (outputManager != null && collisionSound != null) {
            outputManager.play(collisionSound);
        }
        
        if (collisionBehavior != null) {
            collisionBehavior.onCollision(this, other);
        }
    }
    
    public void setOutputManager(OutputManager outputManager) {
        this.outputManager = outputManager;
        try {
            collisionSound = new AudioSource("collision.wav");
            collisionSound.setVolume(0.1f); 
        } catch (Exception e) {
            collisionSound = null;
        }
    }
    
    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return this.lives;
    }

    @Override
    public void dispose() {
        if (collisionSound != null) {
            collisionSound.dispose();
            collisionSound = null;
        }
        if (tex != null) {
            tex.dispose();
            tex = null;
        }
    }
}