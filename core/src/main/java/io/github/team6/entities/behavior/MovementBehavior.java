package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 * Interface: MovementBehavior
 * Defines a contract for any algorithm that controls Entity movement.
 * * Relationships:
 * - Implemented by: ChasingMovementBehavior, StationaryMovementBehavior, ScreenBounceBehavior.
 * - Aggregated by: NonPlayableEntity
 *
 * 1. MovementManager iterates through all entities.
 * 2. It calls entity.movement().
 * 3. The entity delegates the call to its specific MovementBehavior.move().
 */
public interface MovementBehavior {
    /**
     * Abstract method that concrete classes must implement.
     * @param self   The entity executing the movement
     * @param target The entity to interact with (e.g., the player to chase), can be null.
     */
    void move(Entity self, Entity target);
}
