package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public interface CollisionBehavior {
    // Method signature: defines an abstract method that implementing classes must provide
    // This method is called when a collision occurs between 'self' and 'other' entities
    void onCollision(Entity self, Entity other);
}
