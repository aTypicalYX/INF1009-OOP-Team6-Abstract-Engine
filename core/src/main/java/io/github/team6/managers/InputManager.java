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
        // We reset velocity first
        // entity.setXVel(0);

        // We ask the Keyboard class for the hardware state
        // if (keyboard.isLeftPressed()) {
        //     entity.setXVel(-entity.getSpeed());
        // } else if (keyboard.isRightPressed()) {
        //     entity.setXVel(entity.getSpeed());
        // }
        if (playableEntityList.size() == 1) {
            keyboard.getInput(playableEntityList.get(0));
        } 
    }
}
