package io.github.team6.managers;

import java.util.List;

import io.github.team6.collision.Collision;
import io.github.team6.entities.Entity;

/**
 * CollisionManager handles the detection of overlaps between entities.
 * * ROLE:
 * It receives a list of entities and checks if any of them are touching.
 * 
 * Drives overlap checks across all entities
 */


public class CollisionManager {
    private Collision collision;

    public CollisionManager() {
        this.collision = new Collision();
    }

    public void update(List<Entity> entities) {
        for (int i = 0; i < entities.size() -1; i++) {
            for (int j = i+1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);
                collision.checkOverlap(a, b);
            }
        }
    }
}
