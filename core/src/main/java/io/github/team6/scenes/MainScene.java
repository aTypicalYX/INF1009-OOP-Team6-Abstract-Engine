package io.github.team6.scenes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.NonPlayableEntity;
import io.github.team6.entities.PlayableEntity;

public class MainScene extends Scene {
    
    private SpriteBatch batch; // Each scene can manage its own rendering batch

    @Override
    public void onEnter() {
        System.out.println("Entering Main Scene...");
        batch = new SpriteBatch();

        // Create the entities
        PlayableEntity bucket = new PlayableEntity("bucket.png", 100, 220, 5, 50, 50);
        NonPlayableEntity droplet = new NonPlayableEntity("droplet.png", 250, 220, 5, 50, 50);

        // Add them to the EntityManager (inherited from Scene class)
        entityManager.addEntity(bucket);
        entityManager.addPlayableEntity(bucket);
        entityManager.addEntity(droplet);
    }

    @Override
    public void update(float dt) {
        // Run the Managers
        inputManager.update(entityManager.getPlayableEntityList());
        movementManager.update(entityManager.getEntityList());
        collisionManager.update(entityManager.getEntityList());
    }

    @Override
    public void render() {
        batch.begin();
        entityManager.drawEntity(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        System.out.println("Disposed of scene...");
        batch.dispose();
        // Clear entities when leaving the scene so they don't persist to the Menu
        entityManager.getEntityList().clear(); 
        entityManager.getPlayableEntityList().clear();
    }
}