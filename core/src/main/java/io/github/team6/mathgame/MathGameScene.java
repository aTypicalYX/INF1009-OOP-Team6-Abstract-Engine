package io.github.team6.mathgame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

public class MathGameScene extends Scene {

    private final SceneManager scenes;
    
    // Game Specific Logic
    private EquationGenerator equationGenerator;
    private AsteroidFactory asteroidFactory; // NEW: The Factory to handle spawning
    private int score = 0; 

    // Tiled visuals + world collision
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private final List<Rectangle> worldColliders = new ArrayList<>();
    private float mapPixelWidth;
    private float mapPixelHeight;

    private static final float VIEW_W = 1280f;
    private static final float VIEW_H = 720f;

    // Constants 
    private static final int ASTEROID_COUNT = 5;
    private static final float ASTEROID_SIZE = 50f;
    private static final float ASTEROID_SPEED = 0.8f;

    private PlayableEntity rocket;
    private List<Entity> permanentObstacles;

    public MathGameScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public void onEnter() {
        System.out.println("Entering Math Game Scene...");
        
        equationGenerator = new EquationGenerator();
        permanentObstacles = new ArrayList<>();

        // Load map visuals
        map = new TmxMapLoader().load("maps/level1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();

        int wTiles = map.getProperties().get("width", Integer.class);
        int hTiles = map.getProperties().get("height", Integer.class);
        int tW = map.getProperties().get("tilewidth", Integer.class);
        int tH = map.getProperties().get("tileheight", Integer.class);
        mapPixelWidth = wTiles * tW;
        mapPixelHeight = hTiles * tH;

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
        collisionManager.setWorldCollisionData(worldColliders, mapPixelWidth, mapPixelHeight);

        // 1. Create Rocket (Player)
        rocket = new PlayableEntity(
            "spaceship.png",                
            "collision.wav",             
            outputManager,               
            new ResetOnTouchBehavior(),  
            100, 220, 5, 50, 50, "PLAYER"
        );
        rocket.setOutputManager(outputManager);

        entityManager.addEntity(rocket);
        entityManager.addPlayableEntity(rocket);

        // 2. Initialize the Factory Pattern
        asteroidFactory = new AsteroidFactory(mapPixelWidth, mapPixelHeight, rocket, permanentObstacles, equationGenerator, this);

        // 3. Start the first round!
        generateNewRound();

        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true);
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: background.wav not found.");
        }
    }

    public void addScore(int points) {
        score += points;
        if (score < 0) score = 0; 
    }

    public void generateNewRound() {
        equationGenerator.generateNewEquation();
        int correctAnswer = equationGenerator.getCurrentAnswer();

        for (Entity e : entityManager.getEntityList()) {
            if (e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                e.setActive(false); 
            }
        }
        permanentObstacles.clear();

        int correctIndex = ThreadLocalRandom.current().nextInt(0, ASTEROID_COUNT);

        for (int i = 0; i < ASTEROID_COUNT; i++) {
            int valueForAsteroid;
            
            if (i == correctIndex) {
                valueForAsteroid = correctAnswer;
            } else {
                do {
                    valueForAsteroid = ThreadLocalRandom.current().nextInt(1, 21);
                } while (valueForAsteroid == correctAnswer);
            }

            // USE FACTORY TO CREATE ASTEROIDS
            NonPlayableEntity asteroid = asteroidFactory.createAsteroid(valueForAsteroid, ASTEROID_SIZE, ASTEROID_SPEED);
            
            entityManager.addEntity(asteroid);
            permanentObstacles.add(asteroid);
        }
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            scenes.setScene(new MainMenuScene(scenes));
            return;
        }

        float prevX = rocket.getX();
        float prevY = rocket.getY();

        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());

        entityManager.removeInactiveEntities();

        collisionManager.updateWorld(rocket, prevX, prevY);
        updateCamera(camera, rocket.getX() + rocket.getWidth() / 2f, rocket.getY() + rocket.getHeight() / 2f, mapPixelWidth, mapPixelHeight);
    }
    
    @Override
    public void render(SpriteBatch batch) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        entityManager.drawEntity(batch);
        
        // Use OutputManager for text rendering
        for (Entity entity : entityManager.getEntityList()) {
            if (entity.isActive() && entity.getTag() != null && entity.getTag().startsWith("ASTEROID_")) {
                String numStr = entity.getTag().substring(9);
                outputManager.drawText(batch, numStr, entity.getX() + 15, entity.getY() + 35, 1.2f);
            }
        }
        batch.end();

        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();
        
        // Use OutputManager for HUD
        outputManager.drawText(batch, "Solve: " + equationGenerator.getCurrentEquation(), 500, Gdx.graphics.getHeight() - 40, 2.0f);
        outputManager.drawText(batch, "SCORE: " + score, 20, Gdx.graphics.getHeight() - 20, 1.5f);
        batch.end();
    }

    @Override
    public void dispose() {
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();
        entityManager.getEntityList().clear();
        entityManager.getPlayableEntityList().clear();
    }
}