package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;

import io.github.team6.entities.Entity;

/**
 * Class: ScreenBounceBehavior
 * Handles physics for an entity that moves in a straight line and bounces off walls/obstacles.
 * OOP Concept: Reusability & Generalization.
 */
public class ScreenBounceBehavior implements MovementBehavior {
    private float velocityX;
    private float velocityY;
    
    // Using the interface 'Entity' allows this behavior to process collision against any game object (Polymorphism).
    private List<Entity> obstacles; 

    public ScreenBounceBehavior(float speed, List<Entity> obstacles) {
        this.velocityX = speed;
        this.velocityY = speed;
        this.obstacles = obstacles;
    }

    @Override
    public void move(Entity self, Entity target) {
        // 1. Calculate next projected position
        float nextX = self.getX() + velocityX;
        float nextY = self.getY() + velocityY;

        // 2. Bounce off Screen Edges (Invert Velocity)
        if (nextX < 0 || nextX > Gdx.graphics.getWidth() - self.getWidth()) { 
            velocityX *= -1;
            nextX = self.getX() + velocityX;
        }

        if (nextY < 0 || nextY > Gdx.graphics.getHeight() - self.getHeight()) { 
            velocityY *= -1;
            nextY = self.getY() + velocityY;
        }

        // Apply position update
        self.setX(nextX);
        self.setY(nextY);

        // 3. Bounce off Obstacles
        for (Entity obstacle : obstacles) { 
            if (obstacle == self || !obstacle.isActive()) {
                continue;
            }

            // Check overlap using the Entity's hitbox
            if (self.getHitbox().overlaps(obstacle.getHitbox())) { 
                velocityX *= -1;
                velocityY *= -1;

                // Simple correction to prevent getting stuck inside the obstacle
                // Clamps the position to safe bounds
                float correctedX = Math.max(0, Math.min(self.getX() + velocityX, Gdx.graphics.getWidth() - self.getWidth()));
                float correctedY = Math.max(0, Math.min(self.getY() + velocityY, Gdx.graphics.getHeight() - self.getHeight()));
                self.setX(correctedX);
                self.setY(correctedY);
                return;
            }
        }
    }
}