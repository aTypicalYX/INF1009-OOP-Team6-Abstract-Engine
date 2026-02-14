package io.github.team6.interfaces;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


/**
 * Interface: Renderable
 * Defines a contract that "Anything implementing this MUST be able to draw itself".
 * Allows the engine to treat Buckets, Enemies, and UI elements the same way.
 */
public interface Renderable {
    public void draw(SpriteBatch batch);
}
