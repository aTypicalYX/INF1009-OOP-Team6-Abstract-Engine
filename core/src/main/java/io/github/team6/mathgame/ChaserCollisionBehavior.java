package io.github.team6.mathgame;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;

/**
 * ChaserCollisionBehavior
 * Fires when the chaser entity (black hole / lava) overlaps the player rocket.
 * Instantly triggers game over - no life deduction, just immediate loss.
 *
 * OOP Concepts:
 * - Strategy Pattern: Implements CollisionBehavior, injected into ChaserEntity.
 * - Single Responsibility: Only handles the "chaser touched player" event.
 * - Singleton: Reads GameStateManager to set the game-over flag so that
 *   MathGameScene's update loop detects it on the next frame.
 */
public class ChaserCollisionBehavior implements CollisionBehavior {

    private final MathGameScene scene;

    public ChaserCollisionBehavior(MathGameScene scene) {
        this.scene = scene;
    }

    @Override
    public void onCollision(Entity self, Entity other) {
        if (!"PLAYER".equals(other.getTag())) return;

        // Drain all lives so game-over check in MathGameScene triggers
        GameStateManager gsm = GameStateManager.getInstance();
        while (gsm.getLives() > 0) {
            gsm.deductLife();
        }

        scene.triggerGameOver();
    }
}
