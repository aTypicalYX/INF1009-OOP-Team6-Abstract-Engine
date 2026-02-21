package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 Resets the entity to the starting position (100, 150) if it hits a specific hazard.
 Used By: PlayableEntity
 */
public class ResetOnTouchBehavior implements CollisionBehavior {

    @Override
    public void onCollision(Entity self, Entity other) {
        // Identify the type of the other object using a String tag.
        // This decouples the logic from specific Java classes.
        if (other.getTag().equals("ENEMY") || other.getTag().equals("HAZARD")) {
            System.out.println("Hit Hazard! Resetting position.");

            // Execute the reset logic
            self.setX(100);
            self.setY(150);
        }
    }
}