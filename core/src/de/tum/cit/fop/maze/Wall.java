package de.tum.cit.fop.maze;

public class Wall  {
    public final int gridX;
    public final int gridY;
    public final float worldX;
    public final float worldY;

    public static final float TILE_SIZE = 32f;

    public Wall(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldX = gridX * TILE_SIZE;
        this.worldY = gridY * TILE_SIZE;
    }
}
