package io.github.team6.mathgame;

/**
 * GameStateManager – Singleton Pattern
 *
 * Purpose: Acts as a single, globally accessible source of truth for all
 * game-session data: lives remaining, current score, and the number of
 * equations answered correctly.
 *
 * Design Pattern: Singleton
 * - Private constructor prevents external instantiation.
 * - Static getInstance() returns (and lazily creates) the one instance.
 * - reset() lets MathGameScene start a fresh session without destroying
 *   and recreating the object.
 */
public class GameStateManager {

    // ---------------------------------------------------------------
    // Game rules / tuning constants
    // ---------------------------------------------------------------
    /** Set number of lives the player begins every session with. */
    public static final int STARTING_LIVES = 4;

    /** Number of correct answers required to WIN the game. */
    public static final int EQUATIONS_TO_WIN = 5;

    /** Points awarded for each correct answer. */
    public static final int POINTS_PER_CORRECT = 10;
    /** Starting time in seconds for each game session. */
    public static final float STARTING_TIME       = 120f; // 2 minutes per level
    
    // ---------------------------------------------------------------
    // Singleton infrastructure
    // ---------------------------------------------------------------

    /** The one-and-only instance of this class. */
    private static GameStateManager instance;

    // Returns the single shared instance, creating it on the first call.
    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    /** Private constructor – callers MUST use getInstance(). */
    private GameStateManager() {
        reset();
    }

    // ---------------------------------------------------------------
    // State fields
    // ---------------------------------------------------------------
    private int   lives;
    private int   score;
    private int   equationsAnswered;
    private int   levelEquationsAnswered;
    private int   level;
    private float timeSeconds;
    private boolean gameOver;
    private boolean scoreMultiplierActive;
    private int currentStreak;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    // Resets all fields to their initial values. Called by MathGameScene when starting a new session.
    public void reset() {
        lives                 = STARTING_LIVES;
        score                 = 0;
        equationsAnswered     = 0;
        levelEquationsAnswered = 0;
        level                 = 1;
        timeSeconds           = STARTING_TIME;
        gameOver              = false;
        scoreMultiplierActive = false;
        currentStreak         = 0;

    }

    // -----------------------------------------------------------------------
    // Score multiplier power-up
    // -----------------------------------------------------------------------

    // Activates the 2x score multiplier for the next correct answer only
    public void activateScoreMultiplier() { scoreMultiplierActive = true; }

    public boolean isScoreMultiplierActive() { return scoreMultiplierActive; }

    // Called by NumberCollectionBehavior after awarding bonus points
    public void consumeScoreMultiplier() { scoreMultiplierActive = false; }


    // -----------------------------------------------------------------------
    // Lives
    // -----------------------------------------------------------------------

    public int getLives() { return lives; }

    public boolean deductLife() {
        currentStreak = 0; // reset streak on incorrect answer
        if (lives > 0) lives--;
        if (lives <= 0) gameOver = true;
        return lives > 0;
    }

    // Power-up: restore one life, capped at STARTING_LIVES.
    public void addLife() {
        if (lives < STARTING_LIVES) lives++;
    }

    // Refresh lives and timers when in a new level
    public void refreshLivesAndTimer() {
        lives                  = STARTING_LIVES;
        timeSeconds            = STARTING_TIME;
        gameOver               = false;
        levelEquationsAnswered = 0; 
    }

    public boolean isGameOver() { return gameOver; }

    // -----------------------------------------------------------------------
    // Score
    // -----------------------------------------------------------------------

    public int  getScore()           { return score; }
    
    public void addScore(int points) { 
        // Double points if streak is 3 or more
        int multiplier = (currentStreak >= 3) ? 2 : 1;
        score = Math.max(0, score + (points * multiplier)); 
    }

    // -----------------------------------------------------------------------
    // Equations / win condition
    // -----------------------------------------------------------------------

    public int getEquationsAnswered()      { return equationsAnswered; }
    public int getLevelEquationsAnswered() { return levelEquationsAnswered; }

    public int getCurrentStreak() { return currentStreak; }

    public boolean recordCorrectAnswer() {
        equationsAnswered++;
        levelEquationsAnswered++;
        currentStreak++;
        return equationsAnswered >= EQUATIONS_TO_WIN;
    }

    public boolean hasWon() { return equationsAnswered >= EQUATIONS_TO_WIN; }

    // -----------------------------------------------------------------------
    // Level
    // -----------------------------------------------------------------------

    public int  getLevel()      { return level; }
    public void setLevel(int l) { level = l; }

    // -----------------------------------------------------------------------
    // Timer
    // -----------------------------------------------------------------------

    public float getTimeSeconds()  { return timeSeconds; }

    // Called each frame by MathGameScene (only when not paused).
    public void tickTime(float dt) {
        timeSeconds = Math.max(0, timeSeconds - dt);
    }

    // Power-up: extend the timer.
    public void addTime(float seconds) {
        timeSeconds += seconds;
    }

    public boolean isTimeUp() { return timeSeconds <= 0; }
}
