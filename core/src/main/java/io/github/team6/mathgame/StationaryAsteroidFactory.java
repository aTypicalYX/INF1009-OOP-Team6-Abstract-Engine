package io.github.team6.mathgame;

import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.StationaryMovementBehavior;

/**
 * StationaryAsteroidFactory – Concrete Factory (Abstract Factory Pattern)
 *
 * Produces asteroids that remain in place and never chase the player.
 * These are easier to avoid but the player must actively navigate to the
 * correct one.  Using a second concrete factory makes it trivial to swap
 * or mix asteroid behaviour without changing any game-logic code.
 *
 * Design Pattern: Abstract Factory (Concrete Product Creator)
 * - Implements {@link IAsteroidFactory}.
 * - Speed is forced to 0 so the physics engine does not try to move it.
 */
public class StationaryAsteroidFactory implements IAsteroidFactory {

    /** The rocket is still stored as target so NonPlayableEntity stays well-formed. */
    private final PlayableEntity targetRocket;

    public StationaryAsteroidFactory(PlayableEntity targetRocket) {
        this.targetRocket = targetRocket;
    }

    /**
     * Builds a stationary asteroid entity.
     * Speed is clamped to 0 regardless of the value passed in, because a
     * StationaryMovementBehavior does not move the entity at all.
     */
    @Override
    public NonPlayableEntity createAsteroid(int numberValue,
                                             float size,
                                             float speed,
                                             float spawnX,
                                             float spawnY,
                                             EquationGenerator equationGenerator,
                                             MathGameScene scene) {
        return new NonPlayableEntity(
            "asteroid.png",
            spawnX, spawnY,
            0f,                                  // stationary – speed is always 0
            size, size,
            "ASTEROID_" + numberValue,
            new StationaryMovementBehavior(),
            new NumberCollectionBehavior(equationGenerator, numberValue, scene),
            targetRocket
        );
    }
}
