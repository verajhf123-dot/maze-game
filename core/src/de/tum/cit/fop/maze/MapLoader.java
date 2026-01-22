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
    // 用于记录地图边界
    public int minX = Integer.MAX_VALUE;
    public int maxX = Integer.MIN_VALUE;
    public int minY = Integer.MAX_VALUE;
    public int maxY = Integer.MIN_VALUE;

    // 数据容器：存放从文件读出来的所有东西
    public static class LevelData {
        public List<Wall> walls = new ArrayList<>();
        public List<Enemy> enemies = new ArrayList<>();
        // 新增：存放陷阱
        public List<Trap> traps = new ArrayList<>();
        // 新增：存放钥匙
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

                    // 🔥 根据老师要求的 ID 表加载 🔥
                    switch (type) {
                        case 0: // 墙
                            data.walls.add(new Wall(x, y));
                            break;
                        case 1: // 起点
                            data.entryPosition = new Vector2(pixelX, pixelY);
                            break;
                        case 2: // 终点
                            data.exitPosition = new Vector2(x, y);
                            break;
                        case 3: // 陷阱 (根据坐标奇偶数生成不同陷阱)
                            if ((x + y) % 2 == 0) {
                                data.traps.add(new MechanismTrap(pixelX, pixelY));
                            } else {
                                data.traps.add(new Fog(pixelX, pixelY));
                            }
                            break;
                        case 4: // 敌人 (精确控制各关卡配置)
                            int currentEnemyIdx = data.enemies.size(); // 获取这是当前地图加载的第几个敌人

                            if (levelNumber == 1) {
                                // Level 1: 3只狐狸 (前提：地图文件里至少有3个 ID=4 的坐标)
                                data.enemies.add(new NineTailedFox(pixelX, pixelY));
                            }
                            else if (levelNumber == 2) {
                                // Level 2: 2狐狸 + 1穷奇
                                if (currentEnemyIdx < 2) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else data.enemies.add(new QiongQi(pixelX, pixelY));
                            }
                            else if (levelNumber == 3) {
                                // Level 3: 3狐狸 + 1烛龙
                                if (currentEnemyIdx < 3) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else if (levelNumber == 4) {
                                // Level 4: 1狐狸 + 2穷奇 + 1烛龙
                                if (currentEnemyIdx == 0) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (currentEnemyIdx <= 2) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else if (levelNumber == 5) {
                                // Level 5: 3狐狸 + 2穷奇 + 2烛龙
                                if (currentEnemyIdx < 3) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (currentEnemyIdx < 5) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            else {
                                // Level 6+ (无尽模式): 随机混搭
                                int rand = (x + y + currentEnemyIdx) % 3;
                                if (rand == 0) data.enemies.add(new NineTailedFox(pixelX, pixelY));
                                else if (rand == 1) data.enemies.add(new QiongQi(pixelX, pixelY));
                                else data.enemies.add(new ZhuLong(pixelX, pixelY));
                            }
                            break;
                        case 5: // 钥匙 (默认非 Bonus)
                            data.keys.add(new Key(pixelX, pixelY, false));
                            break;

                        case 6: // 道具 (根据你要求的概率逻辑)
                            double rng = Math.random();
                            if (levelNumber <= 2) {
                                // 前两关只出仙丹或御风符
                                if (rng < 0.7) data.items.add(new Xiandan(pixelX, pixelY));
                                else data.items.add(new de.tum.cit.fop.maze.items.Yufengfu(pixelX, pixelY));
                            } else {
                                // 后续关卡加入金刚符
                                if (rng < 0.5) data.items.add(new Xiandan(pixelX, pixelY));
                                else if (rng < 0.8) data.items.add(new de.tum.cit.fop.maze.items.Yufengfu(pixelX, pixelY));
                                else data.items.add(new Jingangfu(pixelX, pixelY));
                            }
                            break;

                        case 7: // 装饰：lishu
                            data.decorations.add(new Decoration(
                                    pixelX, pixelY,
                                    lishu(),
                                    Wall.TILE_SIZE * 2f,   // 宽：2格
                                    Wall.TILE_SIZE * 3f    // 高：3格（明显超过墙）
                            ));
                            break;

                        case 8: // 装饰：lishu2
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