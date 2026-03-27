package io.github.team6.mathgame;

import java.util.List;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.ChasingMovementBehavior;

/**
 * ChasingAsteroidFactory – Concrete Factory (Abstract Factory Pattern)
 *
 * Produces asteroids that actively chase the player using ChasingMovementBehavior.  
 * Design Pattern: Abstract Factory (Concrete Product Creator)
 * - Implements IAsteroidFactory.
 * - The caller (AsteroidFactory / MathGameScene) only knows the
 *   interface; it never imports this class directly.
 */
public class ChasingAsteroidFactory implements IAsteroidFactory {

    /** Obstacles list shared with ChasingMovementBehavior for avoidance steering. */
    private final List<Entity> obstaclesList;

    /** The rocket target that chasing asteroids will move towards. */
    private final PlayableEntity targetRocket;

    public ChasingAsteroidFactory(List<Entity> obstaclesList, PlayableEntity targetRocket) {
        this.obstaclesList = obstaclesList;
        this.targetRocket  = targetRocket;
    }

    /**
     * Builds a chasing asteroid entity.
     * The asteroid's movement uses ChasingMovementBehavior meaning it will continuously pursue the player each frame. 
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
            speed,
            size, size,
            "ASTEROID_" + numberValue,
            new ChasingMovementBehavior(obstaclesList),
            new NumberCollectionBehavior(equationGenerator, numberValue, scene),
            targetRocket
        );
    }
}
