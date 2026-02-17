package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

/*
Implements a chasing behavior where an entity moves toward a target while avoiding obstacles.
The entity calculates a direction to the target and adjusts its path to steer clear of nearby obstacles,
including sliding along obstacles if in collision, ensuring smooth pursuit within screen bounds.
*/

public class ChasingMovementBehavior implements MovementBehavior {
    private List<NonPlayableEntity> obstacles; // List of obstacles to avoid during movement

    // Constructor takes list of obstacles to consider
    public ChasingMovementBehavior(List<NonPlayableEntity> obstacles) {
        this.obstacles = obstacles;
    }


    @Override
    public void move(Entity self, Entity target) {
        if (target == null) {
            return; // No target to chase, so skip movement
        }

        // Calculate normalized vector pointing from self to target
        Vector2 chaseDirection = new Vector2(target.getX() - self.getX(), target.getY() - self.getY());
        if (chaseDirection.isZero()) {
            return; // Already at target, no movement needed
        }

        chaseDirection.nor();

        Vector2 avoidDirection = new Vector2(0, 0); // Accumulates avoidance vectors from obstacles

        // Loop through obstacles to calculate avoidance
        for (NonPlayableEntity obstacle : obstacles) {
            if (obstacle == self || !obstacle.isActive()) {
                continue; // Skip self and inactive obstacles
            }

            // Vector pointing away from obstacle to self
            Vector2 fromObstacle = new Vector2(self.getX() - obstacle.getX(), self.getY() - obstacle.getY());

            float distance = fromObstacle.len();
            // Define radius within which avoidance applies, based on entity sizes
            float influenceRadius = Math.max(self.getWidth(), self.getHeight()) + Math.max(obstacle.getWidth(), obstacle.getHeight());

            // If obstacle is within influence radius, add avoidance vector weighted by proximity
            if (distance > 0 && distance < influenceRadius) {
                fromObstacle.nor();

                float strength = (influenceRadius - distance) / influenceRadius;

                avoidDirection.add(fromObstacle.scl(strength));
            }

            // If self is already colliding with obstacle, add tangent vector to escape collision
            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                // Tangent vector perpendicular to direction towards target
                Vector2 tangent = new Vector2(-(target.getY() - self.getY()), target.getX() - self.getX());

                if (!tangent.isZero()) {
                    tangent.nor();

                    avoidDirection.add(tangent.scl(1.2f)); // Slightly stronger push along tangent to slide away
                }
            }
        }

        Vector2 finalDirection = new Vector2(chaseDirection); // Start with chasing direction

        // Add avoidance contribution if present
        if (!avoidDirection.isZero()) {
            avoidDirection.nor();

            finalDirection.add(avoidDirection.scl(1.35f)); // Weighted influence
        }

        if (finalDirection.isZero()) {
            return; // No movement if combined direction is zero
        }

        finalDirection.nor();  // Normalize final movement vector

         // Calculate new position based on speed and direction
        float newX = self.getX() + finalDirection.x * self.getSpeed();
        float newY = self.getY() + finalDirection.y * self.getSpeed();

        // Clamp position within screen bounds
        newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - self.getWidth()));
        newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - self.getHeight()));

        // Apply new position to entity
        self.setX(newX);
        self.setY(newY);
    }
}