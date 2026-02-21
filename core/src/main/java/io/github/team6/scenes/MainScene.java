package io.github.team6.scenes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import io.github.team6.entities.behavior.PermanentCollisionBehavior;
import io.github.team6.entities.behavior.ResetOnTouchBehavior;
import io.github.team6.entities.behavior.StationaryMovementBehavior;
import io.github.team6.inputoutput.MusicSource;
import io.github.team6.managers.SceneManager;
public class MainScene extends Scene {

    private final SceneManager scenes;
    private BitmapFont font; // Used for drawing UI text (Score)

    // Game State Data
    private float timeSurvived;
    private int score;

    // Tiled visuals + world collision
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private final List<Rectangle> worldColliders = new ArrayList<>();
    private float mapPixelWidth;
    private float mapPixelHeight;

    // Camera viewport
    private static final float VIEW_W = 1280f;
    private static final float VIEW_H = 720f;

    public MainScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    // Constants for configuration
    private static final int PERMANENT_STATIONARY_COUNT = 2;
    private static final int CHASING_COUNT = 2;

    private static final float PERMANENT_DROPLET_WIDTH = 50f;
    private static final float PERMANENT_DROPLET_HEIGHT = 50f;
    private static final float CHASING_DROPLET_WIDTH = 30f;
    private static final float CHASING_DROPLET_HEIGHT = 30f;
    private static final float CHASING_DROPLET_SPEED = 0.5f;

    // Entity References
    private PlayableEntity playableEntity;
    private List<Entity> permanentObstacles;

