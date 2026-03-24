package io.github.team6.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import io.github.team6.managers.SceneManager;
import io.github.team6.mathgame.HowToPlayScene;
import io.github.team6.mathgame.IntroScene;
import io.github.team6.mathgame.LeaderboardScene;

public class MainMenuScene extends Scene {

    private final SceneManager scenes;

    private Stage stage; // Scene2D container for UI elements
    private Skin skin;   // JSON style definitions for buttons/fonts
    private Texture logoTexture;
    private Image logoImage;
    private Texture planetTexture;
    private AnimatedImage planetImage;
    private Group logoGroup;

    // Track window size so we can update the Stage viewport on resize (e.g. maximize)
    private int lastWidth = -1;
    private int lastHeight = -1;

    public MainMenuScene(SceneManager scenes) {
        this.scenes = scenes;
    }

    @Override
    public boolean isBackgroundVisible() {
        return true;
    }

    @Override 
    public boolean isAmbientAudioEnabled() {
        return true;
    }


    @Override
    public void onEnter() {
        // Stop any background music from previous scene
        outputManager.stopBgm();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);   // Divert input from game logic to UI logic

        // Ensure viewport matches the current window size at entry
        lastWidth = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
        stage.getViewport().update(lastWidth, lastHeight, true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));


        // Loads 'SPACE COUNT' logo
        logoTexture = new Texture(Gdx.files.internal("space_count_logo.png"));
        logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        logoImage = new Image(logoTexture);

        // Proportionally scales logo to desired width
        float screenWidth = Gdx.graphics.getWidth();
        float logoWidth = screenWidth * 0.50f;      // Adjusts group logo size
        float aspect = (float) logoTexture.getHeight() / logoTexture.getWidth();
        float logoHeight = logoWidth * aspect;
        logoImage.setSize(logoWidth, logoHeight);

        // Loads animated planet icon
        planetTexture = new Texture(Gdx.files.internal("planet_sheet.png"));
        planetTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Animates each frame
        int frameCount = 60;
        TextureRegion[][] tmp = TextureRegion.split(planetTexture, planetTexture.getWidth() / frameCount, planetTexture.getHeight());
        Animation<TextureRegion> planetAnimation = new Animation<>(0.1f, tmp[0]);
        planetImage = new AnimatedImage(planetAnimation);

        // Exact placement of planet icon, relative to logo
        float planetSize = logoWidth * 0.16f;
        planetImage.setSize(planetSize, planetSize);

        // Exact placement of where the planet icon will be in the grouping
        float targetX = logoWidth * 0.30f;
        float targetY = logoHeight * 0.20f;

        // Stays centered when scaling
        planetImage.setPosition(targetX - (planetSize / 2), targetY - (planetSize / 2));

        // Groups 'SPACE COUNT' logo and animated planet icon
        logoGroup = new Group();
        logoGroup.setSize(logoWidth,logoHeight);

        // Positions logo at group's origin
        logoImage.setPosition(0,0);

        logoGroup.addActor(logoImage);
        logoGroup.addActor(planetImage);
        
        // Planet floating animation effect
        planetImage.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 8, 2f, Interpolation.sine),
            Actions.moveBy(0, -8, 2f, Interpolation.sine)
        )));

        // UI Element Creation
        Label title = new Label("Welcome aboard, Orbiter P8-6.", skin);
        title.setAlignment(Align.center);
        title.setFontScale(2.0f);

        TextButton startBtn = new TextButton("Start Game", skin);
        TextButton howToPlayBtn = new TextButton("How to Play", skin);
        TextButton leaderboardBtn = new TextButton("Leaderboard", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton exitBtn = new TextButton("Exit", skin);

        // Customises text button designs (colours, font size etc.)
        Color textButtonColor = Color.valueOf("#57729d");
   
        // Groups all buttons
        TextButton[] menuButtons = {startBtn, howToPlayBtn, leaderboardBtn, settingsBtn, exitBtn};

        for (TextButton btn: menuButtons) {
            btn.getLabel().setFontScale(1.4f);
            btn.setColor(textButtonColor);
        }

        // Event Listener (Observer Pattern): React to button clicks
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();

                // Switch State: Load the Main Game via the intro cutscene
                //scenes.setScene(new MainScene(scenes));
                // NOTE: To change back to the original MainScene from Abstract Engine Part 1, just replace IntroScene with MainScene here.
                scenes.setScene(new IntroScene(scenes));
            }
        });

        howToPlayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                scenes.setScene(new HowToPlayScene(scenes));
            }
        });

        leaderboardBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                scenes.setScene(new LeaderboardScene(scenes)); // view-only mode
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();

                // Switch State: Load Settings
                scenes.setScene(new SettingsScene(scenes));
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                outputManager.playUiClick();
                Gdx.app.exit();
            }
        });

        //Configure Menu positions
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(logoGroup).padTop(20).padBottom(50).row();
        table.add(title).padBottom(50).row();
        table.defaults().width(280).height(60).pad(8);
        
        for (TextButton btn: menuButtons) {
            table.add(btn).row();
        }

        // UI Fade-in effect on game startup
        table.getColor().a = 0;
        table.addAction(Actions.fadeIn(2.0f, Interpolation.pow2In));

        stage.addActor(table);
    }

    @Override
    public void update(float dt) {
        stage.act(dt);  // Update UI animations
    }

    @Override
    public void render(SpriteBatch batch) {
        // If window size changed (maximize / resize), update Stage viewport
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            stage.getViewport().update(width, height, true);
        }

        // Renders the UI
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        //if (bgTexture != null) bgTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (planetTexture != null) planetTexture.dispose();
    }
}

// Renders frames from animations
class AnimatedImage extends Image {
    private final Animation<TextureRegion> animation;
    private float stateTime;

    private final TextureRegionDrawable drawable = new TextureRegionDrawable();
    public AnimatedImage(Animation<TextureRegion> animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        drawable.setRegion(animation.getKeyFrame(stateTime, true));
        setDrawable(drawable);
    }
}
