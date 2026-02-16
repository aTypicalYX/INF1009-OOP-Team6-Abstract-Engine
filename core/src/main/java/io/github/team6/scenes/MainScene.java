package io.github.team6.scenes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.NonPlayableEntity.DropletType;
import io.github.team6.entities.PlayableEntity;
import io.github.team6.entities.behavior.ChasingMovementBehavior;
import io.github.team6.entities.behavior.PermanentCollisionBehavior;
import io.github.team6.entities.behavior.StationaryMovementBehavior;
import io.github.team6.inputoutput.MusicSource;
import io.github.team6.managers.SceneManager;

public class MainScene extends Scene {

    private final SceneManager scenes;
    private BitmapFont font;

    private float timeSurvived;
    private int score;

    public MainScene(SceneManager scenes) {
        this.scenes = scenes;
    }
    
    private static final int PERMANENT_STATIONARY_COUNT = 2;
    private static final int CHASING_COUNT = 2;

    private static final float PERMANENT_DROPLET_WIDTH = 50f;
    private static final float PERMANENT_DROPLET_HEIGHT = 50f;
    private static final float CHASING_DROPLET_WIDTH = 30f;
    private static final float CHASING_DROPLET_HEIGHT = 30f;
    private static final float CHASING_DROPLET_SPEED = 0.5f;

    //private SpriteBatch batch;
    private PlayableEntity bucket;
    private List<NonPlayableEntity> permanentObstacles;

    @Override
    public void onEnter() {
        System.out.println("Entering Main Scene...");
        font = new BitmapFont(); 

        // Create the entities
        //PlayableEntity bucket = new PlayableEntity("bucket.png", 100, 220, 5, 50, 50);
        //NonPlayableEntity droplet = new NonPlayableEntity("droplet.png", 250, 220, 5, 50, 50);

        // Add them to the EntityManager (inherited from Scene class)
        bucket = new PlayableEntity("bucket.png", 100, 220, 5, 50, 50);
        bucket.setOutputManager(outputManager); // Set OutputManager to enable collision sound
        entityManager.addEntity(bucket);
        entityManager.addPlayableEntity(bucket);
        //entityManager.addEntity(droplet);
        permanentObstacles = new ArrayList<>();

        for (int i = 0; i < PERMANENT_STATIONARY_COUNT; i++) {
            NonPlayableEntity permanentStationaryDroplet = createPermanentStationaryDroplet();
            entityManager.addEntity(permanentStationaryDroplet);
            permanentObstacles.add(permanentStationaryDroplet);
        }

        for (int i = 0; i < CHASING_COUNT; i++) {
            entityManager.addEntity(createChasingDroplet());
        }

        // bgm for game
        try {
            MusicSource gameBgm = new MusicSource("background.wav");
            outputManager.setBgm(gameBgm);
            outputManager.playBgm(true); // true = loop
            System.out.println("[DEBUG] Game background.wav loaded and playing");
        } catch (Exception e) {
            System.out.println("[DEBUG] Warning: background.wav not found. Background music disabled.");
            e.printStackTrace();
        }
    }

    @Override
    public void update(float dt) {
        // Press ESC to go back to main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            scenes.setScene(new MainMenuScene(scenes));
            return;
        }
        // Run the Managers
        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());
        entityManager.removeInactiveEntities();
        
        // Increase score based on survival time
        timeSurvived += dt;
        score = (int) timeSurvived * 10;
        
    }

    private NonPlayableEntity createPermanentStationaryDroplet() {
        float[] position = getSafeSpawnPosition(PERMANENT_DROPLET_WIDTH, PERMANENT_DROPLET_HEIGHT);
        return new NonPlayableEntity(
                "droplet.png", position[0], position[1], 0, PERMANENT_DROPLET_WIDTH, PERMANENT_DROPLET_HEIGHT,
                new StationaryMovementBehavior(),
                new PermanentCollisionBehavior(),
                bucket,
                DropletType.PERMANENT_STATIONARY);
    }

    private NonPlayableEntity createChasingDroplet() {
        float[] position = getSafeSpawnPosition(CHASING_DROPLET_WIDTH, CHASING_DROPLET_HEIGHT);
        return new NonPlayableEntity(
                "droplet.png", position[0], position[1], CHASING_DROPLET_SPEED, CHASING_DROPLET_WIDTH, CHASING_DROPLET_HEIGHT,
                new ChasingMovementBehavior(permanentObstacles),
                new PermanentCollisionBehavior(),
                bucket,
                DropletType.CHASING);
    }

    private float[] getSafeSpawnPosition(float width, float height) {
        float maxX = Math.max(0, Gdx.graphics.getWidth() - width);
        float maxY = Math.max(0, Gdx.graphics.getHeight() - height);

        for (int attempt = 0; attempt < 20; attempt++) {
            float randomX = ThreadLocalRandom.current().nextFloat() * maxX;
            float randomY = ThreadLocalRandom.current().nextFloat() * maxY;

            boolean intersectsBucket = randomX < bucket.getX() + bucket.getWidth()
                    && randomX + width > bucket.getX()
                    && randomY < bucket.getY() + bucket.getHeight()
                    && randomY + height > bucket.getY();

            if (!intersectsBucket) {
                return new float[] { randomX, randomY };
            }
        }

        return new float[] { 0, 0 };
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        font.draw(batch, "Arrow Keys to move", 40, Gdx.graphics.getHeight() - 40);
        font.draw(batch, "ESC to return to menu", 40, Gdx.graphics.getHeight() - 80);
        entityManager.drawEntity(batch);

        // DRAW HUD
        font.setColor(1, 1, 1, 1); // White
        font.getData().setScale(1.5f);
        font.draw(batch, "SCORE: " + score, 20, Gdx.graphics.getHeight() - 20);

        batch.end();
    }

    @Override
    public void dispose() {
        System.out.println("Disposed of scene...");
        font.dispose();
        // Clear entities when leaving the scene so they don't persist to the Menu
        entityManager.getEntityList().clear(); 
        entityManager.getPlayableEntityList().clear();

    }
}