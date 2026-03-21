package io.github.team6.entities;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.entities.behavior.MovementBehavior;

public class NonPlayableEntity extends Entity {

    private static final Map<String, Texture> textureCache = new HashMap<>();
    private Texture tex;

    // Strategy Interfaces: these hold the specific logic for this instance.
    private MovementBehavior movementBehavior;
    private CollisionBehavior collisionBehavior;
    private Entity target;

    // --- Spinning Animation ---
    // Variables to track and control the spinning animation for certain entities.
    private float rotation = 0f;
    private float rotationSpeed = 0f; // Measured in degrees per second

    // Constructors
    public NonPlayableEntity() {
        super();
    }

    // Basic Constructor
    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height, String tag) {
        super(x, y, speed, width, height, tag);
        // Only load the texture if we haven't seen it before, to save memory.
        if (!textureCache.containsKey(fileName)) {
            textureCache.put(fileName, new Texture(Gdx.files.internal(fileName)));
        }
        // Assign the shared texture reference
        this.tex = textureCache.get(fileName);
    }

    // Full Constructor: Performs Dependency Injection for behaviors.    
    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height, String tag,
            MovementBehavior movementBehavior, CollisionBehavior collisionBehavior, Entity target) {
        this(fileName, x, y, speed, width, height, tag);
        this.movementBehavior = movementBehavior;
        this.collisionBehavior = collisionBehavior;
        this.target = target;
    }

    // --- Setter for rotation ---
    // Allows factories to make certain entities spin, while others (like black holes) stay still
    public void setRotationSpeed(float speed) {
        this.rotationSpeed = speed;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (tex != null) {
            // --- LibGDX Drawing ---
            // Calculate the exact center of the entity so it spins on its own axis
            float originX = getWidth() / 2f;
            float originY = getHeight() / 2f;

            // This 16-parameter draw method allows us to apply the dynamic rotation
            // without distorting the scale or stretching the image.
            batch.draw(tex, 
                getX(), getY(), 
                originX, originY, 
                getWidth(), getHeight(), 
                1.0f, 1.0f,       // scaleX, scaleY (1.0 = normal size)
                rotation,         // The dynamically updating angle
                0, 0,             // srcX, srcY (Start at top left of image)
                tex.getWidth(), tex.getHeight(), // Read the full width/height of the texture
                false, false      // Do not flip the image horizontally or vertically
            );
        }
    }

    @Override
    public void movement() {
        // --- Tick the rotation forward ---
        // Use getDeltaTime() so the spin is perfectly smooth regardless of computer lag
        rotation += rotationSpeed * Gdx.graphics.getDeltaTime();

        // Delegates the movement logic to the assigned MovementBehavior strategy
        if (movementBehavior != null) {
            movementBehavior.move(this, target);
        }
    }

    @Override
    public void onCollision(Entity other) {
        // Delegates collision logic to the assigned CollisionBehavior strategy
        if (collisionBehavior != null) {
            collisionBehavior.onCollision(this, other);
        }
    }
}