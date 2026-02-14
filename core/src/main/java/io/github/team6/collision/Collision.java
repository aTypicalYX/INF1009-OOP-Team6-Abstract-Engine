package io.github.team6.collision;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.NonPlayableEntity.DropletType;
import io.github.team6.entities.PlayableEntity;


/**
 * Collision performs the math to check if two things touch.
 * It does NOT manage lists of objects (Manager's job).
 */
public class Collision {

    /**
     * checkOverlap determines if two entities have collided.
     * @param a The first entity
     * @param b The second entity
     */

    public void checkOverlap(Entity a, Entity b) {
        // LibGDX's Rectangle class has a built-in .overlaps() method.
        // Use getHitbox() to ensure we are checking the current position.

        if (!a.getHitbox().overlaps(b.getHitbox())) { // No collision
            return;
        }
        if (isBucketWithChasingDroplet(a, b) || isBucketWithPermanentDroplet(a, b)) { 
            // Collision between bucket and a droplet
            System.out.println("Collided!!");
            resolveCollision(a, b);
            return;
        }

        // If they touch, trigger the resolution logic
        resolveCollision(a, b);
    }

    private boolean isBucketWithChasingDroplet(Entity a, Entity b) {
        // Check if one entity is the bucket and the other is a chasing droplet
        return (isPlayable(a) && isDropletType(b, DropletType.CHASING))
                || (isPlayable(b) && isDropletType(a, DropletType.CHASING));
    }

    private boolean isBucketWithPermanentDroplet(Entity a, Entity b) {
        // Check if one entity is the bucket and the other is a permanent stationary droplet
        return (isPlayable(a) && isDropletType(b, DropletType.PERMANENT_STATIONARY))
                || (isPlayable(b) && isDropletType(a, DropletType.PERMANENT_STATIONARY));
    }

    private boolean isPlayable(Entity entity) {
        return entity instanceof PlayableEntity;
    }

    private boolean isDropletType(Entity entity, DropletType expectedType) {
        if (entity instanceof NonPlayableEntity) {
            NonPlayableEntity nonPlayableEntity = (NonPlayableEntity) entity;
            return nonPlayableEntity.getDropletType() == expectedType;
        }
        return false;
    }


    /**
     * resolveCollision handles what happens AFTER a collision is confirmed.
     */
    public void resolveCollision(Entity a, Entity b) {
        // If 'a' is a Bucket, it runs Bucket.onCollision().
        // If 'a' is a Droplet, it runs Droplet.onCollision().
        a.onCollision(); 
        b.onCollision();
    }
}
