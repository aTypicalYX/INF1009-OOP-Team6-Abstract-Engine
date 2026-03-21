package io.github.team6.mathgame;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;
import io.github.team6.inputoutput.AudioSource;
import io.github.team6.managers.OutputManager;

/**
 * PowerUpCollisionBehavior
 * Applied to each power-up entity. When the player rocket overlaps it,
 * this behavior applies the power-up effect, plays a sound, and
 * deactivates the power-up so it is removed from the world.
 *
 * OOP Concepts:
 * - Strategy Pattern: Implements CollisionBehavior, injected at spawn time.
 * - Singleton (GameStateManager): all state mutations go through the singleton.
 * - Open/Closed: new PowerUpType values require only a new case here.
 */
public class PowerUpCollisionBehavior implements CollisionBehavior {

    private static final int TIME_EXTENSION_SECONDS = 15;

    private final PowerUpType     type;
    private final MathGameScene   scene;
    private final OutputManager   outputManager;
    private final String          soundPath;

    /**
     * @param type          Which effect to apply.
     * @param scene         Back-reference for equation refresh.
     * @param outputManager Used to play the power-up SFX.
     * @param soundPath     Asset path for the power-up sound (or null).
     */
    public PowerUpCollisionBehavior(PowerUpType type, MathGameScene scene,
                                     OutputManager outputManager, String soundPath) {
        this.type          = type;
        this.scene         = scene;
        this.outputManager = outputManager;
        this.soundPath     = soundPath;
    }

    // When the player collides with the power-up, apply the effect and play the sound.
    @Override
    public void onCollision(Entity self, Entity other) {
        if (!"PLAYER".equals(other.getTag())) return;

        // Play unique sound per power-up type
        if (soundPath != null) {
            try {
                AudioSource sfx = new AudioSource(soundPath);
                sfx.setVolume(0.4f);
                outputManager.play(sfx);
            } catch (Exception e) {
                System.out.println("[PowerUp] Sound not found: " + soundPath);
            }
        }

        // Apply the power-up effect by mutating shared game state through the singleton manager
        GameStateManager gsm = GameStateManager.getInstance();

        // Each power-up type has a different effect on the game state, and we display a floating text to give feedback to the player.
        switch (type) {
            case TIME_EXTENSION:
                gsm.addTime(TIME_EXTENSION_SECONDS);
                scene.spawnFloatingText("+" + TIME_EXTENSION_SECONDS + "s!",
                    self.getX(), self.getY() + 40,
                    com.badlogic.gdx.graphics.Color.CYAN);
                break;

            case EXTRA_LIFE:
                gsm.addLife();
                scene.spawnFloatingText("+1 Life!",
                    self.getX(), self.getY() + 40,
                    com.badlogic.gdx.graphics.Color.GREEN);
                break;

            case SCORE_MULTIPLIER:
                gsm.activateScoreMultiplier();
                scene.spawnFloatingText("2× Score Next!",
                    self.getX(), self.getY() + 40,
                    com.badlogic.gdx.graphics.Color.YELLOW);
                break;
        }

        // Remove power-up from world
        self.setActive(false);
    }
}
