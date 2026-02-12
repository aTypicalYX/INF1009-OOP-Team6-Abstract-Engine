package io.github.team6.managers;

import java.util.List;
import io.github.team6.collision.Collision;
import io.github.team6.entities.Entity;

public class CollisionManager {
    private Collision collision;

    public CollisionManager() {
        this.collision = new Collision();
    }

    public void update(List<Entity> entities) {
        for (int i = 0; i < entities.size() -1; i++) {
            for (int j = i+1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);
                collision.checkOverlap(a, b);
            }
        }
    }
}
