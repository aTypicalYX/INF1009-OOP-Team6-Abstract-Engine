package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

/**
 * Class: StationaryMovementBehavior
 * Defines a "Null" behavior for objects that do not move.
 * OOP Concept: Null Object Pattern / Polymorphism.
 * * Even though this code does nothing, it is necessary so that 'Stationary' entities 
 * can still be treated as 'Movable' objects by the MovementManager, preventing null pointer errors.
 */
public class StationaryMovementBehavior implements MovementBehavior {
    @Override
    public void move(Entity self, Entity target) {
        // Intentionally empty: this droplet does not move.
    }
}
