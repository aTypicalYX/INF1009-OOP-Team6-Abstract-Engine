package io.github.team6.mathgame;

/**
 * GameStateManager – Singleton Pattern
 *
 * Purpose: Acts as a single, globally accessible source of truth for all
 * game-session data: lives remaining, current score, and the number of
 * equations answered correctly.
 *
 * Why Singleton here?
 * Multiple systems (MathGameScene, NumberCollectionBehavior, GameOverScene)
 * need to READ and WRITE the same game state.  Without a Singleton those
 * systems would each hold their own copy and quickly fall out of sync.
 * The Singleton guarantees that every call to getInstance() returns the
 * exact same object, so state is always consistent.
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
    /** How many lives the player begins every session with. */
    public static final int STARTING_LIVES = 4;

    /** Number of correct answers required to WIN the game. */
    public static final int EQUATIONS_TO_WIN = 5;

    /** Points awarded for each correct answer. */
    public static final int POINTS_PER_CORRECT = 10;

    // ---------------------------------------------------------------
    // Singleton infrastructure
    // ---------------------------------------------------------------

    /** The one-and-only instance of this class. */
    private static GameStateManager instance;

    /**
     * Returns the single shared instance, creating it on the first call.
     * This is a "lazy initialisation" Singleton – the object is not built
     * until something actually needs it.
     */
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
    private int lives;
    private int score;
    private int equationsAnswered;   // counts CORRECT answers only

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /**
     * Resets every field back to its starting value.
     * Call this at the beginning of each new game session so that the
     * Singleton does not carry stale data from a previous play-through.
     */
    public void reset() {
        lives            = STARTING_LIVES;
        score            = 0;
        equationsAnswered = 0;
    }

    // ---------------------------------------------------------------
    // Lives
    // ---------------------------------------------------------------

    public int getLives() {
        return lives;
    }

    /**
     * Removes one life.
     * @return {@code true} when the player still has lives left,
     *         {@code false} when all lives are exhausted (game over).
     */
    public boolean deductLife() {
        if (lives > 0) lives--;
        return lives > 0;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    // ---------------------------------------------------------------
    // Score
    // ---------------------------------------------------------------

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score = Math.max(0, score + points);
    }

    // ---------------------------------------------------------------
    // Win condition
    // ---------------------------------------------------------------

    public int getEquationsAnswered() {
        return equationsAnswered;
    }

    /**
     * Increments the correct-answer counter.
     * @return {@code true} when the player has answered enough equations
     *         to satisfy the win condition.
     */
    public boolean recordCorrectAnswer() {
        equationsAnswered++;
        return equationsAnswered >= EQUATIONS_TO_WIN;
    }

    public boolean hasWon() {
        return equationsAnswered >= EQUATIONS_TO_WIN;
    }
}
