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
 *  1. Deciding WHICH concrete factory to use for each asteroid
 *     (chasing vs stationary) – the Factory Pattern decision layer.
 *  2. Calculating a SAFE spawn position near the player, so asteroids
 *     never appear too far away to be found, but never directly on top
 *     of the rocket.
 *  3. Delegating the actual object construction to the appropriate
 *     {@link IAsteroidFactory} implementation – the Abstract Factory
 *     Pattern delegation layer.
 *
 * Pattern relationships:
 *   MathGameScene  ──creates──▶  AsteroidFactory (Factory)
 *                                      │ delegates to
 *                         ┌────────────┴────────────────┐
 *                 ChasingAsteroidFactory      StationaryAsteroidFactory
 *                 (Abstract Factory impl)     (Abstract Factory impl)
 *
 * GameStateManager (Singleton) is NOT touched here; it is accessed
 * inside NumberCollectionBehavior at collision time.
 */
public class AsteroidFactory {

    // -------------------------------------------------------------------
    // Constants – spawn ring around the player
    // -------------------------------------------------------------------

    /** Minimum pixel distance from the player centre for asteroid spawns. */
    private static final float MIN_SPAWN_RADIUS = 400f;

    /** Maximum pixel distance from the player centre for asteroid spawns. */
    private static final float MAX_SPAWN_RADIUS = 700f;

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
     * 1. Finds a safe spawn position NEAR the player.
     * 2. Randomly picks chasing or stationary factory (50 / 50).
     * 3. Delegates construction to the chosen IAsteroidFactory.
     *
     * @param numberValue The answer number displayed on the asteroid.
     * @param size        Sprite width/height in pixels.
     * @param speed       Base movement speed (stationary factory ignores this).
     * @return Fully constructed, ready-to-add NonPlayableEntity.
     */
    public NonPlayableEntity createAsteroid(int numberValue, float size, float speed) {

        // Step 1 – safe spawn position in a ring around the player
        float[] pos = getNearbySpawnPosition(size, size);

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
    // Private helper – ring-based near-player spawn algorithm
    // -------------------------------------------------------------------

    /**
     * Generates a candidate position within [MIN_SPAWN_RADIUS, MAX_SPAWN_RADIUS]
     * of the player's centre, verifying it does not overlap the rocket or any
     * existing asteroid.  Falls back gracefully after 50 failed attempts.
     */
    private float[] getNearbySpawnPosition(float width, float height) {
    ThreadLocalRandom rand = ThreadLocalRandom.current();

    float rocketCX = targetRocket.getX() + targetRocket.getWidth()  / 2f;
    float rocketCY = targetRocket.getY() + targetRocket.getHeight() / 2f;

    // Define safe vertical spawn band — keep asteroids away from floor and ceiling tiles
    float floorBuffer   = 160f;  // tiles at the bottom, don't spawn below this Y
    float ceilingBuffer = 100f;  // tiles at the top, don't spawn above this Y from top
    float minY = floorBuffer;
    float maxY = mapHeight - ceilingBuffer - height;

    for (int attempt = 0; attempt < 100; attempt++) {
        double angle  = rand.nextDouble() * 2.0 * Math.PI;
        float  radius = MIN_SPAWN_RADIUS + rand.nextFloat() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

        float cx = rocketCX + (float)(Math.cos(angle) * radius) - width  / 2f;
        float cy = rocketCY + (float)(Math.sin(angle) * radius) - height / 2f;

        // Clamp within map bounds with floor/ceiling buffers
        cx = Math.max(0, Math.min(cx, mapWidth  - width));
        cy = Math.max(minY, Math.min(cy, maxY));         // <-- key change

        // Reject if overlaps player sprite
        if (   cx < targetRocket.getX() + targetRocket.getWidth()
            && cx + width  > targetRocket.getX()
            && cy < targetRocket.getY() + targetRocket.getHeight()
            && cy + height > targetRocket.getY()) {
            continue;
        }

        // Reject if overlaps existing asteroid
        boolean hitsOther = false;
        for (Entity obstacle : obstaclesList) {
            if (!obstacle.isActive()) continue;
            if (   cx < obstacle.getX() + obstacle.getWidth()
                && cx + width  > obstacle.getX()
                && cy < obstacle.getY() + obstacle.getHeight()
                && cy + height > obstacle.getY()) {
                hitsOther = true;
                break;
            }
        }

        // Reject if overlaps a wall collider
        boolean hitsWall = false;
        for (Rectangle wall : worldColliders) {
            if (   cx < wall.x + wall.width  && cx + width  > wall.x
                && cy < wall.y + wall.height && cy + height > wall.y) {
                hitsWall = true;
                break;
            }
        }

        if (!hitsOther && !hitsWall) {
            return new float[]{ cx, cy };
        }
    }

    // Fallback: place near player but within safe Y band
    return new float[]{
        Math.max(0, Math.min(rocketCX + MIN_SPAWN_RADIUS, mapWidth - width)),
        Math.max(minY, Math.min(rocketCY, maxY))
        };
    }
}
