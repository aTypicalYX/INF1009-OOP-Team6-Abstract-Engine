package io.github.team6.entities.behavior;
import java.util.List;
import com.badlogic.gdx.Gdx;
import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

public class BouncingAroundDropletsMovementBehavior implements MovementBehavior {
    private float velocityX;
    private float velocityY;
    private List<NonPlayableEntity> obstacles;

    // Constructor: initializes the behavior with speed and obstacles
    public BouncingAroundDropletsMovementBehavior(float speed, List<NonPlayableEntity> obstacles) {
        this.velocityX = speed;
        this.velocityY = speed;
        this.obstacles = obstacles;
    }

    // Method override: implements the move method from MovementBehavior interface
    // This method updates the entity's position to bounce around the screen and off obstacles
    @Override
    public void move(Entity self, Entity target) {
        // Calculates the next horizontal position based on current position and velocity
        float nextX = self.getX() + velocityX;
        // Calculates the next vertical position based on current position and velocity
        float nextY = self.getY() + velocityY;

        // Checks if the next X position is out of bounds (left or right edge)
        if (nextX < 0 || nextX > Gdx.graphics.getWidth() - self.getWidth()) {
            // Reverses the horizontal velocity to bounce off the edge
            velocityX *= -1;
            // Recalculates nextX with the reversed velocity
            nextX = self.getX() + velocityX;
        }

        // Checks if the next Y position is out of bounds (top or bottom edge)
        if (nextY < 0 || nextY > Gdx.graphics.getHeight() - self.getHeight()) {
            // Reverses the vertical velocity to bounce off the edge
            velocityY *= -1;
            // Recalculates nextY with the reversed velocity
            nextY = self.getY() + velocityY;
        }

        // Sets the entity's new X position
        self.setX(nextX);
        // Sets the entity's new Y position
        self.setY(nextY);

        // Loops through each obstacle in the list
        for (NonPlayableEntity obstacle : obstacles) {
            // Skips if the obstacle is the entity itself or not active
            if (obstacle == self || !obstacle.isActive()) {
                // Continues to the next obstacle
                continue;
            }

            // Checks if the entity's hitbox overlaps with the obstacle's hitbox
            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                // Reverses both velocity components to bounce off the obstacle
                velocityX *= -1;
                velocityY *= -1;

                // Calculates a corrected X position, ensuring it stays within bounds
                float correctedX = Math.max(0, Math.min(self.getX() + velocityX, Gdx.graphics.getWidth() - self.getWidth()));
                // Calculates a corrected Y position, ensuring it stays within bounds
                float correctedY = Math.max(0, Math.min(self.getY() + velocityY, Gdx.graphics.getHeight() - self.getHeight()));
                // Sets the corrected X position
                self.setX(correctedX);
                // Sets the corrected Y position
                self.setY(correctedY);
                // Returns early to avoid further processing
                return;
            }
        }
    }
}
