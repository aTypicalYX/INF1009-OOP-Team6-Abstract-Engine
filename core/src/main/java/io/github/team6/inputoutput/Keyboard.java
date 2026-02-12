package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;

public class Keyboard {
    public boolean isLeft() {
        return Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    public boolean isRight() {
        return Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    public boolean isUp() {
        return Gdx.input.isKeyPressed(Input.Keys.UP);
    }

    public boolean isDown() {
        return Gdx.input.isKeyPressed(Input.Keys.DOWN);
    }
    public boolean isW() {
        return Gdx.input.isKeyPressed(Input.Keys.W);
    }

    public boolean isA() {
        return Gdx.input.isKeyPressed(Input.Keys.A);
    }

    public boolean isS() {
        return Gdx.input.isKeyPressed(Input.Keys.S);
    }

    public boolean isD() {
        return Gdx.input.isKeyPressed(Input.Keys.D);
    }
    public boolean isSpacebar() {
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    public void getInput(Entity e) {
        Vector2 direction = new Vector2(0, 0);

        if (isLeft())  direction.x -= 5;
        if (isRight()) direction.x += 5;
        if (isUp())    direction.y += 5;
        if (isDown())  direction.y -= 5;

        if (!direction.isZero()) {
            direction.nor(); // Normalizes the vector to length 1
            e.setX(e.getX() + direction.x * e.getSpeed());
            e.setY(e.getY() + direction.y * e.getSpeed());
        }
    }
}
