package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 * Defines a behavior where the entity destroys itself upon contact.
 * OOP Concept: Strategy Pattern.
 * * Can be assigned to future items coins, pickups, or projectiles.
 */
public class DisappearOnCollisionBehavior implements CollisionBehavior {

    /**
     * onCollision()
     * Implementation of the abstract method defined in the Interface.
     */
    @Override
    public void onCollision(Entity self, Entity other) {
        // Deactivate the entity so the EntityManager removes it next frame.
        // Decouples the Death logic from the Entity class itself.
        self.setActive(false);
    }
}
