package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

public class Wall  {
    public final int gridX;
    public final int gridY;
    public final float worldX;
    public final float worldY;


    public static final float TILE_SIZE = 32f;

    private final Rectangle bounds;

    public Wall(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldX = gridX * TILE_SIZE;
        this.worldY = gridY * TILE_SIZE;

        this.bounds = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}

