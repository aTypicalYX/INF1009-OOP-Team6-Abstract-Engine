
package io.github.team6.managers;

import java.util.List;

import io.github.team6.entities.Entity;

/**
 * MovementManager is responsible for applying physics/movement logic.
 */

public class MovementManager {
    public void update(List<Entity> entities) {
        for (Entity e : entities) {
            e.movement(); // Triggers the specific movement logic for that subclass
        }
    }
}
