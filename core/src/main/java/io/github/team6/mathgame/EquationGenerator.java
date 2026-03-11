package io.github.team6.mathgame;

// ThreadLocalRandom is used to generate random numbers in a thread-safe way.
import java.util.concurrent.ThreadLocalRandom;

/**
 * EquationGenerator is responsible for creating simple math equations for the game. 
 * It generates two random numbers, forms an addition equation, and stores the correct answer.
 * * OOP Concept: Encapsulation. The internal state (the equation string and the answer) 
 * are kept private. Outside classes can only read them via getters or interact via checkAnswer().
 */
public class EquationGenerator {
    
    // Private fields: The outside world doesn't need to modify these directly.
    private String currentEquation;
    private int currentAnswer;

    /**
     * Generates a new simple addition problem for kids (e.g. "3 + 4 = ?").
     * This method updates the internal state of the object.
     */
    public void generateNewEquation() {
        // Generate two random numbers between 1 and 10 (inclusive)
        // Note: nextInt(origin, bound) is inclusive of origin, exclusive of bound.
        int num1 = ThreadLocalRandom.current().nextInt(1, 11); 
        int num2 = ThreadLocalRandom.current().nextInt(1, 11); 
        
        // Calculate the answer and build the display string
        currentAnswer = num1 + num2;
        currentEquation = num1 + " + " + num2 + " = ?";
    }

    // --- GETTERS ---
    // Allow other classes (like MathGameScene) to read the data without modifying it.
    
    public String getCurrentEquation() { 
        return currentEquation; 
    }
    
    public int getCurrentAnswer() { 
        return currentAnswer; 
    }
    
    /**
     * Evaluates if the provided answer matches the currently generated equation.
     * @param answer The number collected by the player.
     * @return true if correct, false otherwise.
     */
    public boolean checkAnswer(int answer) { 
        return answer == currentAnswer; 
    }
}