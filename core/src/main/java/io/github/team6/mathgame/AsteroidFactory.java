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
 * 3. Delegating the actual object construction to the appropriate IAsteroidFactory implementation – the Abstract Factory
 * Pattern delegation layer.
 *
 * Pattern relationships:
 * MathGameScene  AsteroidFactory (Factory) then delegates ChasingAsteroidFactory StationaryAsteroidFactory
 */
public class AsteroidFactory {

   // -------------------------------------------------------------------
    // Constants – Vertical Spawn Window
    // -------------------------------------------------------------------

    /** Minimum pixel distance above the player's current Y for asteroid spawns. */
    private static final float VERTICAL_SPAWN_OFFSET_MIN = 300f;   // min px above rocket
    private static final float VERTICAL_SPAWN_OFFSET_MAX = 700f;   // max px above rocket (within ~720px viewport)

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

    // Constructor – receives all environment context via Dependency Injection
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
     */
    public NonPlayableEntity createAsteroid(int numberValue, float size, float speed) {
        // Step 1 – safe spawn position in a vertical window above the player
        float[] pos = getScreenSpaceSpawnPosition(size, size);

        // Step 2 – pick concrete factory (Abstract Factory pattern decision)
        IAsteroidFactory chosenFactory =
            ThreadLocalRandom.current().nextBoolean() ? chasingFactory : stationaryFactory;

        // Step 3 – delegate the actual object construction
        NonPlayableEntity asteroid = chosenFactory.createAsteroid(
            numberValue, size, speed,
            pos[0], pos[1],
            equationGenerator, scene
        );

        // --- Tumbling Asteroids ---
        // Generate a random spin between -150 and +150 degrees per second.
        // Negative = clockwise spin. Positive = counter-clockwise spin.
        float randomSpin = ThreadLocalRandom.current().nextFloat() * 300f - 150f;
        asteroid.setRotationSpeed(randomSpin);
        // -------------------------------------

        return asteroid;
    }

    // -------------------------------------------------------------------
    // Private helper – Vertical Window spawn algorithm
    // -------------------------------------------------------------------

    /**
     * Spawns within the visible viewport above the rocket.
     * X: anywhere across the full map width.
     * Y: between VERTICAL_SPAWN_OFFSET_MIN and VERTICAL_SPAWN_OFFSET_MAX pixels above the rocket.
     *
     * Falls back gracefully after 50 attempts if all candidate positions overlap existing asteroids.
     */
    private float[] getScreenSpaceSpawnPosition(float width, float height) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        float rocketY = targetRocket.getY();

        // Y band: directly above the rocket within the visible screen
        float minY = rocketY + VERTICAL_SPAWN_OFFSET_MIN;
        float maxY = rocketY + VERTICAL_SPAWN_OFFSET_MAX;
        // Clamp to map bounds
        maxY = Math.min(maxY, mapHeight - height - 60f);
        minY = Math.min(minY, maxY);

        for (int attempt = 0; attempt < 50; attempt++) {
            float cx = rand.nextFloat() * (mapWidth - width);
            float cy = minY + rand.nextFloat() * Math.max(1f, maxY - minY);

            // Reject if too close to the rocket
            if (overlapsRocket(cx, cy, width, height)) continue;

            // Reject if overlaps an existing asteroid
            if (overlapsObstacle(cx, cy, width, height)) continue;

            // Reject if overlaps a wall tile
            if (overlapsWall(cx, cy, width, height)) continue;

            return new float[]{ cx, cy };
        }

        // Fallback: place just above the rocket centre
        float fallbackX = Math.max(0,
            Math.min(targetRocket.getX(), mapWidth - width));
        float fallbackY = Math.min(rocketY + VERTICAL_SPAWN_OFFSET_MAX - 50f, mapHeight - height);
        return new float[]{ fallbackX, fallbackY };
    }

    private boolean overlapsRocket(float cx, float cy, float w, float h) {
        return cx < targetRocket.getX() + targetRocket.getWidth()
            && cx + w > targetRocket.getX()
            && cy < targetRocket.getY() + targetRocket.getHeight()
            && cy + h > targetRocket.getY();
    }

    private boolean overlapsObstacle(float cx, float cy, float w, float h) {
        for (Entity obs : obstaclesList) {
            if (!obs.isActive()) continue;
            if (cx < obs.getX() + obs.getWidth()
                    && cx + w > obs.getX()
                    && cy < obs.getY() + obs.getHeight()
                    && cy + h > obs.getY()) {
                return true;
            }
        }
        return false;
    }

    private boolean overlapsWall(float cx, float cy, float w, float h) {
        for (Rectangle wall : worldColliders) {
            if (cx < wall.x + wall.width  && cx + w > wall.x
                    && cy < wall.y + wall.height && cy + h > wall.y) {
                return true;
            }
        }
        return false;
    }
}