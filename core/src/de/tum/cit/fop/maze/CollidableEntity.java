package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public interface CollidableEntity {
    Rectangle getHitbox();
    Vector2 getPosition();
    void syncPositionToHitbox();
}
