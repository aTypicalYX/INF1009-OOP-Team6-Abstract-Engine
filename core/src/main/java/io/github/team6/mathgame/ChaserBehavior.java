package io.github.team6.mathgame;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.MovementBehavior;

/**
 * ChaserBehavior
 * MovementBehavior for the chaser entity (black hole / lava).
 * Moves the entity upward at a base speed that is scaled by the current level.
 *
 * Speed formula: baseSpeed + (level - 1) * SPEED_INCREMENT
 * This means the chaser gets faster as the player answers more equations.
 *
 * OOP Concepts:
 * - Strategy Pattern: Implements MovementBehavior — swappable at construction
 *   time. The ChaserEntity does not know how it moves, only that it does.
 * - Single Responsibility: Only moves the entity upward. All game-over
 *   logic is handled by ChaserCollisionBehavior.
 * - Open/Closed: Adding a new chase pattern (e.g. sinusoidal) requires
 *   only a new class implementing MovementBehavior — no entity changes.
 */
public class ChaserBehavior implements MovementBehavior {

    private static final float BASE_SPEED        = 0.4f;
    private static final float LEVEL_INCREMENT   = 0.10f;
    private static final float TIME_ACCELERATION = 0.004f; // extra speed per second elapsed
    private static final float MAX_SPEED         = 3.0f;   // hard cap so it never becomes impossible

    @Override
    public void move(Entity self, Entity target) {
        int   level   = GameStateManager.getInstance().getLevel();
        float elapsed = GameStateManager.STARTING_TIME
                        - GameStateManager.getInstance().getTimeSeconds();

        float currentSpeed = BASE_SPEED
                           + (level - 1) * LEVEL_INCREMENT
                           + elapsed * TIME_ACCELERATION;

        // Clamp so the chaser never becomes impossibly fast
        currentSpeed = Math.min(currentSpeed, MAX_SPEED);

        self.setY(self.getY() + currentSpeed);
    }
}
