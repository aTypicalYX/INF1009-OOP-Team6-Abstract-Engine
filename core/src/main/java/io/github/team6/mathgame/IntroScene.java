package io.github.team6.mathgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.Scene;

/**
 * IntroScene
 * Displays a series of story + tutorial text panels after the player presses Start Game.
 * Each panel types itself out character by character (typewriter effect).
 *
 * Controls:
 *   - SPACE or ENTER  : advance (or snap text if still typing)
 *   - Next button     : same as SPACE / ENTER
 *   - Skip button     : jumps immediately to MathGameScene
 *
 * Level context:
 *   - levelContext 1  : 7-panel Level 1 intro (story + tutorial)
 *   - levelContext 2  : 4-panel Level 2 cutscene (volcano crash + new mechanics)
 *
 * === Design Patterns Used ===
 *
 * 1. STATE PATTERN
 *    currentPanel drives which content is displayed each frame.
 *    Advancing the panel is equivalent to a state transition.
 *
 * 2. SINGLETON (GameStateManager)
 *    setLevel() is called through the Singleton before launching MathGameScene
 *    so the correct level context carries through.
 *
 * 3. OBSERVER PATTERN
 *    Scene2D ChangeListeners on the Next and Skip buttons react to player input
 *    without polling, consistent with the rest of the codebase.
 */
public class IntroScene extends Scene {

    // -----------------------------------------------------------------
    // Level 1 panels  { title, body }
    // -----------------------------------------------------------------
    private static final String[][] LEVEL1_PANELS = {
        {
            "The Year is 2157",
            "You are a lost astronaut drifting through the vast emptiness of space.\n\n" +
            "Your ship is running on fumes, and the stars offer no comfort."
        },
        {
            "Danger Approaches",
            "A dark hole is encroaching your spaceship, consuming everything in its path.\n\n" +
            "Then - a faint signal. It's coming from one of the planets above."
        },
        {
            "The Equation Engine",
            "Thankfully, your ship runs on an equation engine.\n\n" +
            "And the world is made of math - including the asteroids floating around you!\n\n" +
            "Collect the correct asteroid to fuel your journey home."
        },
        {
            "How to Move",
            "Use the ARROW KEYS to fly your spaceship in any direction.\n\n" +
            "Navigate carefully - the further up you travel, the closer you are to safety."
        },
        {
            "Solving Equations",
            "Each round, an equation appears at the top of the screen.\n\n" +
            "Asteroids float around you - each carrying a number.\n\n" +
            "Fly into the asteroid with the CORRECT ANSWER to collect fuel.\n\n" +
            "Hit a WRONG answer and you lose a life!"
        },
        {
            "Power-Ups",
            "Keep an eye out for glowing power-ups drifting through space. Fly into them to collect!\n\n" +
            "+Time         - extends your survival timer\n\n" +
            "+Life          - restores one lost life\n\n" +
            "2x Score   - doubles points for your next correct answer"
        },
        {
            "Watch Your Back",
            "The black hole below is rising - and it gets faster over time.\n\n" +
            "If it catches you, it's game over instantly.\n\n" +
            "Press P or ESC at any time to PAUSE the game.\n\n" +
            "Good luck, astronaut. The planet is counting on you."
        }
    };

    // -----------------------------------------------------------------
    // Level 2 panels  { title, body }
    // -----------------------------------------------------------------
    private static final String[][] LEVEL2_PANELS = {
        {
            "Phew... You Made It.",
            "Against all odds, you navigated through the asteroid field and reached the planet.\n\n" +
            "But the landing was rough. You've crashed into the slopes of an active volcano."
        },
        {
            "A New Threat",
            "The lava is rising fast - and it won't stop for anyone.\n\n" +
            "You need to launch your ship immediately and make your escape before it's too late."
        },
        {
            "The Equations Have Changed",
            "Level 2 introduces subtraction alongside addition.\n\n" +
            "Keep a sharp eye on the equation at the top of the screen.\n\n" +
            "The debris floating around you still holds the answers - " +
            "collect the correct one to keep your engines burning."
        },
        {
            "Stay Sharp",
            "The lava rises faster than the black hole did.\n\n" +
            "Power-ups are still scattered across the map - use them wisely.\n\n" +
            "Reach the planet above to escape. Good luck, astronaut.\n\n" +
            "You're going to need it."
        }
    };

