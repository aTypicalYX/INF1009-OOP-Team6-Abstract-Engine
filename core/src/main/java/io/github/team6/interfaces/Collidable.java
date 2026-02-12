/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package io.github.team6.interfaces;
import com.badlogic.gdx.math.Rectangle;


public interface Collidable {
    public Rectangle getHitbox();
    public void onCollision();
}
