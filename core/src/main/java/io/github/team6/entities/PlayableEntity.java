package io.github.team6.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayableEntity extends Entity {
    private Texture tex;

    // Constructors
    public PlayableEntity() {
    }
    
    public PlayableEntity(String fileName, float x, float y, float speed, float width, float height) {
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
        
    }

    @Override
    public void onCollision() {
        // upon collision, teleport back to coordinates 0,0
        this.setX(0);
        this.setY(0);
    }
    
}

