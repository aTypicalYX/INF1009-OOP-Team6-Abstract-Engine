package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public interface MovementBehavior {
    // Method signature: defines an abstract method that implementing classes must provide
    // This method is responsible for updating the position of the 'self' entity based on the 'target' entity
    void move(Entity self, Entity target);
}
