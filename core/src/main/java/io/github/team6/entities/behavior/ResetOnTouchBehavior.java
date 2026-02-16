package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;


/**
 * Implements: CollisionBehavior
 * This defines the specific reaction of "Resetting to start" when hit.
 * Used By: PlayableEntity (The Bucket)
 * * This allows us to swap behaviors easily.
 */
public class ResetOnTouchBehavior implements CollisionBehavior {

    /**
     * @param self  The entity that owns this behavior (The Player)
     * @param other The entity that was hit (The Droplet)
     */
    @Override
    public void onCollision(Entity self, Entity other) {
        // Only reset if we hit an Enemy (NonPlayableEntity).
        if (other instanceof NonPlayableEntity) {
            System.out.println("Hit enemy! Resetting position.");

            // Execute the reset logic
            self.setX(0);
            self.setY(0);
            
            // Note: Sound is currently handled in PlayableEntity. 
            // Ideally, sound logic would also move here, but let's keep it simple for now.
        }
    }
}