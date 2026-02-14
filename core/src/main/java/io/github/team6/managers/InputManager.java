package io.github.team6.managers;

import java.util.List;

import io.github.team6.entities.Entity;
import io.github.team6.inputoutput.Keyboard;

/**
 * InputManager handles the link between keyboard keys and Game Objects.
 */

public class InputManager {
    private Keyboard keyboard;

    public InputManager() {
        this.keyboard = new Keyboard();
    }

    public void update(List<Entity> playableEntityList) {
    if (playableEntityList == null || playableEntityList.isEmpty()) return;

    // Player 1: Arrow keys
    keyboard.getArrowInput(playableEntityList.get(0));

    // Player 2 (optional): WASD
    if (playableEntityList.size() >= 2) {
        keyboard.getWASDInput(playableEntityList.get(1));
        }
    }
}
