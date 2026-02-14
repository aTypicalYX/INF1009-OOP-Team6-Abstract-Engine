package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public interface MovementBehavior {
    void move(Entity self, Entity target);
}
