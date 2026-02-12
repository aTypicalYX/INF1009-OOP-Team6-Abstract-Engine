package io.github.team6.collision;

import io.github.team6.entities.Entity;

public class Collision {
    public void checkOverlap(Entity a, Entity b) {
        if (a.getHitbox().overlaps(b.getHitbox())) {
            System.out.println("Collided!!");
            resolveCollision(a, b);
        }
    }

    public void resolveCollision(Entity a, Entity b) {
        a.onCollision(); 
        b.onCollision();
    }
}
