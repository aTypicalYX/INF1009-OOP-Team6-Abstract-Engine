package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public class DisappearOnCollisionBehavior implements CollisionBehavior {
    @Override
    public void onCollision(Entity self) {
        self.setActive(false);
    }
}
