package io.github.team6.managers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.Entity;

/**
 * EntityManager keeps track of all active entities in the scene.
 * It allows other systems (like CollisionManager) to request the list of entities to process.
 * OOP Concepts & Design Patterns:
 * - Aggregation: Maintains lists of Entity objects without strictly owning their definitions.
 * - Data Segregation (Optimization): Keeps a separate `playableEntityList` so the InputManager doesn't 
 * have to filter through hundreds of non-playable entities (like droplets or walls) every frame.
 * - Single Responsibility Principle (SRP): Solely responsible for adding, removing, and storing entities. 
 * It does not move them or check their collisions.
 */

public class EntityManager {
  // Master list of all entities
  private List<Entity> entityList;

  // Specific list for entities that the player controls (Buckets)
  private List<Entity> playableEntityList; 

  // Constructor
  public EntityManager() {
		entityList = new ArrayList<>();
    playableEntityList = new ArrayList<>(); // contain only playable entity
	}

  public void addEntity(Entity e) {entityList.add(e); }
  public void addPlayableEntity(Entity e) {playableEntityList.add(e); }

  // drawEntity() handles the rendering loop. It tells every entity to draw itself onto the supplied SpriteBatch.
  public void drawEntity(SpriteBatch batch) {
    for (Entity e : entityList) {
      e.draw(batch);
    }
  }

  public void removeInactiveEntities() {
    entityList.removeIf(entity -> !entity.isActive()); // Remove entities that are no longer active
    playableEntityList.removeIf(entity -> !entity.isActive()); // Ensure playable entities are also removed from the playable list
  }

  // Getters for other Managers to access the data
  public List<Entity> getEntityList() {
    return entityList;
  }

  public List<Entity> getPlayableEntityList() {
    return playableEntityList;
  }
}
