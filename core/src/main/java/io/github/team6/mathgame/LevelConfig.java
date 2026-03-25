package io.github.team6.mathgame;

/**
 * LevelConfig
 * Holds all level-specific asset and configuration data.
 *
 * === Design Patterns ===
 * - Open/Closed Principle: open for extension (add new levels here),
 *   closed for modification (MathGameScene stays untouched).
 * - Encapsulation: level data is bundled into one immutable object.
 */
public class LevelConfig {

    // -------------------------------------------------------------------
    // All level definitions - add new levels here only
    // -------------------------------------------------------------------
    private static final LevelConfig[] LEVELS = {
        new LevelConfig(1, "maps/level1.tmx", "blackhole.png"),
        new LevelConfig(2, "maps/level2.tmx", "lava.png"),
    };

    // -------------------------------------------------------------------
    // Per-level data fields (public final - immutable after construction)
    // -------------------------------------------------------------------
    public final int    levelNumber;
    public final String mapFile;
    public final String chaserTexture;

    private LevelConfig(int levelNumber, String mapFile, String chaserTexture) {
        this.levelNumber    = levelNumber;
        this.mapFile        = mapFile;
        this.chaserTexture  = chaserTexture;
    }

    /**
     * Returns the config for the given level number.
     * Falls back to level 1 if the level is not defined.
     */
    public static LevelConfig forLevel(int level) {
        for (LevelConfig config : LEVELS) {
            if (config.levelNumber == level) return config;
        }
        System.out.println("[LevelConfig] No config for level " + level + ", defaulting to level 1.");
        return LEVELS[0];
    }

    /**
     * Returns true if there is a defined config for the next level.
     * Used by VictoryScene to decide whether to show Next Level or Play Again.
     */
    public static boolean hasNextLevel(int currentLevel) {
        for (LevelConfig config : LEVELS) {
            if (config.levelNumber == currentLevel + 1) return true;
        }
        return false;
    }
}
