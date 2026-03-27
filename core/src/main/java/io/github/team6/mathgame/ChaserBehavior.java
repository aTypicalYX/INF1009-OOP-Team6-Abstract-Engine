package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.MovementBehavior;

/**
 * ChaserBehavior
 * MovementBehavior for the chaser entity (black hole / lava).
 * * Uses "Rubber-Banding" logic: The base speed scales with the level, 
 * but if the player gets too far ahead, the chaser multiplies its speed 
 * to catch up, creates sense of urgency.
 * 
 * OOP Concepts:
 * - Strategy Pattern: ChaserBehavior is one of potentially many MovementBehaviors that can be swapped in and out for different entities or even the same entity at different times.
 *  The Chaser entity simply calls its assigned MovementBehavior's move() method without needing to know the details of how it calculates movement.
 * - Single Responsibility: Only moves the entity upward. All game-over logic is handled by ChaserCollisionBehavior.
 * - Open/Closed: Adding a new chase pattern (e.g. sinusoidal) requires
 *   only a new class implementing MovementBehavior - no entity changes.
 * - Encapsulation: The internal logic of how the speed is calculated and applied is hidden within this class, allowing for easy adjustments without affecting other parts of the codebase.
 */
public class ChaserBehavior implements MovementBehavior {

    private static final float BASE_SPEED             = 0.5f;
    private static final float LEVEL_INCREMENT        = 0.1f;
    private static final float MAX_SPEED              = 1.0f;   

    // Rubber-Band Tuning Constants
    private static final float RUBBER_BAND_DISTANCE   = 700f; // Roughly one screen height
    private static final float RUBBER_BAND_MULTIPLIER = 2.5f; // Max speed boost when catching up

    @Override
    public void move(Entity self, Entity target) {
        int level = GameStateManager.getInstance().getLevel();

        // 1. Calculate the normal speed for this level
        float currentSpeed = BASE_SPEED + (level - 1) * LEVEL_INCREMENT;

        // 2. Apply Rubber-Banding if the player is getting too far away
        if (target != null) {
            float distanceToPlayer = target.getY() - self.getY();
            
            if (distanceToPlayer > RUBBER_BAND_DISTANCE) {
                // The further away the player is, the faster the black hole goes
                float extraDistance = distanceToPlayer - RUBBER_BAND_DISTANCE;
                float catchUpFactor = 1.0f + (extraDistance * 0.005f); // Smooth acceleration curve
                
                // Multiply speed, but cap the multiplier so it doesn't instantly teleport
                currentSpeed *= Math.min(catchUpFactor, RUBBER_BAND_MULTIPLIER);
            }
        }

        // 3. Set maximum speed cap so it's not too fast
        currentSpeed = Math.min(currentSpeed, MAX_SPEED);

        // Frame-rate independent movement
        float dt = Gdx.graphics.getDeltaTime();
        float actualSpeed = currentSpeed * 150f * dt;


        self.setY(self.getY() + actualSpeed);
    }
}