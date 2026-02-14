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
    
    public enum DropletType {
        CHASING,
        STATIONARY,
        PERMANENT_STATIONARY
    }

    private Texture tex;
    private MovementBehavior movementBehavior;
    private CollisionBehavior collisionBehavior;
    private Entity target;
    private DropletType dropletType;

    // Constructors
    public NonPlayableEntity() {
    }

    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.tex = new Texture(Gdx.files.internal(fileName));
    }

    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height,
            MovementBehavior movementBehavior, CollisionBehavior collisionBehavior, Entity target, DropletType dropletType) {
        this(fileName, x, y, speed, width, height);
        this.movementBehavior = movementBehavior;
        this.collisionBehavior = collisionBehavior;
        this.target = target;
        this.dropletType = dropletType;
    }

    // getter
    // public Texture getTexture() { return tex; }

    //setter
    // public void setTexture(Texture tex) { this.tex = tex; }

    public DropletType getDropletType() {
        return dropletType;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(tex, getX(), getY());
    }

    /**
     * movement() defines the non playable entity behavior.
     * Unlike PlayableEntity (which is empty here), this has logic.
     */
    @Override
    public void movement() {
        //setY(getY() - getSpeed());  //AI movement
        //if (getY() < 0) setY(480);
        if (movementBehavior != null) {
            movementBehavior.move(this, target);
        }
    }

    @Override
    public void onCollision() {
        // implement post collision logic
        if (collisionBehavior != null) {
            collisionBehavior.onCollision(this);
        }
    }
}
