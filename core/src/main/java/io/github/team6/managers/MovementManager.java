
package io.github.team6.managers;

import java.util.List;

import io.github.team6.entities.Entity;

/**
 * MovementManager is responsible for applying physics/movement logic.
 * * OOP Concepts & Design Patterns:
 * - Polymorphism: Iterates through a generic list of `Entity` objects and calls `.movement()`. 
 * Because of dynamic binding, it doesn't need to know whether the entity is a player, a chasing enemy, 
 * or a stationary object. The correct logic is executed automatically based on the object's dynamic type.
 * - Strategy Pattern Enabler: By triggering `.movement()`, it activates the specific `MovementBehavior` 
 * injected into each entity, driving the Component-Based architecture of the engine.
 */

public class MovementManager {
    public void update(List<Entity> entities) {
        for (Entity e : entities) {
            e.movement(); // Triggers the specific movement logic for that subclass
        }
    }
}
