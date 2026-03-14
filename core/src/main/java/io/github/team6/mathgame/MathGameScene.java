package io.github.team6.mathgame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import io.github.team6.entities.behavior.ChasingMovementBehavior;
import io.github.team6.entities.behavior.ResetOnTouchBehavior;
import io.github.team6.inputoutput.MusicSource;
import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * MathGameScene is the concrete implementation of a level.
 * It sits on top of the abstract engine and handles all the logic specific to the Space Math game.
 */
public class MathGameScene extends Scene {

    private final SceneManager scenes; // Used to switch scenes (e.g., back to main menu)
    private BitmapFont font; // Used to draw text on the screen
    
    // --- GAME SPECIFIC STATE ---
    private EquationGenerator equationGenerator;
    private int score = 0; 

    // --- ENGINE / RENDERING COMPONENTS ---
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera; // Defines what part of the game world the player currently sees
    
    private final List<Rectangle> worldColliders = new ArrayList<>();
    private float mapPixelWidth;
    private float mapPixelHeight;

    // Viewport dimensions (Standard 720p HD)
    private static final float VIEW_W = 1280f;
    private static final float VIEW_H = 720f;

    // --- ENTITY CONFIGURATION CONSTANTS ---
    private static final int ASTEROID_COUNT = 5;
    private static final float ASTEROID_SIZE = 50f;
    private static final float ASTEROID_SPEED = 0.8f;

    // References to active entities
    private PlayableEntity rocket;
    private List<Entity> permanentObstacles;

