package io.github.team6.mathgame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

import io.github.team6.entities.Entity;
import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.ResetOnTouchBehavior;
import io.github.team6.inputoutput.MusicSource;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.Scene;


/**
 * MathGameScene – core gameplay scene.
 *
 * The player controls a rocket and must collide with the asteroid that displays
 * the correct answer to the on-screen math equation.
 *
 * === Design Patterns Used ===
 *
 * 1. SINGLETON  (GameStateManager)
 *    A single, globally accessible object tracks lives, score, and the number
 *    of equations answered.  Every system that needs this data – the collision
 *    behaviour, the HUD renderer here, and the end-screens – all read/write the
 *    same instance, so state is always consistent.
 *    ▶ See: GameStateManager.getInstance()
 *
 * 2. FACTORY  (AsteroidFactory)
 *    MathGameScene asks AsteroidFactory.createAsteroid() for a ready-to-use
 *    NonPlayableEntity.  The scene never directly calls 'new NonPlayableEntity()'
 *    or decides which movement strategy to apply – that responsibility belongs
 *    entirely to the factory.
 *    ▶ See: asteroidFactory.createAsteroid(...)
 *
 * 3. ABSTRACT FACTORY  (IAsteroidFactory / ChasingAsteroidFactory / StationaryAsteroidFactory)
 *    AsteroidFactory itself delegates to one of two concrete factories that both
 *    implement IAsteroidFactory.  Swapping in a new "family" of asteroid behaviour
 *    (e.g. all chasing, or a future "zigzag" type) requires zero changes here.
 *    ▶ See: AsteroidFactory constructor, IAsteroidFactory interface
 *
 * Other patterns already present in the engine:
 * - Strategy  (MovementBehavior / CollisionBehavior)
 * - Observer  (NumberCollectionBehavior reacts to collisions)
 * - Facade    (OutputManager)
 */
public class MathGameScene extends Scene {

    private final SceneManager scenes;

    // -----------------------------------------------------------------
    // Game-logic collaborators
    // -----------------------------------------------------------------
    private EquationGenerator equationGenerator;

    /**
     * AsteroidFactory (Factory Pattern) – creates asteroids.
     * Internally it uses the Abstract Factory pattern to choose between
     * ChasingAsteroidFactory and StationaryAsteroidFactory.
     */
    private AsteroidFactory asteroidFactory;

    // -----------------------------------------------------------------
    // Tiled map / camera
    // -----------------------------------------------------------------
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private final List<Rectangle> worldColliders = new ArrayList<>();
    private float mapPixelWidth;
    private float mapPixelHeight;

    private static final float VIEW_W = 1280f;
    private static final float VIEW_H = 720f;

    // -----------------------------------------------------------------
    // Spawn / entity settings
    // -----------------------------------------------------------------
    private static final int   ASTEROID_COUNT = 5;
    private static final float ASTEROID_SIZE  = 50f;
    private static final float ASTEROID_SPEED = 0.8f;

    // -----------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------
    private PlayableEntity rocket;
    private List<Entity>   permanentObstacles;

    // -----------------------------------------------------------------
    // HUD textures
    // -----------------------------------------------------------------
    private Texture filledHeart;
    private Texture emptyHeart;

    // -----------------------------------------------------------------
    // Floating text pop-ups
    // -----------------------------------------------------------------
    private static class FloatingText {
        String text;
        float  x, y;
        Color  color;
        float  timer = 1.0f;
    }
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // =================================================================

    public MathGameScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    // -----------------------------------------------------------------
    // Scene lifecycle
    // -----------------------------------------------------------------

