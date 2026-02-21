package io.github.team6.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;


/**
* Class: Keyboard
 * A simple input wrapper that translates hardware keyboard controls into game logic.
 * * OOP Concepts & Design Patterns:
 * - Facade Pattern / Abstraction: Hides the low-level LibGDX key polling (Input.Keys.LEFT) behind 
 * semantic methods (isLeft()). This abstracts the hardware away from the rest of the engine.
 * - Decoupling: The PlayableEntity does not know how to read the keyboard. Instead, this class reads the keyboard and applies a Vector2 mathematical translation to the Entity. If the game later supports 
 * gamepads, the Entity code remains unchanged.
 */

public class Keyboard {

    // --- Key polling (kept here so game code doesn't touch Input.Keys directly) ---
    // these abstract the specific LibGDX key codes (Input.Keys.LEFT) from the game logic
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


    /**
     * General-purpose movement handler (currently builds direction only)
     * note: this method isn't applying movement yet, see getArrowInput()
     */
    public void getInput(Entity e) {
        Vector2 direction = new Vector2(0, 0);

        // calculate direction based on keys
        if (isLeft())  direction.x -= 5;
        if (isRight()) direction.x += 5;
        if (isUp())    direction.y += 5;
        if (isDown())  direction.y -= 5;

        
    }

    /**
     * Control Scheme: Arrow Keys
     * converts key presses into a direction vector, then applies movement to the entity
     */
    public void getArrowInput(Entity e) {
    Vector2 direction = new Vector2(0, 0);

    if (isLeft())  direction.x -= 5;
    if (isRight()) direction.x += 5;
    if (isUp())    direction.y += 5;
    if (isDown())  direction.y -= 5;

    applyMovement(e, direction);
    }



    /**
     * Applies movement if there's input:
     * - normalizes direction so diagonals aren't faster
     * - moves based on entity speed
     */

    private void applyMovement(Entity e, Vector2 direction) {
        if (!direction.isZero()) {
            direction.nor();

            float newX = e.getX() + direction.x * e.getSpeed();
            float newY = e.getY() + direction.y * e.getSpeed();

            //newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - e.getWidth()));
           // newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - e.getHeight()));

            e.setX(newX);
            e.setY(newY);
        }
    }
}
