package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;
import de.tum.cit.fop.maze.items.Item;
import de.tum.cit.fop.maze.items.Jingangfu;
import de.tum.cit.fop.maze.items.Key;
import de.tum.cit.fop.maze.items.Xiandan;
import de.tum.cit.fop.maze.traps.Fog;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import de.tum.cit.fop.maze.traps.Trap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.badlogic.gdx.graphics.Texture;


public class MapLoader {
    public int minX = Integer.MAX_VALUE;
    public int maxX = Integer.MIN_VALUE;
    public int minY = Integer.MAX_VALUE;
    public int maxY = Integer.MIN_VALUE;

    public static class LevelData {
        public List<Wall> walls = new ArrayList<>();
        public List<Enemy> enemies = new ArrayList<>();
        public List<Trap> traps = new ArrayList<>();
        public List<Key> keys = new ArrayList<>();

        public Vector2 exitPosition = null;
        public Vector2 entryPosition = null;
        public List<Item> items = new ArrayList<>();

        public List<Decoration> decorations = new ArrayList<>();

    }


    public LevelData loadLevel(String internalPath,int levelNumber) {
        System.out.println("[MapLoader] levelNumber=" + levelNumber + " path=" + internalPath);
        LevelData data = new LevelData();
        Properties props = new Properties();

        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (!file.exists()) {
                System.err.println("Map file not found: " + internalPath);
                return data;
            }
            props.load(file.read());


            for (String key : props.stringPropertyNames()) {
                if (!key.contains(",")) continue;

                try {
                    String value = props.getProperty(key).trim();
                    String[] coords = key.split(",");

                    if (coords.length != 2) continue;

                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());

                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);

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
                        case 3:
                            if ((x + y) % 2 == 0) {
                                data.traps.add(new MechanismTrap(pixelX, pixelY));
                            } else {
                                data.traps.add(new Fog(pixelX, pixelY));
                            }
                            break;
                        case 4:
                            int currentEnemyIdx = data.enemies.size();

                            if (levelNumber == 1) {
                                data.enemies.add(new NineTailedFox(pixelX, pixelY));
                            }
                            else if (levelNumber == 2) {
                                if (currentEnemyIdx < 2) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else data.enemies.add(new QiongQi(pixelX, pixelY));
                            }
                            else if (levelNumber == 3) {
                                if (currentEnemyIdx < 3) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else if (levelNumber == 4) {
                                if (currentEnemyIdx == 0) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (currentEnemyIdx <= 2) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else if (levelNumber == 5) {
                                if (currentEnemyIdx < 3) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (currentEnemyIdx < 5) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else {
                                int rand = (x + y + currentEnemyIdx) % 3;
                                if (rand == 0) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (rand == 1) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            break;
                        case 5:
                            data.keys.add(new Key(pixelX, pixelY, false));
                            break;

                        case 6:
                            double rng = Math.random();
                            if (levelNumber <= 2) {
                                if (rng < 0.7) data.items.add(new Xiandan(pixelX, pixelY));
                                else data.items.add(new de.tum.cit.fop.maze.items.Yufengfu(pixelX, pixelY));
                            } else {
                                if (rng < 0.5) data.items.add(new Xiandan(pixelX, pixelY));
                                else if (rng < 0.8) data.items.add(new de.tum.cit.fop.maze.items.Yufengfu(pixelX, pixelY));
                                else data.items.add(new Jingangfu(pixelX, pixelY));
                            }
                            break;

                        case 7:
                            data.decorations.add(new Decoration(
                                    pixelX, pixelY,
                                    lishu(),
                                    Wall.TILE_SIZE * 2f,
                                    Wall.TILE_SIZE * 3f
                            ));
                            break;

                        case 8:
                            data.decorations.add(new Decoration(
                                    pixelX, pixelY,
                                    lishu2(),
                                    Wall.TILE_SIZE * 2.2f,
                                    Wall.TILE_SIZE * 3.2f
                            ));
                            break;

                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Texture LISHU;
    private static Texture LISHU2;

    private static Texture lishu() {
        if (LISHU == null) LISHU = new Texture("lishu.png");
        return LISHU;
    }

    private static Texture lishu2() {
        if (LISHU2 == null) LISHU2 = new Texture("lishu2.png");
        return LISHU2;
    }
}