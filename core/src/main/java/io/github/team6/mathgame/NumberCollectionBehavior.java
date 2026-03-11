package io.github.team6.mathgame;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;

/**
 * This behavior implements the Strategy Pattern. 
 * It defines specifically what happens when an entity (an asteroid) collides with something else.
 * By keeping this separate, the abstract engine doesn't need to know anything about math or scores.
 */
public class NumberCollectionBehavior implements CollisionBehavior {
    
    // Dependencies required for this behavior to work.
    // They are declared 'final' because once the behavior is created, these shouldn't change.
    private final EquationGenerator equationGenerator;
    private final int numberValue;
    private final MathGameScene scene;

    /**
     * Constructor using Dependency Injection.
     * We pass in the tools this behavior needs to do its job.
     * * @param generator To check if the collected number is the correct answer.
     * @param numberValue The specific number assigned to the asteroid holding this behavior.
     * @param scene A reference to the scene so we can tell it to add score or generate a new round.
     */
    public NumberCollectionBehavior(EquationGenerator generator, int numberValue, MathGameScene scene) {
        this.equationGenerator = generator;
        this.numberValue = numberValue;
        this.scene = scene;
    }

    /**
     * Triggered by the engine's CollisionManager when an overlap occurs.
     */
    @Override
    public void onCollision(Entity self, Entity other) {
        // 1. Filter the collision: We only care if the ASTEROID hit the PLAYER.
        // We use the Tag system to identify the player without needing complex class casting.
        if (other.getTag().equals("PLAYER")) {
            
            // 2. Check if the number on this specific asteroid solves the active equation.
            if (equationGenerator.checkAnswer(numberValue)) {
                
                // CORRECT ANSWER SCENARIO
                scene.addScore(10); // Reward the player
                
                // Tell the scene to wipe the board and start a new math problem.
                // Notice how this behavior doesn't do the wiping itself; it delegates to the Scene.
                scene.generateNewRound(); 
                
            } else {
                
                // WRONG ANSWER SCENARIO
                scene.addScore(-5); // Penalize the score
                
                // Destroy just this specific asteroid. 
                // Setting active to false tells the EntityManager to remove it on the next frame.
                self.setActive(false); 
            }
        }
    }
}