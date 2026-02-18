package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 * Implements: CollisionBehavior
 * Resets the entity to the starting position (0,0) if it hits a specific hazard.
 * Used By: PlayableEntity
 * OOP Concept: Open/Closed Principle (OCP).
 * By using Tags ("ENEMY", "HAZARD") instead of 'instanceof' checks, this class 
 * is Open for extension, we can add new hazard types, but Closed for modification
 * (we don't need to change this code to handle new types).
 */
public class ResetOnTouchBehavior implements CollisionBehavior {

    @Override
    public void onCollision(Entity self, Entity other) {
        // We identify the 'type' of the other object using a String tag.
        // This decouples the logic from specific Java classes.
        if (other.getTag().equals("ENEMY") || other.getTag().equals("HAZARD")) {
            System.out.println("Hit Hazard! Resetting position.");

            // Execute the reset logic
            self.setX(0);
            self.setY(0);
        }
    }
}