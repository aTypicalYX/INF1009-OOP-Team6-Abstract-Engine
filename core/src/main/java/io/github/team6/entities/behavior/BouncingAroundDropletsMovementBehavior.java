package io.github.team6.entities.behavior;
import java.util.List;
import com.badlogic.gdx.Gdx;
import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

/*
This class implements a movement behavior where an entity moves with a constant velocity,
bouncing off the edges of the screen and reversing direction upon colliding with specified obstacles.
The entity continuously "bounces around" within the screen bounds, ignoring any target entities. 
*/

public class BouncingAroundDropletsMovementBehavior implements MovementBehavior {
    private float velocityX;
    private float velocityY;
    private List<NonPlayableEntity> obstacles;

    // Initialize movement speed and set obstacles list
    public BouncingAroundDropletsMovementBehavior(float speed, List<NonPlayableEntity> obstacles) {
        this.velocityX = speed;
        this.velocityY = speed;
        this.obstacles = obstacles;
    }

    // Moves entity by updating position; bounces on edges and obstacles
    @Override
    public void move(Entity self, Entity target) { // Target is ignored for this behavior, as it just bounces around
        // Calculate next projected position
        float nextX = self.getX() + velocityX;
        float nextY = self.getY() + velocityY;

        if (nextX < 0 || nextX > Gdx.graphics.getWidth() - self.getWidth()) { // Bounce off left and right edges
            velocityX *= -1;
            nextX = self.getX() + velocityX;
        }

        if (nextY < 0 || nextY > Gdx.graphics.getHeight() - self.getHeight()) { // Bounce off top and bottom edges
            velocityY *= -1;
            nextY = self.getY() + velocityY;
        }

        self.setX(nextX); // Update X position
        self.setY(nextY); // Update Y position

        for (NonPlayableEntity obstacle : obstacles) { // Check collision with each obstacle and bounce if overlapped
            if (obstacle == self || !obstacle.isActive()) {
                continue;
            }

            if (self.getHitbox().overlaps(obstacle.getHitbox())) { // When collide with obstacle, direction will be reversed
                velocityX *= -1;
                velocityY *= -1;

                // Correct position to stay within screen after bounce
                float correctedX = Math.max(0, Math.min(self.getX() + velocityX, Gdx.graphics.getWidth() - self.getWidth()));
                float correctedY = Math.max(0, Math.min(self.getY() + velocityY, Gdx.graphics.getHeight() - self.getHeight()));
                self.setX(correctedX);
                self.setY(correctedY);
                return;
            }
        }
    }
}