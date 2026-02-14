package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

public class BouncingAroundDropletsMovementBehavior implements MovementBehavior {
    private float velocityX;
    private float velocityY;
    private List<NonPlayableEntity> obstacles;

    public BouncingAroundDropletsMovementBehavior(float speed, List<NonPlayableEntity> obstacles) {
        this.velocityX = speed;
        this.velocityY = speed;
        this.obstacles = obstacles;
    }

    @Override
    public void move(Entity self, Entity target) {
        float nextX = self.getX() + velocityX;
        float nextY = self.getY() + velocityY;

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

        for (NonPlayableEntity obstacle : obstacles) {
            if (obstacle == self || !obstacle.isActive()) {
                continue;
            }

            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                velocityX *= -1;
                velocityY *= -1;

                float correctedX = Math.max(0, Math.min(self.getX() + velocityX, Gdx.graphics.getWidth() - self.getWidth()));
                float correctedY = Math.max(0, Math.min(self.getY() + velocityY, Gdx.graphics.getHeight() - self.getHeight()));
                self.setX(correctedX);
                self.setY(correctedY);
                return;
            }
        }
    }
}
