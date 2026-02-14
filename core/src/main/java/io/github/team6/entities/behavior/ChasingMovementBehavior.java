package io.github.team6.entities.behavior;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

public class ChasingMovementBehavior implements MovementBehavior {
    private List<NonPlayableEntity> obstacles;

    public ChasingMovementBehavior(List<NonPlayableEntity> obstacles) {
        this.obstacles = obstacles;
    }

    @Override
    public void move(Entity self, Entity target) {
        if (target == null) {
            return;
        }

        Vector2 chaseDirection = new Vector2(target.getX() - self.getX(), target.getY() - self.getY());
        if (chaseDirection.isZero()) {
            return;
        }
        chaseDirection.nor();

        Vector2 avoidDirection = new Vector2(0, 0);
        for (NonPlayableEntity obstacle : obstacles) {
            if (obstacle == self || !obstacle.isActive()) {
                continue;
            }

            Vector2 fromObstacle = new Vector2(self.getX() - obstacle.getX(), self.getY() - obstacle.getY());
            float distance = fromObstacle.len();
            float influenceRadius = Math.max(self.getWidth(), self.getHeight()) + Math.max(obstacle.getWidth(), obstacle.getHeight());

            if (distance > 0 && distance < influenceRadius) {
                fromObstacle.nor();
                float strength = (influenceRadius - distance) / influenceRadius;
                avoidDirection.add(fromObstacle.scl(strength));
            }

            if (self.getHitbox().overlaps(obstacle.getHitbox())) {
                Vector2 tangent = new Vector2(-(target.getY() - self.getY()), target.getX() - self.getX());
                if (!tangent.isZero()) {
                    tangent.nor();
                    avoidDirection.add(tangent.scl(1.2f));
                }
            }
        }

        Vector2 finalDirection = new Vector2(chaseDirection);
        if (!avoidDirection.isZero()) {
            avoidDirection.nor();
            finalDirection.add(avoidDirection.scl(1.35f));
        }

        if (finalDirection.isZero()) {
            return;
        }

        finalDirection.nor();
        float newX = self.getX() + finalDirection.x * self.getSpeed();
        float newY = self.getY() + finalDirection.y * self.getSpeed();

        newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - self.getWidth()));
        newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - self.getHeight()));

        self.setX(newX);
        self.setY(newY);
    }
}
