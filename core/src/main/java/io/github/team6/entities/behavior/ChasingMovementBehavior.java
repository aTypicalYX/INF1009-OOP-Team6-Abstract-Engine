package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;


/**
 * Implements logic for an entity to chase a target while avoiding obstacles.
 * OOP Concept: Polymorphism & Interface.
 * * This class encapsulates the Chasing algorithm. By isolating this logic here,
 * we adhere to Single Responsibility Principle. The Entity doesn't know
 * the math behind chasing, it just delegates the task to this class.
 */
public class ChasingMovementBehavior implements MovementBehavior {
    
    // List of obstacles that the entity should avoid while chasing the target.
    // Use List<Entity> to allow for Polymorphism (we can avoid any type of Entity).
    private List<Entity> obstacles; 

    public ChasingMovementBehavior(List<Entity> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * move()
     * Calculates the vector required to move towards the target while adding repulsion vectors from obstacles to prevent collisions.
     * * @param self   The entity executing this behavior.
     * @param target The entity to chase, usually the Player.
     */
    @Override
    public void move(Entity self, Entity target) {
        if (target == null) return;

        // Calculate the direct vector to the target
        Vector2 chaseDirection = new Vector2(target.getX() - self.getX(), target.getY() - self.getY());
        if (chaseDirection.isZero()) return;

        chaseDirection.nor(); // Normalize to length 1 (direction only)
        Vector2 avoidDirection = new Vector2(0, 0); 

        // Obstacle Avoidance Loop
        // Polymorphism: Iterating over generic 'Entity' objects. 
        // Don't know if it's a Wall or a Droplet, just that it exists.
        for (Entity obstacle : obstacles) {
            if (obstacle == self || !obstacle.isActive()) continue; 

            // Vector from obstacle to self
            Vector2 fromObstacle = new Vector2(self.getX() - obstacle.getX(), self.getY() - obstacle.getY());
            float distance = fromObstacle.len();

            // Calculate an influence radius based on size
            float influenceRadius = Math.max(self.getWidth(), self.getHeight()) + Math.max(obstacle.getWidth(), obstacle.getHeight());

            // If within range, add a repulsion force
            if (distance > 0 && distance < influenceRadius) {
                fromObstacle.nor();
                float strength = (influenceRadius - distance) / influenceRadius;
                avoidDirection.add(fromObstacle.scl(strength));
            }

            // If actually touching, push away hard
            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                Vector2 tangent = new Vector2(-(target.getY() - self.getY()), target.getX() - self.getX());
                if (!tangent.isZero()) {
                    tangent.nor();
                    avoidDirection.add(tangent.scl(1.2f)); 
                }
            }
        }

        // Combine Chase vector + Avoidance vector
        Vector2 finalDirection = new Vector2(chaseDirection); 

        if (!avoidDirection.isZero()) {
            avoidDirection.nor();
            finalDirection.add(avoidDirection.scl(1.35f)); 
        }

        if (finalDirection.isZero()) return;

        finalDirection.nor();  

        // Apply Speed and Update Position
        float newX = self.getX() + finalDirection.x * self.getSpeed();
        float newY = self.getY() + finalDirection.y * self.getSpeed();

        // Boundary Checking (Keep within screen)
        newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - self.getWidth()));
        newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - self.getHeight()));

        self.setX(newX);
        self.setY(newY);
    }
}