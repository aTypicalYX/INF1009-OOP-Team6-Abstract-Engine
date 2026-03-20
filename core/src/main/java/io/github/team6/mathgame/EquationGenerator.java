package io.github.team6.mathgame;

// ThreadLocalRandom is used to generate random numbers in a thread-safe way.
import java.util.concurrent.ThreadLocalRandom;

/**
 * EquationGenerator is responsible for creating simple math equations for the game. 
 * It encapsulates the logic for generating random equations and checking answers, keeping this concern separate from the game logic.
 * How it works: When the MathGameScene needs a new equation, it calls generateNewEquation(), which updates the internal state of the EquationGenerator with a new equation and its answer.
 * The scene can then retrieve the current equation and answer using the provided getters.
 * The equations it can generate include addition, subtraction, multiplication, and division.
 * How it chooses which type of equation to generate is based on a random operator selection.
 */
public class EquationGenerator {

    // Stores the current equation as a string (e.g. "3 + 4 = ?") and its answer as an integer (e.g. 7).
    private String currentEquation;
    private int currentAnswer;

    // Track the current difficulty level. Defaults to 1.
    private int currentLevel = 1;

    // Method for the Scene to set the current level, which can be used to adjust the difficulty of generated equations in the future.
    public void setLevel(int level) {
        this.currentLevel = level;
    }


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


        // Restrict the math operator based on the player's level.
        // Level 1 = 0 (Addition only)
        // Level 2 = 0 to 1 (Addition & Subtraction)
        // Level 3 = 0 to 2 (Up to Multiplication)
        // Level 4+ = 0 to 3 (All operators unlocked)
        int maxOperator = Math.min(3, currentLevel - 1); 
        int operatorType = rand.nextInt(0, maxOperator + 1);
        int num1, num2;

        // Generate the equation based on the randomly selected operator.
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
