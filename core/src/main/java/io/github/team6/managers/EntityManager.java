package io.github.team6.managers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.Entity;



public class EntityManager {
  private List<Entity> entityList;
  private List<Entity> playableEntityList; 

  // Constructor
  public EntityManager() {
		entityList = new ArrayList<>();
    playableEntityList = new ArrayList<>(); // contain only playable entity
	}

  public void addEntity(Entity e) {entityList.add(e); }
  public void addPlayableEntity(Entity e) {playableEntityList.add(e); }

  public void drawEntity(SpriteBatch batch) {
    for (Entity e : entityList) {
      e.draw(batch);
    }
  }

  public List<Entity> getEntityList() {
    return entityList;
  }

  public List<Entity> getPlayableEntityList() {
    return playableEntityList;
  }
}