    @Override
    public void onEnter() {
        System.out.println("Entering Main Scene...");
        font = new BitmapFont();

        // Load Tiled map (visuals) + collision rectangles (platform feel)
        map = new TmxMapLoader().load("maps/level1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();

        // Map pixel size for bounds + camera clamping
        int wTiles = map.getProperties().get("width", Integer.class);
        int hTiles = map.getProperties().get("height", Integer.class);
        int tW = map.getProperties().get("tilewidth", Integer.class);
        int tH = map.getProperties().get("tileheight", Integer.class);
        mapPixelWidth = wTiles * tW;
        mapPixelHeight = hTiles * tH;

        // Load world colliders from Tiled object layer "Collisions"
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
        System.out.println("[DEBUG] World colliders loaded: " + worldColliders.size());
        collisionManager.setWorldCollisionData(worldColliders, mapPixelWidth, mapPixelHeight);

        // 1. Create Player Entity
        // Inject dependencies (Texture, Sound, Behavior) here.
        playableEntity = new PlayableEntity(
            "bucket.png",                // Texture
            "collision.wav",             // Sound
            outputManager,               // Audio Manager
            new ResetOnTouchBehavior(),  // Reset position on hit
            100, 220, 5, 50, 50, "PLAYER"
        );
        playableEntity.setOutputManager(outputManager);

        // 2. Register with EntityManager so it gets updated/drawn
        entityManager.addEntity(playableEntity);
        entityManager.addPlayableEntity(playableEntity);
        // entityManager.addEntity(droplet);
        permanentObstacles = new ArrayList<>();

        // 3. Factory Logic: Create Obstacles
        // Preferred: spawn from Tiled layer "Spawns"
        boolean spawnedFromTiled = spawnDropletsFromTiled();

        // Fallback: if no spawn objects were found, use your original random spawning
        if (!spawnedFromTiled) {
            for (int i = 0; i < PERMANENT_STATIONARY_COUNT; i++) {
                NonPlayableEntity permanentStationaryDroplet = createPermanentStationaryDroplet();
                entityManager.addEntity(permanentStationaryDroplet);
                permanentObstacles.add(permanentStationaryDroplet);
            }

            for (int i = 0; i < CHASING_COUNT; i++) {
                entityManager.addEntity(createChasingDroplet());
            }
        }

        // 4. Start Background Music
        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true);
            System.out.println("[DEBUG] Game background.wav loaded and playing");
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: background.wav not found. Background music disabled.");
            e.printStackTrace();
        }
    }

    // The Main Game Loop Logic ( Input -> Move -> Collide -> Cleanup.)
    @Override
    public void update(float dt) {
        // Press ESC to go back to main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            scenes.setScene(new MainMenuScene(scenes));
            return;
        }

        // Keep previous position for world-collision resolution
        float prevX = playableEntity.getX();
        float prevY = playableEntity.getY();

        // Run the Managers
        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());

        entityManager.removeInactiveEntities();

        // World collision + camera follow
        collisionManager.updateWorld(playableEntity, prevX, prevY);
        updateCamera(camera, playableEntity.getX() + playableEntity.getWidth() / 2f, playableEntity.getY() + playableEntity.getHeight() / 2f, mapPixelWidth, mapPixelHeight);
    
        // Increase score based on survival time
        timeSurvived += dt;
        score = (int) timeSurvived * 10;
    }

    // Helper Factory Method to create a specific type of enemy
    private NonPlayableEntity createPermanentStationaryDroplet() {
        float[] position = getSafeSpawnPosition(PERMANENT_DROPLET_WIDTH, PERMANENT_DROPLET_HEIGHT);
        // PHASE 1 CHANGE: Pass "ENEMY" (or "OBSTACLE") tag. Removed DropletType.
        return new NonPlayableEntity(
            "droplet.png", position[0], position[1], 0,
            PERMANENT_DROPLET_WIDTH, PERMANENT_DROPLET_HEIGHT, "ENEMY",
            new StationaryMovementBehavior(),
            new PermanentCollisionBehavior(),
            playableEntity
        );
    }

    // Helper Factory Method to create a Chasing Enemy
    private NonPlayableEntity createChasingDroplet() {
        float[] position = getSafeSpawnPosition(CHASING_DROPLET_WIDTH, CHASING_DROPLET_HEIGHT);
        // PHASE 1 CHANGE: Pass "ENEMY" tag. Removed DropletType.
        return new NonPlayableEntity(
            "droplet.png", position[0], position[1], CHASING_DROPLET_SPEED,
            CHASING_DROPLET_WIDTH, CHASING_DROPLET_HEIGHT, "ENEMY",
            new ChasingMovementBehavior(permanentObstacles),
            new PermanentCollisionBehavior(),
            playableEntity
        );
    }

    // Algorithm to find a spawn point that isn't colliding with the player
    private float[] getSafeSpawnPosition(float width, float height) {
        float maxX = Math.max(0, mapPixelWidth - width);
        float maxY = Math.max(0, mapPixelHeight - height);

        for (int attempt = 0; attempt < 20; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            float randomY = ThreadLocalRandom.current().nextFloat() * maxY;

            boolean intersectsplayableEntity = randomX < playableEntity.getX() + playableEntity.getWidth()
                    && randomX + width > playableEntity.getX()
                    && randomY < playableEntity.getY() + playableEntity.getHeight()
                    && randomY + height > playableEntity.getY();

            if (!intersectsplayableEntity) {
                return new float[] { randomX, randomY };
            }
        }

        return new float[] { 0, 0 };
    }

    /**
     * Spawns droplets based on Tiled Object Layer "Spawns".
     * Each object must have a custom property: type = enemy_chasing OR enemy_stationary
     */
    private boolean spawnDropletsFromTiled() {
        MapLayer spawnLayer = map.getLayers().get("Spawns");
        if (spawnLayer == null) {
            System.out.println("Testing: No Spawns layer found. Using random spawns (default).");
            return false;
        }

        boolean spawned = false;
        // Spawns objects at the exact coordinates specified in Tiled.
        for (MapObject obj : spawnLayer.getObjects()) {
            String type = obj.getProperties().get("type", String.class);
            if (type == null) continue;

            Float xObj = obj.getProperties().get("x", Float.class);
            Float yObj = obj.getProperties().get("y", Float.class);
            if (xObj == null || yObj == null) continue;

            float x = xObj;
            float y = yObj;
            //Each object must have a custom property: type = enemy_chasing OR enemy_stationary
            if ("enemy_stationary".equals(type)) {
                NonPlayableEntity e = new NonPlayableEntity(
                    "droplet.png", x, y, 0,
                    PERMANENT_DROPLET_WIDTH, PERMANENT_DROPLET_HEIGHT, "ENEMY",
                    new StationaryMovementBehavior(),
                    new PermanentCollisionBehavior(),
                    playableEntity
                );
                entityManager.addEntity(e);
                permanentObstacles.add(e);
                spawned = true;      
            }

            if ("enemy_chasing".equals(type)) {
                NonPlayableEntity e = new NonPlayableEntity(
                    "droplet.png", x, y, CHASING_DROPLET_SPEED,
                    CHASING_DROPLET_WIDTH, CHASING_DROPLET_HEIGHT, "ENEMY",
                    new ChasingMovementBehavior(permanentObstacles),
                    new PermanentCollisionBehavior(),
                    playableEntity
                );
                entityManager.addEntity(e);
                spawned = true;
            }
        }
        // FOR TESTING: Returns true if at least one droplet was spawned from Tiled.
        System.out.println("Testing: Spawned from Tiled: " + spawned); 
        return spawned;
    }
    
    @Override
    public void render(SpriteBatch batch) {
        // Render map FIRST
        mapRenderer.setView(camera);
        mapRenderer.render();

        // WORLD RENDER (camera space)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        entityManager.drawEntity(batch);
        batch.end();

        // UI RENDER (screen space)
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        batch.begin();

        //DRAW HUD + Text Rendering (instructions + score)
        font.draw(batch, "Arrow Keys to move", 200, Gdx.graphics.getHeight() - 40);
        font.draw(batch, "ESC to return to menu", 200, Gdx.graphics.getHeight() - 80);
        font.setColor(1, 1, 1, 1); // White
        font.getData().setScale(1.5f);
        font.draw(batch, "SCORE: " + score, 20, Gdx.graphics.getHeight() - 20);

        batch.end();
    }

    @Override
    public void dispose() {
        System.out.println("Disposed of scene...");
        font.dispose();
        // Dispose Tiled resources
        if (mapRenderer != null) mapRenderer.dispose();
        if (map != null) map.dispose();

        // Clear entities when leaving the scene so they don't persist to the Menu
        entityManager.getEntityList().clear();
        entityManager.getPlayableEntityList().clear();
    }
}
