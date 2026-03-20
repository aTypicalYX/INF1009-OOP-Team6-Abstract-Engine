package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * PlanetEntity
 * Draws an animated spinning planet at the top of the map using a sprite sheet.
 * Not an Entity subclass - it is a simple self-contained renderer owned by
 * MathGameScene, drawn in world space after all other entities.
 *
 * Sprite sheet: planet_sheet.png
 *   - 60 frames in a single horizontal row
 *   - Each frame is 128 x 128 px
 *
 * OOP Concepts:
 * - Composition  : Owned by MathGameScene ("has-a" PlanetEntity).
 * - Encapsulation: Texture, animation, and timer are all private.
 * - Single Responsibility: Only handles planet rendering.
 */
public class PlanetEntity {

    //private static final int   FRAME_COUNT  = 60;
    private static final int   FRAME_W      = 128;
    private static final int   FRAME_H      = 128;
    private static final float FRAME_DURATION = 0.05f; // seconds per frame (~20 fps spin)

    private final Texture                  sheet;
    private final Animation<TextureRegion> animation;

    private final float drawX;   // world X to draw at
    private final float drawY;   // world Y to draw at
    private final float drawW;   // rendered width  (can differ from FRAME_W for scaling)
    private final float drawH;   // rendered height

    private float stateTime = 0f;

    /**
     * @param centerX  World X centre of the planet.
     * @param bottomY  World Y of the bottom edge of the planet.
     * @param size     Rendered size in pixels (width and height).
     */
    public PlanetEntity(float centerX, float bottomY, float size) {
        sheet = new Texture(Gdx.files.internal("planet_sheet.png"));

        // Slice the sheet into individual frames
        TextureRegion[][] tmp = TextureRegion.split(sheet, FRAME_W, FRAME_H);
        TextureRegion[]   frames = tmp[0]; // single row

        animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        drawW = size;
        drawH = size;
        drawX = centerX - size / 2f;
        drawY = bottomY;
    }

    /**
     * Updates the animation timer. Call from MathGameScene.update().
     * @param dt Delta time in seconds.
     */
    public void update(float dt) {
        stateTime += dt;
    }

    /**
     * Draws the current animation frame.
     * Must be called between batch.begin() / batch.end() in world space.
     */
    public void draw(SpriteBatch batch) {
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        batch.draw(frame, drawX, drawY, drawW, drawH);
    }

    /** Returns the bounding box Y for win-zone collision reference. */
    public float getTopY()    { return drawY + drawH; }
    public float getBottomY() { return drawY; }
    public float getCenterX() { return drawX + drawW / 2f; }

    /** Release GPU resources. Call from MathGameScene.dispose(). */
    public void dispose() {
        sheet.dispose();
    }
}
