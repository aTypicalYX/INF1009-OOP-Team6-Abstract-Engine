package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.entities.behavior.MovementBehavior;

/**
 * NonPlayableEntity represents objects controlled by the Computer
 */
public class NonPlayableEntity extends Entity{
    

    private Texture tex;
    private MovementBehavior movementBehavior;
    private CollisionBehavior collisionBehavior;
    private Entity target;

    // Constructors
    public NonPlayableEntity() {
        super();
    }

    
    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height, String tag) {
        super(x, y, speed, width, height, tag);
        this.tex = new Texture(Gdx.files.internal(fileName));
    }

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

    /**
     * movement() defines the non playable entity behavior.
     * Unlike PlayableEntity (which is empty here), this has logic.
     */
    @Override
    public void movement() {
        if (movementBehavior != null) {
            movementBehavior.move(this, target);
        }
    }

    @Override
    public void onCollision(Entity other) {
        /// implement post collision logic
        if (collisionBehavior != null) {
            collisionBehavior.onCollision(this, other);
        }
    }
}
