package io.github.team6.mathgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * LeaderboardManager
 * Persists and retrieves the top 5 high scores using LibGDX Preferences.
 *
 * Storage format (key → value):
 *   "name_1" → "Alice"     "score_1" → 980
 *   "name_2" → "Bob"       "score_2" → 750
 *   ... up to rank 5
 *
 * LibGDX Preferences automatically maps to:
 *   Windows : %APPDATA%\ExtremeEquations\leaderboard.xml
 *   Linux   : ~/.prefs/ExtremeEquations/leaderboard
 *   macOS   : ~/Library/Preferences/ExtremeEquations/leaderboard
 *
 * OOP Concepts:
 * - Encapsulation  : Raw Preferences I/O is hidden; callers only use
 *                    addEntry() and getEntries().
 * - Single Responsibility: Only manages leaderboard persistence.
 * - Immutable data : ScoreEntry is a plain immutable record so callers
 *                    cannot accidentally mutate stored data.
 */
public class LeaderboardManager {

    private static final String PREFS_NAME = "ExtremeEquations";
    private static final int    MAX_ENTRIES = 5;

    // -----------------------------------------------------------------------
    // ScoreEntry — immutable data record
    // -----------------------------------------------------------------------

    /** Immutable record representing one leaderboard entry. */
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public final String name;
        public final int    score;
        public final int    level;  // which level the score was achieved on

        public ScoreEntry(String name, int score, int level) {
            this.name  = name;
            this.score = score;
            this.level = level;
        }

        /** Higher scores rank first. */
        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score);
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Loads all stored entries, inserts the new one, re-sorts, keeps top 5,
     * and saves back to disk.
     *
     * @param name  Player-entered name (trimmed, max 12 chars).
     * @param score Final score for this session.
     * @param level Level reached.
     */
    public void addEntry(String name, int score, int level) {
        List<ScoreEntry> entries = getEntries();
        entries.add(new ScoreEntry(truncate(name), score, level));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries = entries.subList(0, MAX_ENTRIES);
        }
        save(entries);
    }

    /**
     * Returns up to MAX_ENTRIES entries sorted best-first.
     * Returns an empty list if no scores have been saved yet.
     */
    public List<ScoreEntry> getEntries() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        List<ScoreEntry> entries = new ArrayList<>();

        for (int i = 1; i <= MAX_ENTRIES; i++) {
            String name  = prefs.getString("name_"  + i, null);
            int    score = prefs.getInteger("score_" + i, -1);
            int    level = prefs.getInteger("level_" + i, 1);
            if (name != null && score >= 0) {
                entries.add(new ScoreEntry(name, score, level));
            }
        }

        Collections.sort(entries);
        return entries;
    }

    /**
     * Returns true if the given score qualifies for the leaderboard
     * (either fewer than MAX_ENTRIES exist, or the score beats the lowest).
     */
    public boolean isHighScore(int score) {
        List<ScoreEntry> entries = getEntries();
        if (entries.size() < MAX_ENTRIES) return true;
        return score > entries.get(entries.size() - 1).score;
    }

    /** Wipes all saved scores (useful for testing). */
    public void clear() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.clear();
        prefs.flush();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void save(List<ScoreEntry> entries) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        // Clear old values first so stale entries don't linger
        prefs.clear();

        for (int i = 0; i < entries.size(); i++) {
            ScoreEntry e = entries.get(i);
            prefs.putString("name_"  + (i + 1), e.name);
            prefs.putInteger("score_" + (i + 1), e.score);
            prefs.putInteger("level_" + (i + 1), e.level);
        }
        prefs.flush(); // Write to disk
    }

    private String truncate(String name) {
        if (name == null || name.isBlank()) return "???";
        name = name.trim();
        return name.length() > 12 ? name.substring(0, 12) : name;
    }
}
