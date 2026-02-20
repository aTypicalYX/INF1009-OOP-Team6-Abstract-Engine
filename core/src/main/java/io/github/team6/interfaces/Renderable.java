package io.github.team6.interfaces;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


/**
 * Interface: Renderable
 * Allows the engine to treat Buckets, Enemies, and UI elements the same way.
 * They will all have a draw() method that takes in a SpriteBatch and draws itself onto the screen.
 */
public interface Renderable {
    public void draw(SpriteBatch batch);
}
