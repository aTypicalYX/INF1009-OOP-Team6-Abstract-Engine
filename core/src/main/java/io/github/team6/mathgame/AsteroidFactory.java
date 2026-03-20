package io.github.team6.mathgame;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.math.Rectangle;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;

/**
 * AsteroidFactory – Factory Pattern (Director / Coordinator)
 *
 * Responsible for:
 * 1. Deciding WHICH concrete factory to use for each asteroid
 * (chasing vs stationary) – the Factory Pattern decision layer.
 * 2. Calculating a SAFE spawn position ABOVE the player, ensuring asteroids
 * appear dynamically as the player scrolls upward, without overlapping.
 * 3. Delegating the actual object construction to the appropriate
 * {@link IAsteroidFactory} implementation – the Abstract Factory
 * Pattern delegation layer.
 *
 * Pattern relationships:
 * MathGameScene  ──creates──▶  AsteroidFactory (Factory)
 * │ delegates to
 * ┌────────────┴────────────────┐
 * ChasingAsteroidFactory      StationaryAsteroidFactory
 * (Abstract Factory impl)     (Abstract Factory impl)
 *
 * GameStateManager (Singleton) is NOT touched here; it is accessed
 * inside NumberCollectionBehavior at collision time.
 */
public class AsteroidFactory {

    // -------------------------------------------------------------------
    // Constants – Vertical Spawn Window
    // -------------------------------------------------------------------

    /** Minimum pixel distance above the player's current Y for asteroid spawns. */
    private static final float VERTICAL_SPAWN_OFFSET_MIN = 400f;

    /** Maximum pixel distance above the player's current Y for asteroid spawns. */
    private static final float VERTICAL_SPAWN_OFFSET_MAX = 700f;

    // -------------------------------------------------------------------
    // Dependencies injected at construction time
    // -------------------------------------------------------------------
    private final float mapWidth;
    private final float mapHeight;
    private final PlayableEntity targetRocket;
    private final List<Entity> obstaclesList;
    private final EquationGenerator equationGenerator;
    private final MathGameScene scene;
    private final List<Rectangle> worldColliders;
    

    // Abstract Factory – concrete implementations chosen at construction
    private final IAsteroidFactory chasingFactory;
    private final IAsteroidFactory stationaryFactory;

    /**
     * Constructor – receives all environment context via Dependency Injection.
     *
     * @param mapWidth        World width in pixels.
     * @param mapHeight       World height in pixels.
     * @param target          The player rocket.
     * @param obstaclesList   Already-placed asteroids (overlap + steering).
     * @param generator       Shared EquationGenerator.
     * @param scene           Back-reference for score/life callbacks.
     * @param worldColliders  Map colliders (e.g., walls).
     */
    public AsteroidFactory(float mapWidth, float mapHeight,
                           PlayableEntity target,
                           List<Entity> obstaclesList,
                           EquationGenerator generator,
                           MathGameScene scene,
                           List<Rectangle> worldColliders) {   
        this.mapWidth          = mapWidth;
        this.mapHeight         = mapHeight;
        this.targetRocket      = target;
        this.obstaclesList     = obstaclesList;
        this.equationGenerator = generator;
        this.scene             = scene;
        this.worldColliders    = worldColliders;

        // Concrete factory instances – never exposed to MathGameScene
        this.chasingFactory    = new ChasingAsteroidFactory(obstaclesList, target);
        this.stationaryFactory = new StationaryAsteroidFactory(target);
    }

    /**
     * Factory Method – the public creation API used by MathGameScene.
     *
     * 1. Finds a safe spawn position ABOVE the player (Vertical Scroller).
     * 2. Randomly picks chasing or stationary factory (50 / 50).
     * 3. Delegates construction to the chosen IAsteroidFactory.
     *
     * @param numberValue The answer number displayed on the asteroid.
     * @param size        Sprite width/height in pixels.
     * @param speed       Base movement speed (stationary factory ignores this).
     * @return Fully constructed, ready-to-add NonPlayableEntity.
     */
    public NonPlayableEntity createAsteroid(int numberValue, float size, float speed) {

        // Step 1 – safe spawn position in a vertical window above the player
        float[] pos = getSafeSpawnPosition(size, size);

        // Step 2 – pick concrete factory (Abstract Factory pattern decision)
        IAsteroidFactory chosenFactory =
            ThreadLocalRandom.current().nextBoolean() ? chasingFactory : stationaryFactory;

        // Step 3 – delegate the actual object construction
        return chosenFactory.createAsteroid(
            numberValue, size, speed,
            pos[0], pos[1],
            equationGenerator, scene
        );
    }

    // -------------------------------------------------------------------
    // Private helper – Vertical Window spawn algorithm
    // -------------------------------------------------------------------

    /**
     * Generates a candidate position within the vertical window [Y + 400, Y + 700]
     * above the player's centre, verifying it does not overlap the rocket or any
     * existing asteroid. Falls back gracefully after 50 failed attempts.
     */
    private float[] getSafeSpawnPosition(float width, float height) {
        float maxX = Math.max(0, mapWidth - width);
        
        // 1. Define the vertical "Spawn Window" relative to the rocket
        // The viewport is 720p tall, so MIN offset puts it just above the top edge of the screen.
        float spawnMinY = targetRocket.getY() + VERTICAL_SPAWN_OFFSET_MIN; 
        float spawnMaxY = targetRocket.getY() + VERTICAL_SPAWN_OFFSET_MAX; 

        // 2. Clamp the maximum Y to the map boundaries so we don't spawn outside the world
        float mapMaxY = Math.max(0, mapHeight - height);
        spawnMaxY = Math.min(spawnMaxY, mapMaxY);

        // 3. Edge Case: If the player is at the very top of the map, collapse the window
        if (spawnMinY > spawnMaxY) {
            spawnMinY = spawnMaxY; 
        }

        // Try up to 50 times to find a random spot that doesn't overlap anything
        for (int attempt = 0; attempt < 50; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            
            // Generate Y within our new vertical window
            float randomY;
            if (spawnMinY == spawnMaxY) {
                randomY = spawnMinY; // Force it to the top if we are out of map space
            } else {
                randomY = ThreadLocalRandom.current().nextFloat() * (spawnMaxY - spawnMinY) + spawnMinY;
            }

            // Check intersection with the player
            boolean hitsPlayer = randomX < targetRocket.getX() + targetRocket.getWidth()
                    && randomX + width > targetRocket.getX()
                    && randomY < targetRocket.getY() + targetRocket.getHeight()
                    && randomY + height > targetRocket.getY();

            // Check intersection with other asteroids to prevent overlaps
            boolean hitsOtherAsteroids = false;
            for (Entity obstacle : obstaclesList) {
                if (randomX < obstacle.getX() + obstacle.getWidth()
                        && randomX + width > obstacle.getX()
                        && randomY < obstacle.getY() + obstacle.getHeight()
                        && randomY + height > obstacle.getY()) {
                    hitsOtherAsteroids = true;
                    break; 
                }
            }

            // If the spot is entirely clear, return the coordinates!
            if (!hitsPlayer && !hitsOtherAsteroids) {
                return new float[] { randomX, randomY };
            }
        }
        
        // Fallback coordinate if the map is too crowded
        return new float[] { 0, spawnMinY }; 
    }
}