    @Override
    public void onEnter() {
        System.out.println("Entering Math Game Scene...");
 
        // ── Singleton: reset game state for this session ──────────────
        GameStateManager.getInstance().reset();
 
        equationGenerator  = new EquationGenerator();
        permanentObstacles = new ArrayList<>();
 
        // Tiled map
        map         = new TmxMapLoader().load("maps/level1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
 
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();
 
        int wTiles = map.getProperties().get("width",     Integer.class);
        int hTiles = map.getProperties().get("height",    Integer.class);
        int tW     = map.getProperties().get("tilewidth", Integer.class);
        int tH     = map.getProperties().get("tileheight",Integer.class);
        mapPixelWidth  = wTiles * tW;
        mapPixelHeight = hTiles * tH;
 
        worldColliders.clear();
        MapLayer colLayer = map.getLayers().get("Collisions");
        if (colLayer != null) {
            for (MapObject obj : colLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    // No Y-flip: use the rectangle exactly as defined in the Tiled map
                    worldColliders.add(new Rectangle(((RectangleMapObject) obj).getRectangle()));
                }
            }
        }
        collisionManager.setWorldCollisionData(worldColliders, mapPixelWidth, mapPixelHeight);
 
        // ── Create Player ─────────────────────────────────────────────
        rocket = new PlayableEntity(
            "spaceship.png",
            "collision.wav",
            outputManager,
            new ResetOnTouchBehavior(),
            100, 220,
            5,                                           // speed
            50, 50,
            "PLAYER",
            GameStateManager.STARTING_LIVES              // lives from Singleton constant
        );
        rocket.setOutputManager(outputManager);
        entityManager.addEntity(rocket);
        entityManager.addPlayableEntity(rocket);
 
        // ── Factory Pattern: build the AsteroidFactory ────────────────
        // AsteroidFactory internally holds both concrete Abstract Factories
        // (ChasingAsteroidFactory & StationaryAsteroidFactory).
        asteroidFactory = new AsteroidFactory(
            mapPixelWidth, mapPixelHeight,
            rocket, permanentObstacles,
            equationGenerator, this,
            worldColliders   // passed so AsteroidFactory can reject tile-overlapping spawn positions
        );
 
        // First round
        generateNewRound();
 
        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true);
        } catch (Exception e) {
            System.out.println("[DEBUG] background.wav not found.");
        }
 
        filledHeart = new Texture(Gdx.files.internal("heart-filled.png"));
        emptyHeart  = new Texture(Gdx.files.internal("heart-empty.png"));
    }

    // -----------------------------------------------------------------
    // Callbacks invoked by NumberCollectionBehavior (Strategy Pattern)
    // These delegate to the Singleton for state mutations, then handle
    // scene transitions.
    // -----------------------------------------------------------------

    /**
     * Called by NumberCollectionBehavior when a WRONG asteroid is hit.
     * The Singleton has already deducted the life; this method only drives
     * the scene transition when lives reach 0.
     */
    public void triggerGameOver() {
        scenes.setScene(new GameOverScene(scenes));
    }

    /**
     * Called by NumberCollectionBehavior when the player wins (5 correct).
     */
    public void triggerWin() {
        scenes.setScene(new WinScene(scenes));
    }

    /**
     * Kept for backward compatibility with any call sites that still use
     * deductLife() directly (e.g., world-boundary collisions handled in the
     * abstract engine).  Uses the Singleton for the actual deduction.
     */
    public void deductLife() {
        boolean stillAlive = GameStateManager.getInstance().deductLife();
        // Sync rocket's displayed lives with Singleton
        rocket.setLives(GameStateManager.getInstance().getLives());
        if (!stillAlive) {
            triggerGameOver();
        }
    }

    /**
     * Kept for backward compatibility.  Uses the Singleton for the actual
     * score update.
     */
    public void addScore(int points) {
        GameStateManager.getInstance().addScore(points);
    }

    // -----------------------------------------------------------------
    // Round generation
    // -----------------------------------------------------------------

    /**
     * Generates a new set of asteroids for the current equation.
     * One asteroid displays the correct answer; the rest show decoys.
     *
     * Uses the Factory Pattern (asteroidFactory) to create each asteroid.
     * The factory internally uses the Abstract Factory pattern to pick
     * ChasingAsteroidFactory or StationaryAsteroidFactory per asteroid.
     */
    public void generateNewRound() {
        equationGenerator.generateNewEquation();
        int correctAnswer = equationGenerator.getCurrentAnswer();

        // Remove existing asteroids
        for (Entity e : entityManager.getEntityList()) {
            if (e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                e.setActive(false);
            }
        }
        permanentObstacles.clear();

        // Decide which asteroid index holds the correct answer
        int correctIndex = ThreadLocalRandom.current().nextInt(0, ASTEROID_COUNT);

        // Scale speed with level (capped at 2.5)
        int   level        = computeLevel();
        float dynamicSpeed = Math.min(2.5f, ASTEROID_SPEED + (level - 1) * 0.15f);

        for (int i = 0; i < ASTEROID_COUNT; i++) {
            int value;
            if (i == correctIndex) {
                value = correctAnswer;
            } else {
                do {
                    value = ThreadLocalRandom.current().nextInt(1, 21);
                } while (value == correctAnswer);
            }

            // ── Factory Pattern call ────────────────────────────────────
            // AsteroidFactory decides which Abstract Factory implementation
            // to use and returns a fully configured NonPlayableEntity.
            NonPlayableEntity asteroid =
                asteroidFactory.createAsteroid(value, ASTEROID_SIZE, dynamicSpeed);

            entityManager.addEntity(asteroid);
            permanentObstacles.add(asteroid);
        }
    }

    /** Derives the current difficulty level from equations answered. */
    private int computeLevel() {
        // Every equation answered increases the level (min 1)
        return Math.max(1, GameStateManager.getInstance().getEquationsAnswered() + 1);
    }

    // -----------------------------------------------------------------
    // Floating text feedback
    // -----------------------------------------------------------------

    public void spawnFloatingText(String text, float x, float y, Color color) {
        FloatingText ft = new FloatingText();
        ft.text  = text;
        ft.x     = x;
        ft.y     = y;
        ft.color = new Color(color);
        floatingTexts.add(ft);
    }

    // -----------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------

    @Override
    public void update(float dt) {
        // Guard – prevent update after scene transition is already queued
        if (GameStateManager.getInstance().isGameOver()) return;

        float prevX = rocket.getX();
        float prevY = rocket.getY();

        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());

        entityManager.removeInactiveEntities();
        collisionManager.updateWorld(rocket, prevX, prevY);

        // Keep camera following the rocket
        float halfW    = camera.viewportWidth  / 2f;
        float halfH    = camera.viewportHeight / 2f;
        float clampedX = Math.max(halfW, Math.min(rocket.getX() + rocket.getWidth()  / 2f, mapPixelWidth  - halfW));
        float clampedY = Math.max(halfH, Math.min(rocket.getY() + rocket.getHeight() / 2f, mapPixelHeight - halfH));
        camera.position.set(clampedX, clampedY, 0);
        camera.update();

        // Update floating texts (float up, fade out)
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y     += 50 * dt;
            ft.timer -= dt;
            ft.color.a = Math.max(0, ft.timer);
            if (ft.timer <= 0) floatingTexts.remove(i);
        }

        // Sync rocket's displayed lives with Singleton (keeps heart HUD accurate)
        rocket.setLives(GameStateManager.getInstance().getLives());
    }

    // -----------------------------------------------------------------
    // Render
    // -----------------------------------------------------------------

    @Override
    public void render(SpriteBatch batch) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        // ── WORLD SPACE ───────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        entityManager.drawEntity(batch);

        // Draw the number label on each asteroid
        for (Entity e : entityManager.getEntityList()) {
            if (e.isActive() && e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                String num = e.getTag().substring(9); // strip "ASTEROID_"
                outputManager.drawText(batch, num, e.getX() + 15, e.getY() + 35, 1.2f);
            }
        }

        // Floating feedback text
        for (FloatingText ft : floatingTexts) {
            outputManager.drawText(batch, ft.text, ft.x, ft.y, 1.5f, ft.color);
        }

        batch.end();

        // ── SCREEN SPACE / HUD ────────────────────────────────────────
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();

        // ── Singleton: read live values for the HUD ───────────────────
        GameStateManager gsm = GameStateManager.getInstance();

        // Equation prompt
        outputManager.drawText(batch,
            "Solve: " + equationGenerator.getCurrentEquation(),
            500, Gdx.graphics.getHeight() - 40, 2.0f);

        // Score
        outputManager.drawText(batch,
            "SCORE: " + gsm.getScore(),
            20, Gdx.graphics.getHeight() - 20, 1.5f);

        // Win progress counter  (e.g. "Answered: 2 / 5")
        outputManager.drawText(batch,
            "Answered: " + gsm.getEquationsAnswered() + " / " + GameStateManager.EQUATIONS_TO_WIN,
            20, Gdx.graphics.getHeight() - 120, 1.3f);

        // Lives label
        outputManager.drawText(batch, "LIVES: ",
            20, Gdx.graphics.getHeight() - 60, 1.5f);

        // Heart icons
        float heartX  = 90f;
        float heartY  = Gdx.graphics.getHeight() - 85f;
        float spacing = 40f;
        int   maxLives     = GameStateManager.STARTING_LIVES;
        int   currentLives = gsm.getLives();

        for (int i = 0; i < maxLives;     i++) batch.draw(emptyHeart,  heartX + i * spacing, heartY, 32, 32);
        for (int i = 0; i < currentLives; i++) batch.draw(filledHeart, heartX + i * spacing, heartY, 32, 32);

        batch.end();
    }

    // -----------------------------------------------------------------
    // Dispose
    // -----------------------------------------------------------------

    @Override
    public void dispose() {
        if (mapRenderer != null) mapRenderer.dispose();
        if (map         != null) map.dispose();
        entityManager.getEntityList().clear();
        entityManager.getPlayableEntityList().clear();
    }
}
