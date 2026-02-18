package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 * Class: PermanentCollisionBehavior
 * Represents an object that is solid but has no specific reaction to collision.
 * OOP Concept: Null Object Pattern / Default Implementation.
 * Used for obstacles or walls that block movement or exist as hazards, 
 * but do not destroy themselves or change state when hit.
 */
public class PermanentCollisionBehavior implements CollisionBehavior {
    @Override
    public void onCollision(Entity self, Entity other) {
        // Intentionally empty. 
        // This signifies that the entity persists in the world after collision.
        // Logic regarding the other entity (e.g., Player taking damage) is handled 
        // by the other entity's collision behavior.
    }
}
