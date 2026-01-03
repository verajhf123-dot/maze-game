package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MapLoader {

    public static class LevelData {
        public List<Wall> walls = new ArrayList<>();
        public List<Enemy> enemies = new ArrayList<>();
        public Vector2 exitPosition = null;
        public Vector2 entryPosition = null;
    }

    public LevelData loadLevel(String internalPath) {
        LevelData data = new LevelData();
        Properties props = new Properties();

        try {
            FileHandle file = Gdx.files.local(internalPath);
            if (!file.exists()) {
                file = Gdx.files.internal(internalPath);
                System.err.println("Map file not found: " + internalPath);
                return data;
            }
            props.load(file.read());

            for (String key : props.stringPropertyNames()) {

                if (!key.contains(",")) {
                    continue;
                }

                try {
                    String value = props.getProperty(key).trim();
                    String[] coords = key.split(",");

                    if (coords.length != 2) continue;

                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int type = Integer.parseInt(value);

                    float pixelX = x * Wall.TILE_SIZE;
                    float pixelY = y * Wall.TILE_SIZE;

                    switch (type) {
                        case 0:
                            data.walls.add(new Wall(x, y));
                            break;
                        case 1:
                            data.entryPosition = new Vector2(pixelX, pixelY);
                            break;
                        case 2:
                            data.exitPosition = new Vector2(x, y);
                            break;
                        case 4:
                            if ((x + y) % 2 == 0) {
                                data.enemies.add(new NineTailedFox(pixelX, pixelY));
                            } else {
                                data.enemies.add(new QiongQi(pixelX, pixelY));
                            }
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + key);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading map file: " + internalPath);
            e.printStackTrace();
        }

        return data;
    }

    public List<Wall> loadWalls(String internalPath) {
        return loadLevel(internalPath).walls;
    }

}
