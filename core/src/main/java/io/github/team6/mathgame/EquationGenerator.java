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
        // int num1 = ThreadLocalRandom.current().nextInt(1, 11); 
        // int num2 = ThreadLocalRandom.current().nextInt(1, 11); 
        
        // Calculate the answer and build the display string
        // currentAnswer = num1 + num2;
        // currentEquation = num1 + " + " + num2 + " = ?";

        // Pick an operator: 0 = +, 1 = -, 2 = *, 3 = /
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int operatorType = rand.nextInt(0, 4);
        int num1, num2;

        switch(operatorType) {
            case 0:
                num1 = rand.nextInt(1, 21);
                num2 = rand.nextInt(1, 21);
                currentAnswer = num1 + num2;
                currentEquation = num1 + " + " + num2 + " = ?";
                break;
            case 1:
                num1 = rand.nextInt(1, 21);
                num2 = rand.nextInt(1, num1 + 1); // ensure num2 <= num1 for positive answer
                currentAnswer = num1 - num2;
                currentEquation = num1 + " - " + num2 + " = ?";
                break;
            case 2:
                num1 = rand.nextInt(1, 11); // keep value smaller (up to 10x10)
                num2 = rand.nextInt(1, 11);
                currentAnswer = num1 * num2;
                currentEquation = num1 + " x " + num2 + " = ?";
                break;

            case 3:
                // to guarantee a whole number, generate the divisor and the answer first, then multiply for the dividend.
                int possibleAnswer = rand.nextInt(1, 11); 
                num2 = rand.nextInt(1, 11); 
                num1 = possibleAnswer * num2; // num1 is perfectly divisible by num2
                
                currentAnswer = possibleAnswer;
                currentEquation = num1 + " / " + num2 + " = ?";
                break;
        }
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