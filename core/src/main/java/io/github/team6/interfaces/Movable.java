

package io.github.team6.interfaces;


 // ensures all entities that implement this interface have a movement() method that defines how they move each frame
 //MovementManager will call this movement() method for all Movable objects each frame, allowing them to update their positions based on their specific movement logic
public interface Movable {
    public void movement();
}
