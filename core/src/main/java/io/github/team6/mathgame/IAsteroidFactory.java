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
 * Abstract Factory is used here as we want to be able to swap entire families of asteroid behaviour,
 * (e.g. a future "frenzied" difficulty where ALL asteroids chase the player),
 * simply by changing which factory implementation is injected, without touching any scene or game-logic code.
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

    // Assembles and returns a fully initialised asteroid entity.
    NonPlayableEntity createAsteroid(int numberValue, // answer number
                                     float size, // width and height of the asteroid sprite
                                     float speed, // movement speed in world units per frame
                                     float spawnX, // pre-calculated safe X coordinate
                                     float spawnY, // pre-calculated safe Y coordinate
                                     EquationGenerator equationGenerator, // shared generator for collision behavior
                                     MathGameScene scene); // back-reference for score/life updates and floating text
}
