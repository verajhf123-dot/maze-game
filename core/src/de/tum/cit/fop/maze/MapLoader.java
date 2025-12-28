package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import de.tum.cit.fop.maze.traps.Trap;

import java.nio.file.Paths;
import java.util.*;

public class MapLoader {

    public static class LevelData {
        public List<Wall> walls = new ArrayList<>();
        public List<Enemy> enemies = new ArrayList<>();
        public List<Trap> traps = new ArrayList<>();
        public Vector2 exitPosition = null;
    }

    public List<Wall> loadWalls(String internalPath) {
        return loadLevel(internalPath).walls;


    }

    public LevelData loadLevel(String path) {
        LevelData data = new LevelData();
       Properties props = new Properties();
       Random random = new Random();

        try {
            System.out.println("🔍 Start searching for map: " + path);

            FileHandle file = Gdx.files.local(path);

            if (!file.exists()) {
                System.out.println("   Not found in root, trying upper level...");
                file = Gdx.files.local("../" + path);
            }
            if (!file.exists()) {
                throw new RuntimeException(" FAILED. Looked at: " + Gdx.files.local(path).file().getAbsolutePath() +
                        " AND " + Gdx.files.local("../" + path).file().getAbsolutePath());
            }
            System.out.println(" FOUND MAP AT: " + file.file().getAbsolutePath());

            props.load(file.read());

            for (String key : props.stringPropertyNames()) {
                // 【新增 1】跳过不包含逗号的行（忽略 Width, Height 等元数据）
                if (!key.contains(",")) {
                    System.out.println("跳过元数据: " + key + " = " + props.getProperty(key));
                    continue;
                }

                String value = props.getProperty(key).trim();

                try {
                    int type = Integer.parseInt(value);
                    String[] xy = key.split(",");
                    int gridX = Integer.parseInt(xy[0]);
                    int gridY = Integer.parseInt(xy[1]);

                    float pixelX = gridX * Wall.TILE_SIZE;
                    float pixelY = gridY * Wall.TILE_SIZE;

                    switch (type) {
                        case 0:
                            data.walls.add(new Wall(gridX, gridY));
                            break;
                        case 2:
                            data.exitPosition = new Vector2(
                                    pixelX + Wall.TILE_SIZE / 2f,
                                    pixelY + Wall.TILE_SIZE / 2f
                            );
                            break;
                        case 3:
                            data.traps.add(new MechanismTrap(pixelX, pixelY));
                            break;
                        case 4:
                            int r = random.nextInt(3);
                            if (r == 0) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                            else if (r == 1) data.enemies.add(new QiongQi(pixelX, pixelY));
                            else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("跳过格式错误的行: Key=" + key + ", Value=" + value);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading map: " + path);
            e.printStackTrace();
        }
        return data;
    }

}
