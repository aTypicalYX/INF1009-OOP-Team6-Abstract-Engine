package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public class DisappearOnCollisionBehavior implements CollisionBehavior {
    @Override
    public void onCollision(Entity self, Entity other) {
        // We don't care 'what' we hit, we just disappear.
        self.setActive(false);
    }
}
