package io.github.team6.mathgame;

import io.github.team6.entities.NonPlayableEntity;

/**
 * IAsteroidFactory – Abstract Factory Pattern (Product interface)
 *
 * Defines the contract that every concrete asteroid factory MUST fulfil.
 * MathGameScene and AsteroidFactory talk ONLY to this interface; they
 * never depend on a specific implementation such as ChasingAsteroidFactory
 * or StationaryAsteroidFactory.
 *
 * Why Abstract Factory here?
 * We want to be able to swap entire "families" of asteroid behaviour
 * (e.g. a future "frenzied" difficulty where ALL asteroids chase the
 * player) simply by changing which factory implementation is injected,
 * without touching any scene or game-logic code.
 *
 * Relationship to the other patterns:
 * - AsteroidFactory (Factory Pattern) acts as the "director" and holds a
 *   reference to an IAsteroidFactory. It delegates the low-level object
 *   creation to whichever concrete factory it was given.
 * - GameStateManager (Singleton) is consulted by NumberCollectionBehavior
 *   to update shared game state on each collision.
 *
 * Design Pattern: Abstract Factory
 */
public interface IAsteroidFactory {

    /**
     * Assembles and returns a fully initialised asteroid entity.
     *
     * @param numberValue  The answer number displayed on the asteroid.
     * @param size         Width and height (square) of the sprite in pixels.
     * @param speed        Movement speed in world-units per frame.
     * @param spawnX       Pre-calculated safe X coordinate.
     * @param spawnY       Pre-calculated safe Y coordinate.
     * @param equationGenerator  Shared generator so the collision behavior
     *                           can verify the answer at impact time.
     * @param scene        Back-reference to MathGameScene used for score /
     *                     life updates and floating-text spawning.
     * @return A ready-to-use NonPlayableEntity (asteroid).
     */
    NonPlayableEntity createAsteroid(int numberValue,
                                     float size,
                                     float speed,
                                     float spawnX,
                                     float spawnY,
                                     EquationGenerator equationGenerator,
                                     MathGameScene scene);
}