    // -----------------------------------------------------------------
    // State
    // -----------------------------------------------------------------
    private int   currentPanel = 0;

    // Typewriter effect
    private float typeTimer  = 0f;
    private int   charsShown = 0;
    private static final float CHARS_PER_SECOND = 40f;

    // -----------------------------------------------------------------
    // Scene2D
    // -----------------------------------------------------------------
    private Stage      stage;
    private Skin       skin;
    private Label      titleLabel;
    private Label      bodyLabel;
    private Label      pageLabel;
    private TextButton nextBtn;
    private PauseOverlay pauseOverlay;

    // Power-up icons - loaded for the power-up tutorial panel
    private com.badlogic.gdx.graphics.Texture iconTime;
    private com.badlogic.gdx.graphics.Texture iconLife;
    private com.badlogic.gdx.graphics.Texture iconMultiplier;
    private static final int POWERUP_PANEL_INDEX = 5; // index of the power-up panel in LEVEL1_PANELS

    private int lastWidth  = -1;
    private int lastHeight = -1;

    private final SceneManager scenes;
    private final int          levelContext; // 1 = level 1 intro, 2 = level 2 cutscene

    // =================================================================

    /** Standard intro - played before Level 1. */
    public IntroScene(SceneManager scenes) {
        this(scenes, 1);
    }

    /** Level-aware constructor - pass 2 for the Level 2 cutscene. */
    public IntroScene(SceneManager scenes, int levelContext) {
        this.scenes       = scenes;
        this.levelContext = levelContext;
    }

    // -----------------------------------------------------------------
    // Scene lifecycle
    // -----------------------------------------------------------------

