package io.github.team6.interfaces;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


// all entities have draw() function to render itself on screen
public interface Renderable {
    public void draw(SpriteBatch batch);
}
