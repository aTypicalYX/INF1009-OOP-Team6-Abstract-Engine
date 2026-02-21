package io.github.team6.managers;

import java.util.List;

import io.github.team6.collision.Collision;
import io.github.team6.entities.Entity;

import com.badlogic.gdx.math.Rectangle;
import io.github.team6.collision.WorldCollision;
import io.github.team6.entities.PlayableEntity;

/**
 * CollisionManager handles the detection of overlaps between entities.
 * It receives a list of entities and checks if any of them are touching.
 * Drives overlap checks across all entities and also checks player-vs-world collisions.
 * It uses the Collision class to perform the actual overlap math, and then triggers the appropriate responses
 */


public class CollisionManager {
    private Collision collision;

    // World collision data (from Tiled "Collisions" object layer) 
    private List<Rectangle> worldColliders;
    private float worldWidth;
    private float worldHeight;

    public CollisionManager() {
        this.collision = new Collision();
    }

    // Set up the world collision data
    public void setWorldCollisionData(List<Rectangle> worldColliders, float worldWidth, float worldHeight) {
        this.worldColliders = worldColliders;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    // entity-vs-entity
    public void update(List<Entity> entities) {
        for (int i = 0; i < entities.size() - 1; i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);
                collision.checkOverlap(a, b);
            }
        }
    }

    // Tiled rectangles
    public void updateWorld(PlayableEntity player, float prevX, float prevY) {
        if (player == null || worldColliders == null) return;

        WorldCollision.resolvePlayerVsWorld(
            player, prevX, prevY,
            worldColliders,
            worldWidth, worldHeight
        );
    }
}
