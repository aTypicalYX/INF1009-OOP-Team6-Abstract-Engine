package io.github.team6.mathgame;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.ChasingMovementBehavior;
import io.github.team6.entities.behavior.MovementBehavior;
import io.github.team6.entities.behavior.StationaryMovementBehavior;

/**
AsteroidFactory is responsible for creating asteroid entities with the correct configuration and behavior.
It uses the Factory design pattern to encapsulate the construction logic of asteroids, which includes determining spawn positions, assigning movement strategies, and setting up collision behaviors.
 */
public class AsteroidFactory {
    
    // Dependencies required to configure an asteroid correctly.
    private final float mapWidth;
    private final float mapHeight;
    private final PlayableEntity targetRocket; // The asteroid needs to know who to chase
    private final List<Entity> obstaclesList;  // The asteroid needs to know about others for flocking/movement
    private final EquationGenerator equationGenerator;
    private final MathGameScene scene;

    /**
     * Constructor using Dependency Injection.
     * We pass all the environment context (map size, player reference, etc.) into the factory once,
     * so it can reuse this context every time it builds a new asteroid.
     */
    public AsteroidFactory(float mapWidth, float mapHeight, PlayableEntity target, 
                           List<Entity> obstaclesList, EquationGenerator generator, MathGameScene scene) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.targetRocket = target;
        this.obstaclesList = obstaclesList;
        this.equationGenerator = generator;
        this.scene = scene;
    }

    /**
     * The core Factory method. 
     * Assembles all the pieces (Texture, Position, Speed, Tag, and Behaviors) to return a fully functional entity.
     * * @param numberValue The specific math number this asteroid represents
     * @param size Size of the asteroid sprite
     * @param speed How fast the asteroid should chase the player
     * @return A fully constructed NonPlayableEntity ready to be added to the EntityManager
     */
    public NonPlayableEntity createAsteroid(int numberValue, float size, float speed) {
        // 1. Calculate a spawn coordinate that doesn't overlap the player instantly
        float[] pos = getSafeSpawnPosition(size, size);
        
        // 2. The Asteroid has a 50% chance to be stationary (for variety), otherwise it will chase the player
        MovementBehavior movementStrategy;
        // Note: The movement strategy is determined at the moment of creation, allowing for dynamic behavior without changing the entity's class.
        if (ThreadLocalRandom.current().nextBoolean()) {
            movementStrategy = new ChasingMovementBehavior(obstaclesList);
        } else {
            movementStrategy = new StationaryMovementBehavior();
            speed = 0f; // Force speed to 0 so the physics engine doesn't try to move it
        }

        // 3. Construct and return the entity
        return new NonPlayableEntity(
            "asteroid.png", pos[0], pos[1], speed,
            size, size, "ASTEROID_" + numberValue, // We embed the number in the tag for rendering
            movementStrategy, // Inject Strategy: either chase or stationary
            new NumberCollectionBehavior(equationGenerator, numberValue, scene), // Inject Strategy: What happens on impact
            targetRocket
        );
    }

    /**
     * Helper algorithm to find a safe X/Y spawn coordinate.
     * It uses an Axis-Aligned Bounding Box (AABB) collision check against the player's current position.
     * This ensures that the asteroid doesn't spawn directly on top of the player, which would be unfair.
     * Additionally, it checks against existing asteroids to prevent overcrowding and ensure a fair spawn.
     */
    private float[] getSafeSpawnPosition(float width, float height) {
        float maxX = Math.max(0, mapWidth - width);
        float maxY = Math.max(0, mapHeight - height);

        // Increased to 50 attempts to ensure it finds a spot as the screen gets crowded
        for (int attempt = 0; attempt < 50; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            float randomY = ThreadLocalRandom.current().nextFloat() * maxY;

            // 1. Check intersection with the player
            boolean hitsPlayer = randomX < targetRocket.getX() + targetRocket.getWidth()
                    && randomX + width > targetRocket.getX()
                    && randomY < targetRocket.getY() + targetRocket.getHeight()
                    && randomY + height > targetRocket.getY();

            // 2. Check intersection with other asteroids
            boolean hitsOtherAsteroids = false;
            for (Entity obstacle : obstaclesList) {
                if (randomX < obstacle.getX() + obstacle.getWidth()
                        && randomX + width > obstacle.getX()
                        && randomY < obstacle.getY() + obstacle.getHeight()
                        && randomY + height > obstacle.getY()) {
                    hitsOtherAsteroids = true;
                    break; // Stop checking this coordinate, it's blocked
                }
            }

            // If it hits nothing, it's a safe spawn
            if (!hitsPlayer && !hitsOtherAsteroids) {
                return new float[] { randomX, randomY };
            }
        }
        return new float[] { 0, 0 };
    }
}