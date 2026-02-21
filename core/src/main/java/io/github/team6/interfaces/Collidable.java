

package io.github.team6.interfaces;
import com.badlogic.gdx.math.Rectangle;

import io.github.team6.entities.Entity;

// ensures entities provide collision logic
public interface Collidable {
    public Rectangle getHitbox();
    public void onCollision(Entity other);
}
