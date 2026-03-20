package io.github.team6.mathgame;

import com.badlogic.gdx.graphics.Color;

import io.github.team6.entities.Entity;
import io.github.team6.entities.behavior.CollisionBehavior;


/*
NumverCollectionBehavior:
Defines the behavior when the player collides with a number asteroid. It checks if the collected number is the correct answer to the current equation. If correct, it awards points and spawns a green "+10" floating text. If incorrect, it deducts a life and spawns a red "-1 Life" floating text. It also triggers the generation of a new round of asteroids after a correct answer.
* * * OOP Concepts & Design Patterns:
- Strategy Pattern: Implements the CollisionBehavior interface to define a specific collision response for number asteroids
- Single Responsibility Principle: This class is solely responsible for handling the logic related to collecting number asteroids and checking answers, keeping it focused and maintainable.
*/
public class NumberCollectionBehavior implements CollisionBehavior {
    
    private final EquationGenerator equationGenerator;
    private final int numberValue;
    private final MathGameScene scene;

    // Constructor takes in the current equation generator, the value of the number asteroid, and a reference to the game scene for updating score/lives and spawning floating text.
    public NumberCollectionBehavior(EquationGenerator generator, int numberValue, MathGameScene scene) {
        this.equationGenerator = generator;
        this.numberValue = numberValue;
        this.scene = scene;
    }

    /*
    onCollision(Entity self, Entity other) is called when the player collides with a number asteroid. It checks if the other entity is the player, then verifies if the collected number matches the current equation's answer. Depending on correctness, it updates the score/lives and spawns appropriate floating text feedback.
    */
    @Override
    public void onCollision(Entity self, Entity other) {
        if (other.getTag().equals("PLAYER")) {
            
            if (equationGenerator.checkAnswer(numberValue)) {
                // Correct Answer
                scene.addScore(10);
                
                // Spawn Green "+10" where the asteroid was
                scene.spawnFloatingText("+10", self.getX(), self.getY() + 30, Color.GREEN);
                
                scene.generateNewRound(); 
            } else {
                // Wrong Answer
                scene.deductLife(); 
                
                // Spawn Red "-1 Life" where the asteroid was!
                scene.spawnFloatingText("-1 Life", self.getX(), self.getY() + 30, Color.RED);
                
                self.setActive(false); 
            }
        }
    }
}