    @Override
    public void onEnter() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        lastWidth  = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
        stage.getViewport().update(lastWidth, lastHeight, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Pause overlay - allows the player to pause during the cutscene
        pauseOverlay = new PauseOverlay(scenes, outputManager);
        // Register the intro's Stage so button clicks are restored after unpausing
        pauseOverlay.setFallbackProcessor(stage);

        // Load power-up icons for the tutorial panel (safe - won't crash if missing)
        try { iconTime       = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("powerup_time.png")); }       catch (Exception e) { iconTime = null; }
        try { iconLife       = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("powerup_life.png")); }       catch (Exception e) { iconLife = null; }
        try { iconMultiplier = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("powerup_multiplier.png")); } catch (Exception e) { iconMultiplier = null; }

        buildUI();
        loadPanel(0);
    }

    @Override
    public void update(float dt) {
        // Pause toggle - P or ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseOverlay.toggle();
        }
        if (pauseOverlay.isPaused()) return;

        stage.act(dt);

        // Typewriter tick - advance characters shown over time
        String fullBody = activePanels()[currentPanel][1];
        if (charsShown < fullBody.length()) {
            typeTimer += dt;
            charsShown = Math.min(fullBody.length(), (int)(typeTimer * CHARS_PER_SECOND));
            bodyLabel.setText(fullBody.substring(0, charsShown));
            nextBtn.setText("Skip text");
        } else {
            // Text fully shown - update button label for context
            boolean isLastPanel = currentPanel >= activePanels().length - 1;
            nextBtn.setText(isLastPanel ? "Let's Go!" : "Next  ›");
        }

        // Keyboard shortcut: SPACE or ENTER advances the panel
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            advance();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Respond to window resize
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        if (w != lastWidth || h != lastHeight) {
            lastWidth  = w;
            lastHeight = h;
            stage.getViewport().update(w, h, true);
        }

        // Dark space background - consistent with MathGameScene
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();

        // Draw power-up icons on the power-up tutorial panel (Level 1 only)
        // Three icons drawn in a horizontal row, centred at the top of the screen
        if (levelContext == 1 && currentPanel == POWERUP_PANEL_INDEX) {
            float iconSize  = 64f;
            float iconGap   = 120f;  // gap between icon centres
            float totalW    = iconSize + iconGap * 2;
            float startX    = w / 2f - totalW / 2f;   // centre the trio
            float iconY     = h - 160f;                // top area, below the panel title zone

            batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4()
                .setToOrtho2D(0, 0, w, h));
            batch.begin();
            if (iconTime       != null) batch.draw(iconTime,       startX,              iconY, iconSize, iconSize);
            if (iconLife       != null) batch.draw(iconLife,       startX + iconGap,    iconY, iconSize, iconSize);
            if (iconMultiplier != null) batch.draw(iconMultiplier, startX + iconGap * 2, iconY, iconSize, iconSize);
            batch.end();
        }

        // Pause overlay - drawn last so it appears on top, no-op when not paused
        pauseOverlay.render(batch);
    }

    @Override
    public void dispose() {
        if (stage        != null) stage.dispose();
        if (skin         != null) skin.dispose();
        if (pauseOverlay != null) pauseOverlay.dispose();
        if (iconTime       != null) iconTime.dispose();
        if (iconLife       != null) iconLife.dispose();
        if (iconMultiplier != null) iconMultiplier.dispose();
    }

    // -----------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------

    private void buildUI() {
        stage.clear();

        // Title label
        titleLabel = new Label("", skin);
        titleLabel.setFontScale(2.8f);
        titleLabel.setAlignment(Align.center);

        // Body label - wraps text, centred
        bodyLabel = new Label("", skin);
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.center);
        bodyLabel.setFontScale(1.4f);

        // Page counter  e.g. "1 / 7"
        pageLabel = new Label("", skin);
        pageLabel.setAlignment(Align.center);

        // Next / Let's Go button
        nextBtn = new TextButton("Next  ›", skin);
        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                advance();
            }
        });

        // Skip Intro button
        TextButton skipBtn = new TextButton("Skip Intro", skin);
        skipBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                launchGame();
            }
        });

        // Root layout table - content centred vertically and horizontally
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.pad(60);

        // Decorative divider
        root.add(new Label("✦  ✦  ✦", skin)).center().padBottom(20).row();

        root.add(titleLabel).center().padBottom(30).row();

        // Body text - generous width, centred, with space above and below
        root.add(bodyLabel).width(900).center().padBottom(40).row();

        // Page counter centred below body
        root.add(pageLabel).center().padBottom(12).row();

        // Buttons centred below page counter
        Table btnRow = new Table();
        btnRow.add(skipBtn).width(160).height(50).padRight(12);
        btnRow.add(nextBtn).width(160).height(50);
        root.add(btnRow).center().row();

        stage.addActor(root);
    }

    // -----------------------------------------------------------------
    // Panel helpers
    // -----------------------------------------------------------------

    private void loadPanel(int index) {
        currentPanel = index;
        charsShown   = 0;
        typeTimer    = 0f;

        String[][] panels = activePanels();
        titleLabel.setText(panels[index][0]);
        bodyLabel.setText("");
        pageLabel.setText((index + 1) + " / " + panels.length);
    }

    private void advance() {
        String fullBody = activePanels()[currentPanel][1];

        // If still typing, snap to full text first (one click to finish, next click to advance)
        if (charsShown < fullBody.length()) {
            charsShown = fullBody.length();
            bodyLabel.setText(fullBody);
            return;
        }

        // Move to next panel or launch game
        if (currentPanel < activePanels().length - 1) {
            loadPanel(currentPanel + 1);
        } else {
            launchGame();
        }
    }

    private void launchGame() {
        // Set level on the Singleton before handing off to MathGameScene
        GameStateManager.getInstance().setLevel(levelContext);
        scenes.setScene(new MathGameScene(scenes));
    }

    /** Returns the correct panel array based on which level intro is playing. */
    private String[][] activePanels() {
        return levelContext == 2 ? LEVEL2_PANELS : LEVEL1_PANELS;
    }
}
