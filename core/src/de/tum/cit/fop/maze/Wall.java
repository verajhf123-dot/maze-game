package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
/**
 * Represents a single wall tile in the game map.
 * A wall has grid coordinates, world coordinates, and a collision rectangle.
 */
public class Wall  {
    /** Grid x-position (tile index). */
    public final int gridX;
    public final int gridY;
    /** World x-position in pixels. */
    public final float worldX;
    public final float worldY;


    public static final float TILE_SIZE = 32f;

    private final Rectangle bounds;
    /**
     * Creates a wall tile at the given grid position.
     * Grid coordinates are converted into world (pixel) coordinates.
     *
     * @param gridX x-position in the tile grid
     * @param gridY y-position in the tile grid
     */
    public Wall(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldX = gridX * TILE_SIZE;
        this.worldY = gridY * TILE_SIZE;

        this.bounds = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);
    }
    /**
     * Returns the collision rectangle of the wall.
     *
     * @return bounding rectangle used for collision detection
     */
    public Rectangle getBounds()
    {
        return bounds;
    }
}

