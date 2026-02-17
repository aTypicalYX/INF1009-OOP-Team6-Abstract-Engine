package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;


/**
 * Implements: CollisionBehavior
 * This defines the specific reaction of "Resetting to start" when hit.
 * Used By: PlayableEntity (The Bucket)
 * * This allows us to swap behaviors easily.
 * Uses Tags instead of Class Types (OCP Compliant).
 */
public class ResetOnTouchBehavior implements CollisionBehavior {

    @Override
    public void onCollision(Entity self, Entity other) {
        // PHASE 3 FIX: Check the TAG, not the Class.
        // This allows "Spikes", "Lava", or "Enemies" to all trigger this logic
        // without changing the code here.
        if (other.getTag().equals("ENEMY") || other.getTag().equals("HAZARD")) {
            System.out.println("Hit Hazard! Resetting position.");

            // Execute the reset logic
            self.setX(0);
            self.setY(0);
        }
    }
}