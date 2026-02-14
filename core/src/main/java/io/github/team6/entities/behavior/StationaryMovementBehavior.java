package io.github.team6.entities.behavior;

import io.github.team6.entities.Entity;

public class StationaryMovementBehavior implements MovementBehavior {
    @Override
    public void move(Entity self, Entity target) {
        // Intentionally empty: this droplet does not move.
    }
}
