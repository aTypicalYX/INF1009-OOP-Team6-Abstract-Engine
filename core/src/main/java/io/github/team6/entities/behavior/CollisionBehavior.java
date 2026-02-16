package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;
/*
CollisionBehaviour is an interface that defines the behavior of an entity when it collides with another entity.
It has a single method, onCollision, which takes two parameters: the entity that is colliding (self) and the entity that is being collided with (other).
This interface can be implemented by any entity that needs to define specific behavior when it collides with
*/
public interface CollisionBehavior {
    // Method signature: defines an abstract method that implementing classes must provide
    // This method is called when a collision occurs between 'self' and 'other' entities
    void onCollision(Entity self, Entity other);
}
