package io.github.team6.mathgame;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.team6.managers.SceneManager;
import io.github.team6.scenes.MainMenuScene;
import io.github.team6.scenes.Scene;

/**
 * LeaderboardScene
 * Two modes controlled by the constructor:
 *
 *   POST_GAME mode (scoreToSave > 0):
 *     Shows a name-entry TextField, then saves the score and refreshes
 *     the list. Used immediately after VictoryScene.
 *
 *   VIEW mode (scoreToSave = -1):
 *     Shows the leaderboard read-only. Used from the main menu.
 *
 * OOP Concepts:
 * - Inheritance    : Extends Scene — standard lifecycle.
 * - Composition    : Owns a LeaderboardManager instance.
 * - State Pattern  : Mode (post-game vs view) drives which UI is shown.
 * - Observer Pattern: Scene2D ChangeListeners on buttons.
 */
public class LeaderboardScene extends Scene {

    private final SceneManager      scenes;
    private final LeaderboardManager leaderboard;

    // POST_GAME mode fields
    private final int  scoreToSave;   // -1 = view mode
    private final int  levelReached;

    private Stage     stage;
    private Skin      skin;
    private TextField nameField;
    private Table     scoreTable;     // rebuilt after name submission

    private int lastW = -1, lastH = -1;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /** View mode — called from main menu. */
    public LeaderboardScene(SceneManager scenes) {
        this(scenes, -1, 1);
    }

    /** Post-game mode — called from VictoryScene after a win. */
    public LeaderboardScene(SceneManager scenes, int scoreToSave, int levelReached) {
        this.scenes       = scenes;
        this.scoreToSave  = scoreToSave;
        this.levelReached = levelReached;
        this.leaderboard  = new LeaderboardManager();
    }

    // -----------------------------------------------------------------------
    // Scene lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onEnter() {
        outputManager.stopBgm();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        lastW = Gdx.graphics.getWidth();
        lastH = Gdx.graphics.getHeight();
        stage.getViewport().update(lastW, lastH, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        buildUI(false);
    }

    @Override
    public void update(float dt) { stage.act(dt); }

    @Override
    public void render(SpriteBatch batch) {
        int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        if (w != lastW || h != lastH) {
            lastW = w; lastH = h;
            stage.getViewport().update(w, h, true);
        }
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }

    // -----------------------------------------------------------------------
    // UI
    // -----------------------------------------------------------------------

    private void buildUI(boolean justSaved) {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.center().pad(20);

        // Title
        Label title = new Label("Leaderboard", skin);
        title.setFontScale(2.2f);
        title.setAlignment(Align.center);
        root.add(title).colspan(3).padBottom(10).row();

        // Name entry — only shown in post-game mode BEFORE the score is saved
        if (scoreToSave >= 0 && !justSaved) {
            Label prompt = new Label("You scored " + scoreToSave + "! Enter your name:", skin);
            prompt.setAlignment(Align.center);
            root.add(prompt).colspan(3).padBottom(6).row();

            nameField = new TextField("", skin);
            nameField.setMessageText("Your name...");
            nameField.setMaxLength(12);
            root.add(nameField).colspan(3).width(280).height(50).padBottom(6).row();

            TextButton submitBtn = new TextButton("Submit", skin);
            submitBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Play click sound
                    outputManager.playUiClick();

                    String name = nameField.getText().trim();
                    if (name.isEmpty()) name = "???";
                    leaderboard.addEntry(name, scoreToSave, levelReached);
                    buildUI(true); // Rebuild to show updated list
                }
            });
            root.add(submitBtn).colspan(3).width(180).height(50).padBottom(16).row();
        }

        // Column headers
        root.add(new Label("#",     skin)).width(40).left();
        root.add(new Label("Name",  skin)).expandX().left();
        root.add(new Label("Score", skin)).width(80).right();
        root.row().padBottom(4);

        // Score rows
        List<LeaderboardManager.ScoreEntry> entries = leaderboard.getEntries();
        if (entries.isEmpty()) {
            Label empty = new Label("No scores yet — be the first!", skin);
            empty.setAlignment(Align.center);
            root.add(empty).colspan(3).padTop(12).row();
        } else {
            String[] medals = {"1.", "2.", "3.", "4.", "5."};
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardManager.ScoreEntry e = entries.get(i);
                root.add(new Label(medals[i],         skin)).width(40).left().padBottom(6);
                root.add(new Label(e.name + "  (Lv." + e.level + ")", skin)).expandX().left().padBottom(6);
                root.add(new Label(String.valueOf(e.score), skin)).width(80).right().padBottom(6);
                root.row();
            }
        }

        // Buttons
        root.add(new Label("", skin)).colspan(3).height(20).row(); // spacer

        TextButton menuBtn = new TextButton("Main Menu", skin);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();

                GameStateManager.getInstance().reset();
                scenes.setScene(new MainMenuScene(scenes));
            }
        });

        if (scoreToSave >= 0) {
            // --- Offer "Next Level" or "Play Again" dynamically ---
            boolean hasNextLevel = levelReached < 2;
            TextButton nextBtn = new TextButton(hasNextLevel ? "Next Level" : "Play Again", skin);
            
            nextBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    
                    outputManager.playUiClick();

                    // If the player just completed Level 1, offer the intro cutscene for Level 2.
                    if (hasNextLevel) {
                        // Progress to the intro cutscene for the next level (Level 2)
                        scenes.setScene(new IntroScene(scenes, levelReached + 1));
                    } else {
                        // Game fully complete! Reset the singleton and start from Level 1
                        GameStateManager.getInstance().reset();
                        scenes.setScene(new MathGameScene(scenes));
                    }
                }
            });
            
            Table btnRow = new Table();
            btnRow.add(nextBtn).width(200).height(55).padRight(16);
            btnRow.add(menuBtn).width(200).height(55);
            root.add(btnRow).colspan(3).padTop(10).row();
        } else {
            // View Mode (From Main Menu) - Just show the Main Menu button
            root.add(menuBtn).colspan(3).width(200).height(55).padTop(10).row();
        }

        stage.addActor(root);
    }
}
