package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public class PermanentCollisionBehavior implements CollisionBehavior {
    @Override
    public void onCollision(Entity self) {
        // Intentionally empty: this entity remains after collision.
    }
}
