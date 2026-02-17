package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;

import io.github.team6.entities.Entity;

/*
 * Formerly: BouncingAroundDropletsMovementBehavior
 * RENAMED: To be generic. It's just a screen bounce logic now.
 * REFACTORED: Now accepts List<Entity> obstacles, making it usable for any entity type.
 */
public class ScreenBounceBehavior implements MovementBehavior {
    private float velocityX;
    private float velocityY;
    
    // PHASE 3 FIX: Use generic Entity list
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

        // 2. Bounce off Screen Edges
        if (nextX < 0 || nextX > Gdx.graphics.getWidth() - self.getWidth()) { 
            velocityX *= -1;
            nextX = self.getX() + velocityX;
        }

        if (nextY < 0 || nextY > Gdx.graphics.getHeight() - self.getHeight()) { 
            velocityY *= -1;
            nextY = self.getY() + velocityY;
        }

        self.setX(nextX);
        self.setY(nextY);

        // 3. Bounce off Obstacles
        for (Entity obstacle : obstacles) { 
            if (obstacle == self || !obstacle.isActive()) {
                continue;
            }

            if (self.getHitbox().overlaps(obstacle.getHitbox())) { 
                velocityX *= -1;
                velocityY *= -1;

                // Simple correction to prevent getting stuck inside the obstacle
                float correctedX = Math.max(0, Math.min(self.getX() + velocityX, Gdx.graphics.getWidth() - self.getWidth()));
                float correctedY = Math.max(0, Math.min(self.getY() + velocityY, Gdx.graphics.getHeight() - self.getHeight()));
                self.setX(correctedX);
                self.setY(correctedY);
                return;
            }
        }
    }
}