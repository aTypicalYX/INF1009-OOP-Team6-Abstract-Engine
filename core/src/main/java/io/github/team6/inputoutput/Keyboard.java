package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;

/**
 * Keyboard is a Low-Level Input Wrapper.
 * * ROLE:
 * It wraps the LibGDX "Gdx.input" calls into easier-to-read methods.
 * It currently handles translating Key Presses into Entity Position changes.
 */


 // These methods return true if the specific key is currently held down.
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

    /**
     * getInput() calculates the movement vector based on keys pressed.
     * @param e The entity to move (usually the player).
     */
    public void getInput(Entity e) {
        Vector2 direction = new Vector2(0, 0);

        // Calculate direction based on keys
        if (isLeft())  direction.x -= 5;
        if (isRight()) direction.x += 5;
        if (isUp())    direction.y += 5;
        if (isDown())  direction.y -= 5;

        // Apply movement if a key was pressed
        if (!direction.isZero()) {
            direction.nor(); // Normalizes the vector to length 1
            //e.setX(e.getX() + direction.x * e.getSpeed());
            //e.setY(e.getY() + direction.y * e.getSpeed());
            float newX = e.getX() + direction.x * e.getSpeed();
            float newY = e.getY() + direction.y * e.getSpeed();
            
            // Boundary Checking: Prevent moving off-screen
            // Math.max(0, ...) ensures it doesn't go negative (left/bottom edge)
            // Math.min(..., Gdx.graphics.getWidth()) ensures it doesn't go past right/top edge
            newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - e.getWidth()));
            newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - e.getHeight()));
            
            // Update the entity's position
            e.setX(newX);
            e.setY(newY);
        }
    }
}
