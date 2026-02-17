package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public interface MovementBehavior {
    // This method is responsible for updating the position of the 'self' entity based on the 'target' entity
    // This move() method is called by the Entity's update() method, which is called every frame by the SceneManager
    void move(Entity self, Entity target);
}
