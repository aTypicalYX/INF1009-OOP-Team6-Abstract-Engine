package io.github.team6.collision;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.NonPlayableEntity.DropletType;

public class Collision {
    public void checkOverlap(Entity a, Entity b) {
        if (!a.getHitbox().overlaps(b.getHitbox())) { // No collision
            return;
        }
        if (isBucketWithChasingDroplet(a, b) || isBucketWithPermanentDroplet(a, b)) { 
            // Collision between bucket and a droplet
            System.out.println("Collided!!");
            resolveCollision(a, b);
            return;
        }
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

    public void resolveCollision(Entity a, Entity b) {
        a.onCollision(); 
        b.onCollision();
    }
}
