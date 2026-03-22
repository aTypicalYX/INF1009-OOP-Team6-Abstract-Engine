package io.github.team6.mathgame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import io.github.team6.entities.behavior.StationaryMovementBehavior;
import io.github.team6.inputoutput.AudioSource;
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
    // Config
    // -----------------------------------------------------------------
    private static final int   ASTEROID_COUNT         = 7;    // more asteroids per round
    private static final float ASTEROID_SIZE          = 60f;
    private static final float ASTEROID_SPEED         = 0.8f;
    private static final float POWERUP_SIZE           = 48f;
    private static final float POWERUP_SPAWN_INTERVAL = 20f;


    // Screen-width viewport — no tile borders visible
    private static final float VIEW_W = 1280f;
    private static final float VIEW_H = 720f;

    // -----------------------------------------------------------------
    // Collaborators
    // -----------------------------------------------------------------
    private EquationGenerator equationGenerator;
    
    /**
     * AsteroidFactory (Factory Pattern) – creates asteroids.
     * Internally it uses the Abstract Factory pattern to choose between
     * ChasingAsteroidFactory and StationaryAsteroidFactory.
     */
    private AsteroidFactory asteroidFactory;
    private PauseOverlay      pauseOverlay;

    // -----------------------------------------------------------------
    // Tiled map / camera
    // -----------------------------------------------------------------
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private final List<Rectangle> worldColliders = new ArrayList<>();
    private float mapPixelWidth;
    private float mapPixelHeight;

    // -----------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------
    private PlayableEntity rocket;
    private NonPlayableEntity chaser;
    private PlanetEntity planet;
    private final List<Entity> permanentObstacles = new ArrayList<>();

    // -----------------------------------------------------------------
    // HUD textures
    // -----------------------------------------------------------------
    private Texture filledHeart;
    private Texture emptyHeart;
    private Texture progressIcon; // The mini-spaceship for the map tracker
    private Texture equationBg;   // Background panel for the equation HUD
    private Texture infoBg;       // Background panel for the stats HUD (top-left)
    // -----------------------------------------------------------------
    // Gameplay SFX
    // -----------------------------------------------------------------
    private AudioSource correctAnswerSfx;
    private AudioSource wrongAnswerSfx;

    // -----------------------------------------------------------------
    // Floating text pop-ups
    // -----------------------------------------------------------------
    private static class FloatingText {
        String text;
        float  x, y;
        Color  color;
        float  timer = 1.2f;
    }
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // -----------------------------------------------------------------
    // Timers
    // -----------------------------------------------------------------
    private float powerUpTimer = 0f;

    // -----------------------------------------------------------------
    // Win trigger rectangle (from Tiled "PLANET" object)
    // -----------------------------------------------------------------
    private Rectangle planetZone;

    // =======================================================================

    public MathGameScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    // -----------------------------------------------------------------
    // Scene lifecycle
    // -----------------------------------------------------------------

    @Override
    public void onEnter() {
        System.out.println("[MathGame] Entering — Level " + GameStateManager.getInstance().getLevel());

        // Only reset fully if starting fresh (level 1).
        // Level 2 carries the score over but refreshes lives and timer.
        if (GameStateManager.getInstance().getLevel() == 1) {
            GameStateManager.getInstance().reset();
        } else {
            // Level 2 entry: restore lives and restart the timer, keep score
            GameStateManager.getInstance().refreshLivesAndTimer();
        }

        equationGenerator = new EquationGenerator();
        equationGenerator.setLevel(GameStateManager.getInstance().getLevel());

        //  Load Tiled map (level-specific)
        String mapFile = (GameStateManager.getInstance().getLevel() == 2)
            ? "maps/level2.tmx" : "maps/level1.tmx";
        map         = new TmxMapLoader().load(mapFile);
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);

        int wTiles = map.getProperties().get("width",      Integer.class);
        int hTiles = map.getProperties().get("height",     Integer.class);
        int tW     = map.getProperties().get("tilewidth",  Integer.class);
        int tH     = map.getProperties().get("tileheight", Integer.class);
        mapPixelWidth  = wTiles * tW;
        mapPixelHeight = hTiles * tH;

        // World colliders
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

        // Read planet/win zone from Spawns layer
        planetZone = null;
        MapLayer spawnLayer = map.getLayers().get("Spawns");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                String type = obj.getProperties().get("type", String.class);
                if ("planet".equals(type) && obj instanceof RectangleMapObject) {
                    planetZone = new Rectangle(((RectangleMapObject) obj).getRectangle());
                }
            }
        }
        // Fallback: treat the top 80px of the map as the win zone
        if (planetZone == null) {
            planetZone = new Rectangle(0, mapPixelHeight - 80, mapPixelWidth, 80);
        }

        //  Animated planet — centred on the win zone 
        float planetSize = 600f;
        planet = new PlanetEntity(
            mapPixelWidth / 2f,              // centre X
            planetZone.y + planetZone.height / 2f - planetSize / 2f, // centred on zone
            planetSize
        );

        //  Spawn rocket centred at bottom 
        float rocketW = 50f, rocketH = 50f;
        float startX  = mapPixelWidth / 2f - rocketW / 2f;
        float startY  = 120f; // above the floor collision tiles
        rocket = new PlayableEntity(
            "spaceship.png", null, outputManager,
            new ResetOnTouchBehavior(),
            startX, startY, 5,
            rocketW, rocketH, "PLAYER",
            GameStateManager.STARTING_LIVES
        );
        entityManager.addEntity(rocket);
        entityManager.addPlayableEntity(rocket);

        //  Camera: start centred on rocket
        camera.position.set(mapPixelWidth / 2f, startY + VIEW_H / 2f, 0);
        camera.update();

        // ── Factory Pattern: build the AsteroidFactory ────────────────
        // AsteroidFactory internally holds both concrete Abstract Factories
        // (ChasingAsteroidFactory & StationaryAsteroidFactory). 
        asteroidFactory = new AsteroidFactory(
            mapPixelWidth, mapPixelHeight,
            rocket, permanentObstacles,
            equationGenerator, this,
            worldColliders
        );
 
        // First round
        generateNewRound();

        //  Chaser entity
        spawnChaser();

        //  Audio
        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true);
        } catch (Exception e) {
            System.out.println("[MathGame] background.wav not found.");
        }
        loadSfx();

        //  HUD textures 
        filledHeart  = new Texture(Gdx.files.internal("heart-filled.png"));
        emptyHeart   = new Texture(Gdx.files.internal("heart-empty.png"));
        progressIcon = new Texture(Gdx.files.internal("spaceship.png"));
        equationBg   = new Texture(Gdx.files.internal("equationBG.png"));
        infoBg       = new Texture(Gdx.files.internal("InfoBG.png"));

        //  Pause overlay
        pauseOverlay = new PauseOverlay(scenes, outputManager);
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
        int level = GameStateManager.getInstance().getLevel();
        scenes.setScene(new VictoryScene(scenes, level));
    }

    public void playCorrectAnswerSfx() {
        if (correctAnswerSfx != null) {
            outputManager.play(correctAnswerSfx);
        }
    }

    public void playWrongAnswerSfx() {
        if (wrongAnswerSfx != null) {
            outputManager.play(wrongAnswerSfx);
        }
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

        // Remove existing asteroids
        for (Entity e : entityManager.getEntityList()) {
            if (e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                e.setActive(false);
            }
        }
        permanentObstacles.clear();

        int   correctIndex  = ThreadLocalRandom.current().nextInt(0, ASTEROID_COUNT);
        int   level         = computeLevel();
        float dynamicSpeed  = Math.min(2.5f, ASTEROID_SPEED + (level - 1) * 0.15f);

        for (int i = 0; i < ASTEROID_COUNT; i++) {
            int value = (i == correctIndex)
                ? equationGenerator.getCurrentAnswer()
                : randomDecoy(equationGenerator.getCurrentAnswer());
            
            // ── Factory Pattern call ────────────────────────────────────
            // AsteroidFactory decides which Abstract Factory implementation
            // to use and returns a fully configured NonPlayableEntity.
                NonPlayableEntity asteroid =
                asteroidFactory.createAsteroid(value, ASTEROID_SIZE, dynamicSpeed);
            entityManager.addEntity(asteroid);
            permanentObstacles.add(asteroid);
        }
    }

    // -----------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------

    @Override
    public void update(float dt) {
        // Pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseOverlay.toggle();
        }
        if (pauseOverlay.isPaused()) return;

        GameStateManager gsm = GameStateManager.getInstance();
        if (gsm.isGameOver()) return;

        // Timer
        gsm.tickTime(dt);
        if (gsm.isTimeUp()) {
            triggerGameOver();
            return;
        }

        float prevX = rocket.getX();
        float prevY = rocket.getY();

        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());
        entityManager.removeInactiveEntities();
        collisionManager.updateWorld(rocket, prevX, prevY);

        // Sync lives display
        rocket.setLives(gsm.getLives());

        // Direct chaser collision check
        // Belt-and-braces: check overlap manually in addition to the
        // behavior system, since the chaser spans the full map width and
        // the CollisionManager may skip it due to entity ordering.
        if (chaser != null && chaser.isActive()) {
            Rectangle rocketBox = new Rectangle(
                rocket.getX(), rocket.getY(),
                rocket.getWidth(), rocket.getHeight());
            float inset = 60f;
            Rectangle chaserBox = new Rectangle(
                chaser.getX(), chaser.getY() + inset,
                chaser.getWidth(), chaser.getHeight() - inset);
            if (rocketBox.overlaps(chaserBox)) {
                triggerGameOver();
                return;
            }

            // --- Asteroid respawn check ---
            // If an asteroid has been swallowed by the chaser (its centre is
            // below the chaser's top edge), or is below the visible screen,
            // teleport it to a random position ahead of the rocket so the
            // player never loses a required asteroid off the bottom.
            float chaserTop    = chaser.getY() + chaser.getHeight();
            float screenBottom = rocket.getY() - VIEW_H / 2f;

            for (Entity e : entityManager.getEntityList()) {
                if (!e.isActive()) continue;
                if (e.getTag() == null || !e.getTag().startsWith("ASTEROID_")) continue;

                float asteroidCentreY = e.getY() + e.getHeight() / 2f;

                // Swallowed by chaser OR scrolled off the bottom of the screen
                if (asteroidCentreY < chaserTop || asteroidCentreY < screenBottom) {
                    // Respawn 200–500px above the rocket, random X within map
                    float newY = rocket.getY()
                        + 200f + ThreadLocalRandom.current().nextFloat() * 300f;
                    float newX = ThreadLocalRandom.current().nextFloat()
                        * (mapPixelWidth - e.getWidth());
                    newY = Math.min(newY, mapPixelHeight - e.getHeight() - 60f);
                    e.setX(newX);
                    e.setY(newY);
                }
            }
            // ------------------------------------------------
        }

        //  Win check: rocket enters planet zone
        Rectangle rocketRect = new Rectangle(
            rocket.getX(), rocket.getY(),
            rocket.getWidth(), rocket.getHeight());
        if (planetZone != null && rocketRect.overlaps(planetZone)) {
            triggerWin();
            return;
        }

        // Camera: scroll upward tracking rocket 
        // Lock X to map centre; track Y with bottom clamp
        float targetCamY = rocket.getY() + rocket.getHeight() / 2f;
        float halfH      = VIEW_H / 2f;
        float clampedY   = Math.max(halfH, Math.min(targetCamY, mapPixelHeight - halfH));
        // Fixed X (screen-width map — no horizontal scroll needed)
        float clampedX   = mapPixelWidth / 2f;
        camera.position.set(clampedX, clampedY, 0);
        camera.update();

        //  Power-up spawner
        powerUpTimer += dt;
        if (powerUpTimer >= POWERUP_SPAWN_INTERVAL) {
            powerUpTimer = 0f;
            spawnPowerUp();
        }

        //  Planet animation 
        if (planet != null) planet.update(dt);

        //  Floating text
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y     += 50 * dt;
            ft.timer -= dt;
            ft.color.a = Math.max(0, ft.timer);
            if (ft.timer <= 0) floatingTexts.remove(i);
        }
    }

    // -----------------------------------------------------------------
    // Render
    // -----------------------------------------------------------------

    @Override
    public void render(SpriteBatch batch) {
        //  World space
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Animated planet drawn first
        if (planet != null) planet.draw(batch);

        entityManager.drawEntity(batch);

        // Take a snapshot to avoid ConcurrentModificationException if the
        // entity list is modified mid-frame (e.g. collision deactivates an entity)
        List<Entity> snapshot = new ArrayList<>(entityManager.getEntityList());

        // Asteroid number labels
        for (Entity e : snapshot) {
            if (e.isActive() && e.getTag() != null && e.getTag().startsWith("ASTEROID_")) {
                String num = e.getTag().substring(9);
                outputManager.drawText(batch, num,
                    e.getX() + e.getWidth() / 4f,
                    e.getY() + e.getHeight() * 0.65f, 1.4f);
            }
        }

        // Power-up labels
        for (Entity e : snapshot) {
            if (e.isActive() && e.getTag() != null && e.getTag().startsWith("POWERUP_")) {
                String label = friendlyPowerUpLabel(e.getTag());
                outputManager.drawText(batch, label,
                    e.getX(), e.getY() + POWERUP_SIZE + 4f, 0.9f, Color.CYAN);
            }
        }

        // Floating feedback text
        for (FloatingText ft : floatingTexts) {
            outputManager.drawText(batch, ft.text, ft.x, ft.y, 1.5f, ft.color);
        }

        batch.end();

        //  HUD (screen space) 
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4()
            .setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();

        // ── Singleton: read live values for the HUD ───────────────────
        GameStateManager gsm = GameStateManager.getInstance();
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        // Equation background image + prompt
        float eqBgW = 420f;
        float eqBgH = 52f;
        float eqBgX = sw / 2f - eqBgW / 2f;
        float eqBgY = sh - eqBgH - 8f;
        if (equationBg != null) {
            batch.draw(equationBg, eqBgX, eqBgY, eqBgW, eqBgH);
        }
        outputManager.drawText(batch,
            "Solve: " + equationGenerator.getCurrentEquation(),
            sw / 2f - 120, sh - 25, 2.0f);

        // Stats HUD background image
        if (infoBg != null) {
            batch.draw(infoBg, 10, sh - 185, 280, 180);
        }

        // Score
        outputManager.drawText(batch,
            "SCORE: " + gsm.getScore(),
            20, Gdx.graphics.getHeight() - 20, 1.5f);

        // Timer
        int secs = (int) Math.ceil(gsm.getTimeSeconds());
        Color timerColor = secs <= 20 ? Color.RED : Color.WHITE;
        outputManager.drawText(batch,
            "TIME: " + secs + "s", 20, sh - 55, 1.3f, timerColor);

        // Level
        outputManager.drawText(batch,
            "Level: " + gsm.getLevel(), 20, sh - 85, 1.2f);

        // Lives Label
        outputManager.drawText(batch, "LIVES: ", 20, sh - 112, 1.5f);
        float heartX = 90f, heartY = sh - 137f;
        for (int i = 0; i < GameStateManager.STARTING_LIVES; i++)
            batch.draw(emptyHeart,  heartX + i * 40, heartY, 32, 32);
        for (int i = 0; i < gsm.getLives(); i++)
            batch.draw(filledHeart, heartX + i * 40, heartY, 32, 32);

        // Equations answered counter
        outputManager.drawText(batch,
            "Equations Solved: " + gsm.getEquationsAnswered(),
            20, Gdx.graphics.getHeight() - 150, 1.3f);

        // Pause hint
        outputManager.drawText(batch,
            "[P] Pause", sw - 100, sh - 20, 1.0f);

        // --- Dynamic Distance Progress Bar ---
        float finishLineY = (planetZone != null) ? planetZone.y : mapPixelHeight;
        float progress = rocket.getY() / finishLineY;
        progress = Math.max(0f, Math.min(1f, progress)); 

        float barX = sw - 60f;
        float barBottom = 100f;
        float barTop = sh - 100f;

        for (float y = barBottom; y <= barTop; y += 30f) {
            outputManager.drawText(batch, ".", barX + 12f, y, 1.0f, Color.DARK_GRAY);
        }
        outputManager.drawText(batch, "PLANET", barX - 20f, barTop + 40f, 1.0f, Color.GREEN);
        
        float currentY = barBottom + (progress * (barTop - barBottom));
        batch.draw(progressIcon, barX, currentY - 16f, 32, 32);
        // ------------------------------------------------

        // --- Danger Warning (Pulsing Text) ---
        // Only check if the chaser actually exists and is active
        if (chaser != null && chaser.isActive()) {
            
            // Calculate the exact pixel gap between the rocket and the top edge of the chaser
            float gap = rocket.getY() - (chaser.getY() + chaser.getHeight());
            
            // If the chaser is closer than 450 pixels (about half the screen height)
            if (gap < 450f) { 
                
                // Use a Sine wave based on the game timer to create a smooth pulse (oscillates between 0.3 and 1.0)
                float pulseAlpha = 0.65f + 0.35f * (float) Math.sin(gsm.getTimeSeconds() * 10f);
                Color warningColor = new Color(1f, 0.1f, 0.1f, pulseAlpha); // Bright Red with pulsing transparency
                
                // Draw it near the bottom center of the screen so the player sees it looking down
                outputManager.drawText(batch, "WARNING: ESCAPE THE VOID!", 
                    sw / 2f - 240, 150, 2.5f, warningColor);
            }
        }
        // ------------------------------------------------

        batch.end();

        //  Pause overlay (always last, no-op when not paused) 
        pauseOverlay.render(batch);
    }

    // -----------------------------------------------------------------
    // Dispose
    // -----------------------------------------------------------------

    @Override
    public void dispose() {
        if (progressIcon != null) progressIcon.dispose();
        if (equationBg   != null) equationBg.dispose();
        if (infoBg       != null) infoBg.dispose();
        
        if (correctAnswerSfx != null) {
            correctAnswerSfx.dispose();
            correctAnswerSfx = null;
        }
        if (wrongAnswerSfx != null) {
            wrongAnswerSfx.dispose();
            wrongAnswerSfx = null;
        }
        if (mapRenderer != null) mapRenderer.dispose();
        if (map         != null) map.dispose();
        entityManager.getEntityList().clear();
        entityManager.getPlayableEntityList().clear();
    }

    // -----------------------------------------------------------------
    // Floating text helper
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
    // Private helpers
    // -----------------------------------------------------------------

    /** Spawns the chaser entity well below the rocket's starting position. */
    private void spawnChaser() {
        int level = GameStateManager.getInstance().getLevel();

        String chaserTexture = (level == 2) ? "lava.png" : "blackhole.png";
        float  chaserW       = mapPixelWidth; // full width — unavoidable

        // Read the actual image height so the hitbox matches the sprite exactly
        float chaserH;
        try {
            Pixmap px = new Pixmap(Gdx.files.internal(chaserTexture));
            // Scale image height proportionally to mapPixelWidth so it matches how it renders
            float aspectRatio = (float) px.getHeight() / px.getWidth();
            chaserH = mapPixelWidth * aspectRatio;
            px.dispose();
        } catch (Exception e) {
            System.out.println("[MathGame] Could not read chaser image size, using default.");
            chaserH = 200f; // safe fallback
        }

        // Start 600px below the rocket so the player has breathing room at spawn
        float startY = rocket.getY() - 600f;

        chaser = new NonPlayableEntity(
            chaserTexture,
            0, startY,
            0,                 // speed managed by ChaserBehavior
            chaserW, chaserH,
            "CHASER",
            new ChaserBehavior(),
            new ChaserCollisionBehavior(this),
            rocket
        );
        entityManager.addEntity(chaser);
    }

    /** Spawns a random power-up at a safe position near but above the rocket. */
    private void spawnPowerUp() {
        PowerUpType[] types     = PowerUpType.values();
        PowerUpType   type      = types[ThreadLocalRandom.current().nextInt(types.length)];
        String[]      textures  = { "powerup_time.png", "powerup_life.png", "powerup_multiplier.png" };
        String[]      sounds    = { "sfx_time.wav",     "sfx_life.wav",     "sfx_multiplier.wav"    };
        int           idx       = type.ordinal();

        float spawnX = 0, spawnY = 0;
        boolean safePosFound = false;

        // --- Prevent Power-ups from spawning inside walls ---
        for (int i = 0; i < 20; i++) { // Try up to 20 times to find a safe spot
            spawnY = rocket.getY() + 200 + ThreadLocalRandom.current().nextFloat() * 400;
            spawnY = Math.min(spawnY, mapPixelHeight - POWERUP_SIZE - 100);
            spawnX = ThreadLocalRandom.current().nextFloat() * (mapPixelWidth - POWERUP_SIZE);

            Rectangle puBox = new Rectangle(spawnX, spawnY, POWERUP_SIZE, POWERUP_SIZE);
            boolean hitsWall = false;
            
            for (Rectangle wall : worldColliders) {
                if (puBox.overlaps(wall)) {
                    hitsWall = true;
                    break;
                }
            }
            if (!hitsWall) {
                safePosFound = true;
                break; // Found a good spot, exit the loop
            }
        }
        
        // If the screen is completely full (very rare), just skip spawning this power-up
        if (!safePosFound) return; 
        // ----------------------------------------------------------------

        NonPlayableEntity pu = new NonPlayableEntity(
            textures[idx], spawnX, spawnY, 0,
            POWERUP_SIZE, POWERUP_SIZE,
            "POWERUP_" + type.name(),
            new StationaryMovementBehavior(),
            new PowerUpCollisionBehavior(type, this, outputManager, sounds[idx]),
            rocket
        );
        entityManager.addEntity(pu);
    }

    /** Returns a random answer value that is not the correct answer. */
    private int randomDecoy(int correctAnswer) {
        int val;
        do {
            val = ThreadLocalRandom.current().nextInt(1, 41);
        } while (val == correctAnswer);
        return val;
    }

    private int computeLevel() {
        return Math.max(1, GameStateManager.getInstance().getEquationsAnswered() + 1);
    }

    private String friendlyPowerUpLabel(String tag) {
        if (tag.contains("TIME"))       return "+Time";
        if (tag.contains("EXTRA"))      return "+Life";
        if (tag.contains("MULTIPLIER")) return "2x Score";
        return "?";
    }

    private void loadSfx() {
        try {
            correctAnswerSfx = new AudioSource("correctAns.wav");
            correctAnswerSfx.setVolume(0.25f);
        } catch (Exception e) { correctAnswerSfx = null; }

        try {
            wrongAnswerSfx = new AudioSource("collision.wav");
            wrongAnswerSfx.setVolume(0.2f);
        } catch (Exception e) { wrongAnswerSfx = null; }
    }
}
