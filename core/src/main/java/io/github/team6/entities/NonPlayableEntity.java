package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NonPlayableEntity extends Entity{
    private Texture tex;

    // Constructors
    public NonPlayableEntity() {
    }

    public NonPlayableEntity(String fileName, float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.tex = new Texture(Gdx.files.internal(fileName));
    }

    // getter
    // public Texture getTexture() { return tex; }

    //setter
    // public void setTexture(Texture tex) { this.tex = tex; }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(tex, getX(), getY());
    }

    @Override
    public void movement() {
        setY(getY() - getSpeed());  //AI movement
        if (getY() < 0) setY(480);
    }

    @Override
    public void onCollision() {
        // implement post collision logic
    }
}
