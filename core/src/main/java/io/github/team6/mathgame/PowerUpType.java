package io.github.team6.mathgame;

/**
 * PowerUpType
 * Enumerates the three collectible power-ups in the game.
 *
 * TIME_EXTENSION   : adds extra seconds to the survival timer
 * EXTRA_LIFE       : grants one additional life (capped at STARTING_LIVES)
 * SCORE_MULTIPLIER : doubles points earned for the next correct answer
 * SHIELD           : grants temporary invulnerability (I-Frames) for a few seconds
 *
 * OOP: Open/Closed : adding a new power-up only requires a new enum value
 * and a branch in PowerUpCollisionBehavior; no other classes change.
 */
public enum PowerUpType {
    TIME_EXTENSION,
    EXTRA_LIFE,
    SCORE_MULTIPLIER
}
