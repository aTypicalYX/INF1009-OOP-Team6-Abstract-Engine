package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.team6.entities.Entity;
import io.github.team6.managers.OutputManager;

/**
 * HUDRenderer
 * Handles ALL heads-up display drawing for MathGameScene.
 * MathGameScene is responsible for game logic; HUDRenderer is responsible
 * for drawing the UI overlay on top of the game world.
 *
 * Design Patterns Notes:
 * - Single Responsibility Principle (SRP): extracted from MathGameScene to its own class.
 * - Singleton: reads live game state from GameStateManager.getInstance() so it always shows authoritative values without needing state passed in.
 */
public class HUDRenderer {

    private final OutputManager    outputManager;
    private final EquationGenerator equationGenerator;

    // HUD textures - owned by MathGameScene, passed in at construction
    private final Texture filledHeart;
    private final Texture emptyHeart;
    private final Texture progressIcon;
    private final Texture equationBg;
    private final Texture infoBg;

    // World Y of the planet/win zone - used for the progress bar
    private final float finishLineY;
    public HUDRenderer(OutputManager outputManager,
                       EquationGenerator equationGenerator,
                       Texture filledHeart,
                       Texture emptyHeart,
                       Texture progressIcon,
                       Texture equationBg,
                       Texture infoBg,
                       float finishLineY) {
        this.outputManager     = outputManager;
        this.equationGenerator = equationGenerator;
        this.filledHeart       = filledHeart;
        this.emptyHeart        = emptyHeart;
        this.progressIcon      = progressIcon;
        this.equationBg        = equationBg;
        this.infoBg            = infoBg;
        this.finishLineY       = finishLineY;
    }

    public void render(SpriteBatch batch, float rocketY, Entity chaser) {

        // Singleton: read live values for the HUD
        GameStateManager gsm = GameStateManager.getInstance();
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        // Equation background image + prompt
        float eqBgW = 560f;
        float eqBgH = 70f;
        float eqBgX = sw / 2f - eqBgW / 2f;
        float eqBgY = sh - eqBgH - 8f;
        if (equationBg != null) {
            batch.draw(equationBg, eqBgX, eqBgY, eqBgW, eqBgH);
        }
        outputManager.drawText(batch,
            "Solve: " + equationGenerator.getCurrentEquation(),
            sw / 2f - 150, sh - 25, 3f);

        // Stats HUD background image
        if (infoBg != null) {
            batch.draw(infoBg, 8, sh - 210, 360, 210);
        }

        // Score
        outputManager.drawText(batch,
            "SCORE: " + gsm.getScore(),
            20, Gdx.graphics.getHeight() - 20, 2.5f);

        // Timer
        int secs = (int) Math.ceil(gsm.getTimeSeconds());
        Color timerColor = secs <= 20 ? Color.RED : Color.WHITE;
        outputManager.drawText(batch,
            "TIME: " + secs + "s", 20, sh - 55, 1.8f, timerColor);

        // Level
        outputManager.drawText(batch,
            "Level: " + gsm.getLevel(), 20, sh - 85, 1.8f);

        // Draw Streak Indicator if player is doing well
        if (gsm.getCurrentStreak() >= 3) {
            outputManager.drawText(batch,
                "STREAK! 2X POINTS!", sw / 2f - 130, sh - 75, 2.0f, Color.ORANGE);
        }

        // Lives Label
        outputManager.drawText(batch, "LIVES: ", 20, sh - 115, 1.8f);
        float heartX = 100f, heartY = sh - 140f;
        for (int i = 0; i < GameStateManager.STARTING_LIVES; i++)
            batch.draw(emptyHeart, heartX + i * 40, heartY, 32, 32);
        for (int i = 0; i < gsm.getLives(); i++)
            batch.draw(filledHeart, heartX + i * 40, heartY, 32, 32);

        // Equations answered counter
        outputManager.drawText(batch,
            "Equations Solved: " + gsm.getEquationsAnswered(),
            20, Gdx.graphics.getHeight() - 150, 2.0f);

        // Pause text
        outputManager.drawText(batch,
            "[P] Pause", sw - 100, sh - 20, 1.0f);

        // Dynamic Distance Progress Bar
        float progress = rocketY / finishLineY;
        progress = Math.max(0f, Math.min(1f, progress));

        float barX      = sw - 60f;
        float barBottom = 100f;
        float barTop    = sh - 100f;

        for (float y = barBottom; y <= barTop; y += 30f) {
            outputManager.drawText(batch, ".", barX + 12f, y, 1.0f, Color.DARK_GRAY);
        }
        outputManager.drawText(batch, "PLANET", barX - 20f, barTop + 40f, 1.0f, Color.GREEN);

        float currentY = barBottom + (progress * (barTop - barBottom));
        batch.draw(progressIcon, barX, currentY - 16f, 32, 32);

        // Danger Warning (Pulsing Text)
        // Only check if the chaser actually exists and is active
        if (chaser != null && chaser.isActive()) {

            // Calculate the exact pixel gap between the rocket and the top edge of the chaser
            float gap = rocketY - (chaser.getY() + chaser.getHeight());

            // If the chaser is closer than 450 pixels (about half the screen height)
            if (gap < 450f) {

                // Use a Sine wave based on the game timer to create a smooth pulse
                float pulseAlpha = 0.65f + 0.35f * (float) Math.sin(gsm.getTimeSeconds() * 10f);
                Color warningColor = new Color(1f, 0.1f, 0.1f, pulseAlpha); // Bright Red with pulsing transparency

                // Draw it near the bottom center of the screen so the player sees it looking down
                outputManager.drawText(batch, "WARNING: ESCAPE THE VOID!",
                    sw / 2f - 240, 150, 2.5f, warningColor);
            }
        }
    }
}
