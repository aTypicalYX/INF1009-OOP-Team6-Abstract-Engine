package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;
/*
CollisionBehaviour is an interface that defines the behavior of an entity when it collides with another entity.
It has a single method, onCollision, which takes two parameters: the entity that is colliding (self) and the entity that is being collided with (other).
This interface can be implemented by any entity that needs to define specific behavior when it collides with
*/
public interface CollisionBehavior {
    void onCollision(Entity self, Entity other);
}
