package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.entities.behavior.MovementBehavior;


public class NonPlayableEntity extends Entity{
    

    private Texture tex;

    // Strategy Interfaces: these hold the specific logic for this instance.
    private MovementBehavior movementBehavior;
    private CollisionBehavior collisionBehavior;
    private Entity target;

    // Constructors
    public NonPlayableEntity() {
        super();
    }

    // Basic Constructor
    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height, String tag) {
        super(x, y, speed, width, height, tag);
        this.tex = new Texture(Gdx.files.internal(fileName));
    }

    // Full Constructor: Performs Dependency Injection for behaviors.    
    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height, String tag,
            MovementBehavior movementBehavior, CollisionBehavior collisionBehavior, Entity target) {
        this(fileName, x, y, speed, width, height, tag);
        this.movementBehavior = movementBehavior;
        this.collisionBehavior = collisionBehavior;
        this.target = target;
    }


    @Override
    public void draw(SpriteBatch batch) {
        if (tex != null) batch.draw(tex, getX(), getY());
    }

    @Override
    public void movement() {
        //Delegates the movement logic to the assigned MovementBehavior strategy
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
