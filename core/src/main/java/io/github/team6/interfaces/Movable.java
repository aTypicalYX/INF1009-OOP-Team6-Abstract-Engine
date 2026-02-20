

package io.github.team6.interfaces;

/**
 * Interface: Movable
 * Defines a contract that the object has movement logic.
 * ALl objects that implement this interface must have a movement() method that defines how they move each frame.
 * The MovementManager will call this movement() method for all Movable objects each frame, allowing them to update their positions based on their specific movement logic.
 */
public interface Movable {
    public void movement();
}
