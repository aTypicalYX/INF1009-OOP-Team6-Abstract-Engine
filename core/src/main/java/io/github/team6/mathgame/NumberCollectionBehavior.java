package io.github.team6.mathgame;

import com.badlogic.gdx.graphics.Color;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;

/**
 * NumberCollectionBehavior
 *
 * Defines the collision response when the player rocket hits a numbered asteroid.
 * It checks whether the asteroid's number matches the current equation's answer,
 * then updates game state accordingly.
 *
 * OOP Concepts & Design Patterns used:
 *
 * 1. Strategy Pattern – implements CollisionBehavior, so asteroid collision
 *    logic is interchangeable without modifying Entity classes.
 *
 * 2. Singleton Pattern (GameStateManager) – all score, lives, and
 *    equations-answered counters live in the single GameStateManager
 *    instance.  This class reads/writes ONLY through that Singleton, so
 *    state is always consistent regardless of how many asteroids are active.
 *
 * 3. Single Responsibility – this class only handles "what happens when
 *    the player touches a numbered asteroid".
 */
public class NumberCollectionBehavior implements CollisionBehavior {

    private final EquationGenerator equationGenerator;
    private final int numberValue;
    private final MathGameScene scene;

    /**
     * @param generator   Shared EquationGenerator (to verify the answer).
     * @param numberValue The number displayed on this asteroid.
     * @param scene       Back-reference to MathGameScene for floating text
     *                    and round/scene transitions.
     */
    public NumberCollectionBehavior(EquationGenerator generator,
                                    int numberValue,
                                    MathGameScene scene) {
        this.equationGenerator = generator;
        this.numberValue       = numberValue;
        this.scene             = scene;
    }

    /**
     * Called by the collision system whenever 'self' (the asteroid) touches
     * another entity.  Only reacts when 'other' is the player (tag "PLAYER").
     *
     * Correct answer:
     *   - Awards points via GameStateManager (Singleton).
     *   - Increments the "equations answered" counter in the Singleton.
     *   - If the player has answered EQUATIONS_TO_WIN equations → triggers win.
     *   - Otherwise spawns a new round.
     *
     * Wrong answer:
     *   - Deducts a life via GameStateManager.
     *   - If lives reach 0 → triggers game over via MathGameScene.
     *   - Otherwise deactivates only this asteroid (others remain).
     */
    @Override
    public void onCollision(Entity self, Entity other) {
        if (!other.getTag().equals("PLAYER")) return;

        // Retrieve the single Singleton instance
        GameStateManager gsm = GameStateManager.getInstance();

        if (equationGenerator.checkAnswer(numberValue)) {
            // ---- CORRECT ANSWER ----------------------------------------
            scene.playCorrectAnswerSfx();
            gsm.addScore(GameStateManager.POINTS_PER_CORRECT);
            scene.spawnFloatingText("+10", self.getX(), self.getY() + 30, Color.GREEN);

            // Check win condition via Singleton
            boolean won = gsm.recordCorrectAnswer();
            if (won) {
                // Player has answered EQUATIONS_TO_WIN equations – they win!
                scene.triggerWin();
            } else {
                // Continue the game with a new equation
                scene.generateNewRound();
            }

        } else {
            // ---- WRONG ANSWER ------------------------------------------
            scene.playWrongAnswerSfx();
            boolean stillAlive = gsm.deductLife();
            scene.spawnFloatingText("-1 Life", self.getX(), self.getY() + 30, Color.RED);

            if (!stillAlive) {
                // All lives exhausted – game over
                scene.triggerGameOver();
            } else {
                // Deactivate only this wrong asteroid; keep the round going
                self.setActive(false);
            }
        }
    }
}
