/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package io.github.team6.interfaces;
import com.badlogic.gdx.math.Rectangle;

import io.github.team6.entities.Entity;

/**
 * Interface: Collidable
 * Defines requirements for objects that can interact physically.
 * 1. Must perform an action when hit (onCollision).
 * 2. Must provide a boundary box (getHitbox).
 */

public interface Collidable {
    public Rectangle getHitbox();
    public void onCollision(Entity other);
}
