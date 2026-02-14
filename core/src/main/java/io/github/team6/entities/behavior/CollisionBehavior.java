package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public interface CollisionBehavior {
    void onCollision(Entity self);
}
