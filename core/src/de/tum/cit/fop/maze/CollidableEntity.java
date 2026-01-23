package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * CollidableEntity defines a common interface for all objects that can collide in the game.
 *
 *
 * Represents an entity that can participate in collision detection.
 *
 * A CollidableEntity provides a hitbox for collision checks and a position
 * in the game world. After collision resolution, the position can be
 * synchronized with the hitbox.
 */



public interface CollidableEntity {
    Rectangle getHitbox();
    Vector2 getPosition();
    void syncPositionToHitbox();
}

