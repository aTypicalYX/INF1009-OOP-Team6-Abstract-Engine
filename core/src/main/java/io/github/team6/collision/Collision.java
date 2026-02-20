package io.github.team6.collision;

import io.github.team6.entities.Entity;

/**
/*
Manager: Collision -> checks overlap -> calls .onCollision()

Entity: PlayableEntity -> has a -> ResetOnTouchBehavior

Behavior: ResetOnTouchBehavior -> executes -> x=0, y=0

Summary: Pure overlap detection math — no game logic
*/

public class Collision {

    /**
     * checkOverlap checks if two entities are overlapping using their hitboxes.
     * Flow: SceneManager -> CollisionManager -> Collision.checkOverlap(a, b)
     * @param a The first entity (e.g., Player)
     * @param b The second entity (e.g., Droplet)
     */
    public void checkOverlap(Entity a, Entity b) {
        // 1. Basic Box Collision Check
        if (!a.getHitbox().overlaps(b.getHitbox())) { 
            return; // No collision, exit early
        }

        // 2. Resolution: If they hit, tell them both.
        // We use Polymorphism here. We don't care if a is a Player or an Enemy.
        // We just know it is an Entity, so it has an onCollision method.
        resolveCollision(a, b);
    }

    /**
     * Helper method to trigger the response.
     * We let the entities decide how to respond to the collision, through polmorphism.
     */
    public void resolveCollision(Entity a, Entity b) {
        // Notify 'a' that it hit 'b'
        a.onCollision(b); 
        
        // Notify 'b' that it hit 'a'
        b.onCollision(a);
    }
}