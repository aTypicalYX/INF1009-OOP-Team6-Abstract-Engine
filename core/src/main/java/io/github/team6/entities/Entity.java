package io.github.team6.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import io.github.team6.interfaces.Collidable;
import io.github.team6.interfaces.Movable;
import io.github.team6.interfaces.Renderable;

public abstract class Entity implements Movable, Renderable, Collidable{
    private float x;
    private float y;
    private float speed;
    private float width;
    private float height;
    private Rectangle hitbox;
    private boolean active;

    // Constructor Methods
    public Entity() {
        this.x = 0;
        this.y = 0;
        this.speed = 0;
        this.width = 0;
        this.height = 0;
        this.hitbox = new Rectangle(x, y, width, height);
        this.active = true;
    }

    public Entity(float x, float y, float speed, float width, float height) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = width;
        this.height = height;
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

    // setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setActive(boolean active) { this.active = active; }

    // draw method
    public void draw(SpriteBatch batch) {}

    @Override
    public Rectangle getHitbox() {
        // Sync the hitbox position with the entity's current coordinates
        hitbox.setPosition(this.x, this.y);
        return hitbox;
    }

}