    public MathGameScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    /**
     * LIFECYCLE METHOD 1: onEnter()
     * Called exactly once when the SceneManager switches to this scene.
     * Used for initializing variables, loading assets, and setting up the initial state.
     */
    @Override
    public void onEnter() {
        System.out.println("Entering Math Game Scene...");
        font = new BitmapFont();
        equationGenerator = new EquationGenerator();
        permanentObstacles = new ArrayList<>();

        // Load the Tiled map (the visual background)
        map = new TmxMapLoader().load("maps/level1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Setup the camera to look at the game world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();

        // Calculate total map size by multiplying tile count by tile size
        int wTiles = map.getProperties().get("width", Integer.class);
        int hTiles = map.getProperties().get("height", Integer.class);
        int tW = map.getProperties().get("tilewidth", Integer.class);
        int tH = map.getProperties().get("tileheight", Integer.class);
        mapPixelWidth = wTiles * tW;
        mapPixelHeight = hTiles * tH;

        // Parse invisible collision boundaries from the Tiled map layer "Collisions"
        worldColliders.clear();
        MapLayer colLayer = map.getLayers().get("Collisions");
        if (colLayer != null) {
            for (MapObject obj : colLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    RectangleMapObject rmo = (RectangleMapObject) obj;
                    worldColliders.add(new Rectangle(rmo.getRectangle()));
                }
            }
        }
        // Hand the collision boundaries over to the engine's CollisionManager
        collisionManager.setWorldCollisionData(worldColliders, mapPixelWidth, mapPixelHeight);

        // 1. Instantiate the Player (Rocket)
        // Note how we inject the ResetOnTouchBehavior strategy here.
        rocket = new PlayableEntity(
            "spaceship.png",                
            "collision.wav",             
            outputManager,               
            new ResetOnTouchBehavior(),  
            100, 220, 5, 50, 50, "PLAYER"
        );
        rocket.setOutputManager(outputManager);

        // Register the rocket with the engine so it gets updated and drawn
        entityManager.addEntity(rocket);
        entityManager.addPlayableEntity(rocket);

        // 2. Start the first round! (Generates math and spawns asteroids)
        generateNewRound();

        // Start Background Music
        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true);
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: background.wav not found.");
        }
    }

    // ==========================================
    // --- GAME LOGIC METHODS (CUSTOM) ---
    // ==========================================

    /**
     * Safely updates the score, ensuring it never drops below zero (good for kids games).
     */
    public void addScore(int points) {
        score += points;
        if (score < 0) score = 0; 
    }

    /**
     * Handles wiping the board and setting up a brand new math equation.
     */
    public void generateNewRound() {
        // 1. Generate new math problem via our encapsulated EquationGenerator
        equationGenerator.generateNewEquation();
        int correctAnswer = equationGenerator.getCurrentAnswer();

        // 2. Cleanup Phase: Loop through all entities and destroy old asteroids
        for (Entity e : entityManager.getEntityList()) {
            // We use the tag prefix to identify which entities are asteroids
            if (e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                e.setActive(false); // Flags it for deletion by the EntityManager
            }
        }
        permanentObstacles.clear();

        // 3. Pick a random index (0 to ASTEROID_COUNT-1) to hold the guaranteed correct answer
        int correctIndex = ThreadLocalRandom.current().nextInt(0, ASTEROID_COUNT);

        // 4. Spawn Phase: Create the new batch of asteroids
        for (int i = 0; i < ASTEROID_COUNT; i++) {
            int valueForAsteroid;
            
            if (i == correctIndex) {
                // This specific asteroid gets the right answer
                valueForAsteroid = correctAnswer;
            } else {
                // Generate a random WRONG answer for the decoys.
                do {
                    valueForAsteroid = ThreadLocalRandom.current().nextInt(1, 21);
                } while (valueForAsteroid == correctAnswer); // Keep trying if we accidentally generated the correct answer
            }

            // Factory method call to construct the entity
            NonPlayableEntity asteroid = createAsteroid(valueForAsteroid);
            
            // Register the new entity with the engine
            entityManager.addEntity(asteroid);
            permanentObstacles.add(asteroid);
        }
    }

    /**
     * Factory method to assemble an Asteroid entity with all its required strategies.
     */
    private NonPlayableEntity createAsteroid(int numberValue) {
        // Find a spot that doesn't overlap the player
        float[] position = getSafeSpawnPosition(ASTEROID_SIZE, ASTEROID_SIZE);
        
        // Construct the asteroid. 
        // Notice we inject our new custom NumberCollectionBehavior into it here!
        // We also tag it with the string "ASTEROID_X" (e.g., "ASTEROID_7") so we can render the text '7' later.
        return new NonPlayableEntity(
            "asteroid.png", position[0], position[1], ASTEROID_SPEED,
            ASTEROID_SIZE, ASTEROID_SIZE, "ASTEROID_" + numberValue,
            new ChasingMovementBehavior(permanentObstacles),
            new NumberCollectionBehavior(equationGenerator, numberValue, this),
            rocket
        );
    }

    /**
     * Algorithm to randomly search for an empty X/Y coordinate to spawn an enemy.
     */
    private float[] getSafeSpawnPosition(float width, float height) {
        float maxX = Math.max(0, mapPixelWidth - width);
        float maxY = Math.max(0, mapPixelHeight - height);

        for (int attempt = 0; attempt < 20; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            float randomY = ThreadLocalRandom.current().nextFloat() * maxY;

            // Simple Axis-Aligned Bounding Box (AABB) collision check against the player
            boolean intersectsRocket = randomX < rocket.getX() + rocket.getWidth()
                    && randomX + width > rocket.getX()
                    && randomY < rocket.getY() + rocket.getHeight()
                    && randomY + height > rocket.getY();

            if (!intersectsRocket) {
                return new float[] { randomX, randomY }; // Found a safe spot
            }
        }
        return new float[] { 0, 0 }; // Fallback
    }

    // ==========================================
    // --- ENGINE LIFECYCLE METHODS ---
    // ==========================================

    /**
     * LIFECYCLE METHOD 2: update()
     * Called every frame (e.g., 60 times a second). 
     * Handles all game logic, input, movement, and physics processing BEFORE rendering.
     */
    @Override
    public void update(float dt) {
        // Scene Transition Logic
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            scenes.setScene(new MainMenuScene(scenes));
            return; // Exit the loop early so we don't update a dead scene
        }

        // Store previous position to handle solid wall collisions
        float prevX = rocket.getX();
        float prevY = rocket.getY();

        // Pass control to the Engine Managers to process the abstract entities
        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());

        // Cleanup entities marked as inactive (e.g., destroyed asteroids)
        entityManager.removeInactiveEntities();

        // Process physics boundaries
        collisionManager.updateWorld(rocket, prevX, prevY);
        
        // Make the camera follow the player's center coordinates
        updateCamera(camera, rocket.getX() + rocket.getWidth() / 2f, rocket.getY() + rocket.getHeight() / 2f, mapPixelWidth, mapPixelHeight);
    }
    
    /**
     * LIFECYCLE METHOD 3: render()
     * Called every frame AFTER update().
     * Strictly responsible for drawing pixels to the screen. No math or logic should happen here.
     */
    @Override
    public void render(SpriteBatch batch) {
        
        // --- LAYER 1: WORLD RENDERING (Uses Camera Coordinates) ---
        mapRenderer.setView(camera);
        mapRenderer.render(); // Draw Tiled Background

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        entityManager.drawEntity(batch); // Ask engine to draw all sprites
        
        // Draw the numbers directly on top of the asteroids
        // font.setColor(30f, 200f, 179f, 1f); 
        font.setColor(Color.MAGENTA);
        font.getData().setScale(1.6f);
        for (Entity entity : entityManager.getEntityList()) {
            if (entity.isActive() && entity.getTag() != null && entity.getTag().startsWith("ASTEROID_")) {
                // Extract the number from the tag string (e.g., "ASTEROID_5" -> "5")
                String numStr = entity.getTag().substring(9);
                // Draw text offset from the entity's X/Y so it appears in the middle of the graphic
                font.draw(batch, numStr, entity.getX() + 20, entity.getY() +40);
            }
        }
        batch.end();

        // --- LAYER 2: UI RENDERING (Uses Screen/Monitor Coordinates) ---
        // We reset the projection matrix so UI elements stay pinned to the screen and don't scroll with the camera
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();
        
        font.setColor(1, 1, 1, 1); 
        font.getData().setScale(2.0f);
        // Draw the current math problem top-center
        font.draw(batch, "Solve: " + equationGenerator.getCurrentEquation(), 500, Gdx.graphics.getHeight() - 40);

        font.getData().setScale(1.5f);
        // Draw Score top-left
        font.draw(batch, "SCORE: " + score, 20, Gdx.graphics.getHeight() - 20);
        batch.end();
    }

    /**
     * LIFECYCLE METHOD 4: dispose()
     * Called when the SceneManager switches AWAY from this scene.
     * Crucial for memory management to prevent memory leaks.
     */
    @Override
    public void dispose() {
        font.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();
        
        // Empty the engine lists so entities don't bleed over into the main menu
        entityManager.getEntityList().clear();
        entityManager.getPlayableEntityList().clear();
    }
}