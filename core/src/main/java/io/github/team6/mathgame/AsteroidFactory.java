package io.github.team6.mathgame;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.ChasingMovementBehavior;

/**
 * Factory Design Pattern:
 * The Factory Pattern centralizes the complex creation logic of objects. 
 * Instead of the MathGameScene knowing *how* to build an asteroid, calculate its safe spawn location, 
 * and attach its specific movement and collision behaviors, the scene just asks the Factory: "Give me an asteroid."
 * * Benefits:
 * - Low Coupling: Keeps the MathGameScene cleaner and easier to read.
 * - Reusability: If you want to spawn asteroids in other scenes or modes later, you just use this factory.
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
        
        // 2. Construct and return the entity
        return new NonPlayableEntity(
            "asteroid.png", pos[0], pos[1], speed,
            size, size, "ASTEROID_" + numberValue, // We embed the number in the tag for rendering
            new ChasingMovementBehavior(obstaclesList), // Inject Strategy: How it moves
            new NumberCollectionBehavior(equationGenerator, numberValue, scene), // Inject Strategy: What happens on impact
            targetRocket
        );
    }

    /**
     * Helper algorithm to find a safe X/Y spawn coordinate.
     * It uses an Axis-Aligned Bounding Box (AABB) collision check against the player's current position.
     */
    private float[] getSafeSpawnPosition(float width, float height) {
        // Define the maximum bounds to keep the asteroid within the map
        float maxX = Math.max(0, mapWidth - width);
        float maxY = Math.max(0, mapHeight - height);

        // Try up to 20 times to find a random spot that doesn't hit the player
        for (int attempt = 0; attempt < 20; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            float randomY = ThreadLocalRandom.current().nextFloat() * maxY;

            // Simple rectangle intersection logic
            boolean intersects = randomX < targetRocket.getX() + targetRocket.getWidth()
                    && randomX + width > targetRocket.getX()
                    && randomY < targetRocket.getY() + targetRocket.getHeight()
                    && randomY + height > targetRocket.getY();

            // If it doesn't intersect, it's safe! Return the coordinates.
            if (!intersects) return new float[] { randomX, randomY };
        }
        
        // Fallback coordinate if the algorithm fails (e.g., if the map is too small)
        return new float[] { 0, 0 };
    }
}