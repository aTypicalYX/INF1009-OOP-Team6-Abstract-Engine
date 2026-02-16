package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;

public class DieOnTouchBehaviour implements CollisionBehavior {
    @Override
    public void onCollision(Entity self, Entity other) {
        // Only die if we hit an enemy
        if (other instanceof NonPlayableEntity) {
            System.out.println("Player Hit! Game Over.");
            // Setting active to false will remove it from the EntityManager list
            // and signal the MainScene that the player is dead.
            self.setActive(false); 
        }
    }
}
