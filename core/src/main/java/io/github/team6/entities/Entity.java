package io.github.team6.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import io.github.team6.interfaces.Collidable;
import io.github.team6.interfaces.Movable;
import io.github.team6.interfaces.Renderable;


/**
 * Class: Entity (Abstract Base Class)
 * Defines the common attributes and behaviors shared by ALL game objects.
 * OOP Concept: Abstraction & Inheritance.
 * * This class implements multiple interfaces (Movable, Renderable, Collidable), forcing all subclasses (Playable/NonPlayable) to adhere to these contracts.
 */
public abstract class Entity implements Movable, Renderable, Collidable{

    // Encapsulation: Fields are private to prevent direct external modification. Access is controlled via Getters and Setters.
    private float x, y, speed, width, height;
    private Rectangle hitbox;
    private boolean active;

    // Tag System allows identification of objects (e.g., "PLAYER", "ENEMY") without relying on class-checking logic.
    private String tag;

    // Default Constructor chaining
    public Entity() {
        this(0, 0, 0, 0, 0, "DEFAULT");
    }

    // Main Constructor
    public Entity(float x, float y, float speed, float width, float height, String tag) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.tag = tag; // Assign the tag
        this.hitbox = new Rectangle(x, y, width, height);
        this.active = true;
    }

    // getters
    public float getX() { return x; }    
    public float getY() { return y; }    
    public float getSpeed() { return speed; }
    public float getWidth() { return width; }    
    public float getHeight() { return height; }
    public boolean isActive() { return active; }
    public String getTag() { return tag; }

    // setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setActive(boolean active) { this.active = active; }
    public void setTag(String tag) { this.tag = tag; }

    @Override
    // draw method
    public void draw(SpriteBatch batch) {}

    @Override
    public Rectangle getHitbox() {
        // Updates the internal hitbox position to match the entity's current location before returning it for collision checks
        hitbox.setPosition(this.x, this.y);
        return hitbox;
    }

    // Abstract Method: Subclasses MUST implement their own reaction to collisions.
    @Override
    public abstract void onCollision(Entity other);

}
