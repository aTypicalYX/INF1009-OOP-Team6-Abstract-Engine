package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

// Class declaration: defines a movement behavior that makes entities chase a target while avoiding obstacles, implementing MovementBehavior
public class ChasingMovementBehavior implements MovementBehavior {
    private List<NonPlayableEntity> obstacles;

    // Constructor: initializes the behavior with a list of obstacles
    public ChasingMovementBehavior(List<NonPlayableEntity> obstacles) {
        this.obstacles = obstacles;
    }

    // Method override: implements the move method from MovementBehavior interface
    // This method moves the entity towards the target while avoiding obstacles
    @Override
    public void move(Entity self, Entity target) {
        // Checks if there is no target to chase
        if (target == null) {
            // Returns early if no target
            return;
        }

        // Calculates the direction vector from self to target
        Vector2 chaseDirection = new Vector2(target.getX() - self.getX(), target.getY() - self.getY());
        // Checks if the chase direction is zero (target is at the same position)
        if (chaseDirection.isZero()) {
            // Returns early if already at target
            return;
        }

        // Normalizes the chase direction to get a unit vector
        chaseDirection.nor();

        // Initializes a vector to accumulate avoidance directions
        Vector2 avoidDirection = new Vector2(0, 0);

        // Loops through each obstacle
        for (NonPlayableEntity obstacle : obstacles) {
            // Skips if the obstacle is the entity itself or not active
            if (obstacle == self || !obstacle.isActive()) {
                // Continues to the next obstacle
                continue;
            }

            // Calculates the vector from obstacle to self
            Vector2 fromObstacle = new Vector2(self.getX() - obstacle.getX(), self.getY() - obstacle.getY());

            // Calculates the distance from self to obstacle
            float distance = fromObstacle.len();

            // Calculates the influence radius based on sizes of self and obstacle
            float influenceRadius = Math.max(self.getWidth(), self.getHeight()) + Math.max(obstacle.getWidth(), obstacle.getHeight());

            // Checks if the distance is within the influence radius
            if (distance > 0 && distance < influenceRadius) {
                // Normalizes the fromObstacle vector
                fromObstacle.nor();

                // Calculates the strength of avoidance based on proximity
                float strength = (influenceRadius - distance) / influenceRadius;

                // Adds the avoidance force to the avoidDirection vector
                avoidDirection.add(fromObstacle.scl(strength));
            }

            // Checks if the entity's hitbox overlaps with the obstacle's hitbox
            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                // Calculates a tangent vector perpendicular to the chase direction for sliding
                Vector2 tangent = new Vector2(-(target.getY() - self.getY()), target.getX() - self.getX());

                // Checks if the tangent vector is not zero
                if (!tangent.isZero()) {
                    // Normalizes the tangent vector
                    tangent.nor();

                    // Adds a strong avoidance force in the tangent direction
                    avoidDirection.add(tangent.scl(1.2f));
                }
            }
        }

        // Creates a copy of the chase direction for the final direction
        Vector2 finalDirection = new Vector2(chaseDirection);

        // Checks if there is any avoidance direction
        if (!avoidDirection.isZero()) {
            // Normalizes the avoid direction
            avoidDirection.nor();

            // Adds the avoidance direction to the final direction with a scaling factor
            finalDirection.add(avoidDirection.scl(1.35f));
        }

        // Checks if the final direction is zero
        if (finalDirection.isZero()) {
            // Returns early if no movement needed
            return;
        }

        // Normalizes the final direction
        finalDirection.nor();

        // Calculates the new X position based on current position, direction, and speed
        float newX = self.getX() + finalDirection.x * self.getSpeed();

        // Calculates the new Y position based on current position, direction, and speed
        float newY = self.getY() + finalDirection.y * self.getSpeed();

        // Clamps the new X position to stay within screen bounds
        newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - self.getWidth()));

        // Clamps the new Y position to stay within screen bounds
        newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - self.getHeight()));

        // Sets the entity's new X position
        self.setX(newX);

        // Sets the entity's new Y position
        self.setY(newY);
    }
}
