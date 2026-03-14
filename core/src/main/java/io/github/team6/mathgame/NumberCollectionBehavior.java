package io.github.team6.mathgame;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;

public class NumberCollectionBehavior implements CollisionBehavior {
    
    private final EquationGenerator equationGenerator;
    private final int numberValue;
    private final MathGameScene scene;

    public NumberCollectionBehavior(EquationGenerator generator, int numberValue, MathGameScene scene) {
        this.equationGenerator = generator;
        this.numberValue = numberValue;
        this.scene = scene;
    }

    @Override
    public void onCollision(Entity self, Entity other) {
        if (other.getTag().equals("PLAYER")) {
            
            if (equationGenerator.checkAnswer(numberValue)) {
                // Correct Answer
                scene.addScore(10);
                scene.generateNewRound(); 
            } else {
                // Wrong Answer: Deduct a life instead of score
                scene.deductLife(); 
                
                // Destroy this specific decoy asteroid so the player isn't hit multiple times
                self.setActive(false); 
            }
        }
    }
}