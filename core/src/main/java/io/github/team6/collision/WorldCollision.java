package io.github.team6.collision;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;

import io.github.team6.entities.PlayableEntity;

/**
 * Class: WorldCollision
 * A utility class responsible for resolving physics between dynamic entities and static map geometry.
 * * OOP Concepts & Design Patterns:
 * - Single Responsibility Principle: Isolates the complex axis-by-axis collision resolution 
 * (needed to prevent players from getting stuck in walls) away from the basic entity to entity Collision class.
 * - Stateless Utility: Designed with static methods that do not hold or modify internal state. It operates 
 * purely on the objects passed into it (PlayableEntity and Rectangle), minimizing memory overhead and preventing unintended side-effects across frames.
 */
public class WorldCollision {

    // PlayableEntity collides with rectangles drawn in Tiled "Collisions" object layer
    public static void resolvePlayerVsWorld(PlayableEntity player, float prevX, float prevY,
                                           List<Rectangle> worldColliders,
                                           float worldWidth, float worldHeight) {

        // Clamp player to MAP bounds first
        float maxX = worldWidth - player.getWidth();
        float maxY = worldHeight - player.getHeight();
        player.setX(Math.max(0, Math.min(player.getX(), maxX)));
        player.setY(Math.max(0, Math.min(player.getY(), maxY)));

        Rectangle p = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        for (Rectangle c : worldColliders) {
            if (!p.overlaps(c)) continue;

            // Resolve by axis using previous position
            Rectangle testX = new Rectangle(prevX, player.getY(), player.getWidth(), player.getHeight());
            Rectangle testY = new Rectangle(player.getX(), prevY, player.getWidth(), player.getHeight());

            boolean xCollides = testX.overlaps(c);
            boolean yCollides = testY.overlaps(c);

            if (!xCollides) {
                player.setX(prevX);
            } else if (!yCollides) {
                player.setY(prevY);
            } else {
                player.setX(prevX);
                player.setY(prevY);
            }

            p.setPosition(player.getX(), player.getY());
        }
    }

}
