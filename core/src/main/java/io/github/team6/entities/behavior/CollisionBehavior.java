package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;
/**
 * Interface: CollisionBehavior
 * Defines a contract for how an Entity reacts when it hits something.
 * OOP Concept: Strategy Pattern.
 * It adheres to the Open/Closed Principle. We can add new collision reactions without modifying the Entity class source code.
 */
public interface CollisionBehavior {
    /**
     * Triggered by CollisionManager when an overlap is detected.
     * @param self  The entity triggering the behavior.
     * @param other The entity that was hit.
     */
    void onCollision(Entity self, Entity other);
}
