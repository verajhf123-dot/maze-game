package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Exit;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.items.Key;
import de.tum.cit.fop.maze.traps.Trap;
import de.tum.cit.fop.maze.traps.Fog;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    private float mapPixelWidth;
    private float mapPixelHeight;
    private int mapWidthInTiles;
    private int mapHeightInTiles;
    private boolean gameWon = false;

    private final MazeRunnerGame game;
    private  OrthographicCamera camera;
    private  BitmapFont font;
    private List<Wall> walls;
    private ShapeRenderer shapeRenderer;
    private float sinusInput = 0f;
    // ==== 新增：路径寻找和陷阱系统 ====
    private AStarPathFinder pathFinder;
    private Array<Trap> traps;
    private int[][] collisionMap; // 用于A*寻路的碰撞地图
    private Rectangle exitArea;
    // ==== 新添加的敌人相关变量 ====
    private Array<Enemy> enemies;
    private Player player; // 假设组员2会创建Player类
    private boolean[][] walkableGrid;// A*寻路需要的地图网格
    // according to my enum;
    private GameState currentState;
    private Stage uiStage;//  stage to put the pauseSetting
    private Table pauseMenuTable; // container of pauseSetting;

    // ==== 其他变量 ====
    private float gameTime = 0f;
    private  int levelNumber;
    private String currentMapPath;
    private InputController controller;
    private Exit exit;
    private Key key;
    private Door door;
    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this(game,1);// 默认进 Level 1
    }


    public GameScreen(MazeRunnerGame game, int levelNumber) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.currentMapPath = "maps/level-" + levelNumber + ".properties";
        initCommon();
    }

    private void initCommon() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(240,160,0);
        camera.zoom = 0.75f;
        font = new BitmapFont();
        font.getData().setScale(1f);

        controller = new InputController();
        enemies = new Array<>();
        uiStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        currentState = GameState.RUNNING;
        createPauseMenu();

    }

    public GameScreen(MazeRunnerGame game,String mapFilePath) {
        this.game = game;
        this.levelNumber = 0;
        this.currentMapPath = mapFilePath;
        initCommon();

    }

    // creat pauseSetting
    private void createPauseMenu() {
        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();
        pauseMenuTable.setDebug(false);

       Label pauseLable = new Label("Game PAUSED", game.getSkin(),"title");
       pauseMenuTable.add(pauseLable).padBottom(40).row();

        TextButton resumeButton = new TextButton("Resume", game.getSkin());
        resumeButton.addListener(new  ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();// tip it ,continue game
            }
        });
        pauseMenuTable.add(resumeButton).width(250).padBottom(15).row();

        TextButton musicButton = new TextButton("Music:ON/OFF", game.getSkin());
        musicButton.addListener(new   ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Music music = game.getBackgroundMusic();
                if (music != null) {
                    if (music.isPlaying()) music.pause();
                    else music.play();
                }

            }
        });
        pauseMenuTable.add(musicButton).width(250).padBottom(15).row();

        TextButton quitButton = new TextButton("Exit to Menu", game.getSkin());
        quitButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Music music = game.getBackgroundMusic();
                if(music != null && music.isPlaying()) {
                    music.play();
                }
                game.goToMenu();

            }
        });
        pauseMenuTable.add(quitButton).width(250).row();

        uiStage.addActor(pauseMenuTable);
    }
    // switch the pauseState
    private void togglePause() {
        Music music = game.getBackgroundMusic();
        if(currentState==GameState.RUNNING) {
            currentState = GameState.PAUSED;
            pauseMenuTable.setVisible(true);
            Gdx.input.setInputProcessor(uiStage);
            if(music !=null && music.isPlaying()) {
                music.pause();
            }

        }else{
            currentState = GameState.RUNNING;
            pauseMenuTable.setVisible(false);
            Gdx.input.setInputProcessor(null);
            if(music !=null) {
                music.play();
            }

        }
    }



    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {

        // ===== 逻辑更新（你这部分基本没问题）=====
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        if (currentState == GameState.RUNNING) {
            controller.update();
            gameTime += delta;
            updateTraps(delta);
            updateEnemies(delta);
            updatePlayer(delta);

            if(key != null && player != null) {
                key.checkPickup(player);
            }

            if (door != null && player != null) {
                door.tryOpen(player);
            }

            checkCollisions();
            checkTrapActivation();
            updateCameraFollowPlayer();
            camera.update();
        }

        ScreenUtils.clear(0, 0, 0, 1);

        SpriteBatch batch = game.getSpriteBatch();

        // =================================================
        // 1️⃣ ShapeRenderer：所有“纯方块”的东西
        // =================================================
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 墙
        if (walls != null) {
            shapeRenderer.setColor(Color.GRAY);
            for (Wall wall : walls) {
                shapeRenderer.rect(
                        wall.worldX,
                        wall.worldY,
                        Wall.TILE_SIZE,
                        Wall.TILE_SIZE
                );
            }
        }

        // 玩家（debug 方块）
        if (player != null) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                    player.getPosition().x,
                    player.getPosition().y,
                    32,
                    32
            );
        }
        // 1. 画钥匙（黄色）
        if (key != null) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(
                    key.getX(),
                    key.getY(),
                    Wall.TILE_SIZE / 2f,
                    Wall.TILE_SIZE / 2f
            );
        }

        // 2. 画门（蓝色）
        if (door != null) {
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(
                    door.getX(),
                    door.getY(),
                    door.getWidth(),
                    door.getHeight()
            );
        }

        // 敌人（没有贴图的）
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (enemy.getTexture() != null) continue;

            shapeRenderer.setColor(enemy.getFallbackBodyColor());
            shapeRenderer.rect(
                    enemy.getX(),
                    enemy.getY(),
                    enemy.getWidth(),
                    enemy.getHeight()
            );
        }

        shapeRenderer.end();

        // =================================================
        // 2️⃣ SpriteBatch：所有“贴图”的东西
        // =================================================
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 敌人（有贴图的）
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        // 其他贴图（道具 / 地图 / etc）
        // drawXXX(batch);

        batch.end();

        // =================================================
        // 3️⃣ UI
        // =================================================
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        drawHUD(batch);
        batch.end();

        if (currentState == GameState.PAUSED) {
            uiStage.act(delta);
            uiStage.draw();
        }
        // 拿钥匙
        if (key != null && !key.isCollected()) {
            key.checkPickup(player);
        }

// 开门
        if (door != null && !door.isOpen()) {
            door.tryOpen(player);
        }
    }






    //这个写在这里是测试用的，等到结束屏幕做完了之后把这个删掉-----王思衡
    private void winGame(){
        gameWon = true;
        System.out.println("Game won!");
    }













    // ===== 画墙结束 =====
    // ========== 新增：敌人相关方法 =========
    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.update(delta);

                // 如果敌人有PathFinder，让它寻找路径到玩家位置
                if (player != null && pathFinder != null) {
                    // 检查玩家是否在检测范围内
                    float distance = enemy.getPosition().dst(player.getPosition());

                    if (distance <= enemy.getDetectionRange()) {
                        // 玩家在检测范围内，开始寻路追击
                        enemy.findPathTo(player.getPosition());

                        // 如果玩家在攻击范围内，攻击
                        if (distance <= enemy.getAttackRange()) {
                            enemy.attack(player);
                        }
                    } else {
                        // 玩家不在检测范围，清空路径
                        enemy.clearPath();
                    }
                }
            }
        }
        // 移除死亡的敌人
        removeDeadEnemies();
    }

    private void drawEnemies(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.render(batch);
            }
        }
    }

    private void removeDeadEnemies() {
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (!enemies.get(i).isAlive()) {
                enemies.removeIndex(i);
            }
        }
    }

    private void checkCollisions() {
        if(player.getHitbox().overlaps(exitArea)){
            exit.onPlayerReach();
        }
        if(exit.isReached()) {
            winGame();
        }
        if (player != null) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && enemy.getBounds().overlaps(player.getHitbox())) {
                    enemy.attack(player);
                }
            }
        }
    }


    private void buildWalkableGrid() {
        // 从地图数据构建可行走网格
        // 假设地图尺寸为 20x20 个瓦片
        int gridWidth  = (int)(mapPixelWidth / Wall.TILE_SIZE);
        int gridHeight = (int)(mapPixelHeight / Wall.TILE_SIZE);


        walkableGrid = new boolean[gridWidth][gridHeight];

        // 初始化所有格子为可行走
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                walkableGrid[x][y] = true;
            }
        }

        // 将墙壁位置标记为不可行走
        for (Wall wall : walls) {
            int gridX = (int)(wall.worldX / Wall.TILE_SIZE);
            int gridY = (int)(wall.worldY / Wall.TILE_SIZE);

            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                walkableGrid[gridX][gridY] = false;
            }
        }

        // 将walkableGrid传递给所有敌人
        for (Enemy enemy : enemies) {
            enemy.setWalkableGrid(walkableGrid);
        }
    }

    private void updatePlayer(float delta) {
        // 由组员2实现
       if (player != null && controller!=null) {
           boolean up = controller.up;
           boolean down = controller.down;
           boolean left = controller.left;
           boolean right = controller.right;
           boolean run = controller.run;

           player.update(delta, up, down, left, right, run);


       }



    }


    private void drawPlayer(SpriteBatch batch) {
        // 由组员2实现\
        if (player != null) {
            player.render(batch);

        }
    }

    private void drawHUD(SpriteBatch batch) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // 绘制敌人数量
        font.draw(batch, "Enemies: " + enemies.size, 10, camera.viewportHeight - 10);

        // 绘制游戏时间
        font.draw(batch, String.format("Time: %.1f", gameTime),
                camera.viewportWidth - 100, camera.viewportHeight - 10);

        // ========== 新增：绘制陷阱数量 ==========
        if (traps != null) {
            font.draw(batch, "Traps: " + traps.size,
                    camera.viewportWidth - 200, camera.viewportHeight - 10);
        }

        // ========== 新增：绘制玩家状态 ==========
        if (player != null) {
            font.draw(batch, "HP: " + (int)player.getHealth() + "/" + (int)player.getMaxHealth(),
                    10, camera.viewportHeight - 40);

            // 如果玩家在迷雾中，显示提示
            for (Trap trap : traps) {
                if (trap instanceof Fog) {
                    Fog fogTrap = (Fog) trap;
                    if (fogTrap.isPlayerInFog(player)) {
                        font.draw(batch, "FOG AFFECTED! Visibility Reduced",
                                camera.viewportWidth / 2 - 100, 30);
                        break;
                    }
                }
            }
        }
    }

    private void drawDebugInfo() {
        // 使用ShapeRenderer绘制敌人碰撞框（调试用）
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);

            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) {
                    shapeRenderer.rect(
                            enemy.getBounds().x,
                            enemy.getBounds().y,
                            enemy.getBounds().width,
                            enemy.getBounds().height
                    );
                }
            }

            shapeRenderer.end();
        }
    }
    // ========== 新增：更新陷阱 ==========
    private void updateTraps(float delta) {
        if (traps != null) {
            for (Trap trap : traps) {
                trap.update(delta);
            }
        }
    }

    // ========== 新增：检查陷阱激活 ==========
    private void checkTrapActivation() {
        if (player != null && traps != null) {
            for (Trap trap : traps) {
                trap.checkActivation(player);
            }
        }
    }

    // ========== 新增：渲染陷阱 ==========

    @Override
    public void resize(int width, int height) {
       camera.viewportWidth = width;
       camera.viewportHeight = height;
       camera.update();
       uiStage.getViewport().update(width, height, true);
        // 不要在这里固定 camera.position
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        MapLoader loader = new MapLoader();
        shapeRenderer = new ShapeRenderer();

        System.out.println("Loading Map from: " + currentMapPath);
        walls = loader.loadWalls(currentMapPath);
        int maxX = 0, maxY = 0;
        for (Wall w : walls) {
            if (w.gridX > maxX) maxX = w.gridX;
            if (w.gridY > maxY) maxY = w.gridY;
        }
        mapPixelWidth = (maxX + 1) * Wall.TILE_SIZE;
        mapPixelHeight = (maxY + 1) * Wall.TILE_SIZE;
        mapWidthInTiles = maxX + 1;
        mapHeightInTiles = maxY +1;
        key = spawnRandomKey();
        door = spawnRandomDoor();

        key = new Key(300, 200);
        door = new Door(
                mapPixelWidth - Wall.TILE_SIZE,
                mapPixelHeight / 2f,
                Wall.TILE_SIZE,
                Wall.TILE_SIZE
        );
// +1 因为 grid 从 0 开始

        System.out.println("Loaded level " + levelNumber + " walls: " + walls.size());

        System.out.println("Loaded walls: " + walls.size());

        shapeRenderer = new ShapeRenderer();
        // ========== 新增：初始化碰撞地图 ==========
        buildCollisionMap(maxX + 1, maxY + 1);
        // ========== 新增：初始化PathFinder ==========
        initPathFinder();
        // ========== 新增：初始化陷阱 ==========
        initTraps();

        // ========== 新增：初始化敌人和玩家 ==========
        initEnemies();
        initPlayer();
        buildWalkableGrid();
        // =====================================
        //remark code (store the original code)
        // ========== 为敌人设置PathFinder ==========
        for (Enemy enemy : enemies) {
            enemy.setPathFinder(pathFinder);
        }
        currentState = GameState.RUNNING;
        Gdx.input.setInputProcessor(null);

        Music music =game.getBackgroundMusic();
        if (music != null && !music.isPlaying()) {
            music.play();
            music.setLooping(true);

        }
    }


    private Key spawnRandomKey() {
        HashSet<String> wallSet = new HashSet<>();
        for (Wall w : walls) {
            wallSet.add(w.gridX + "," + w.gridY);
        }

        Random random = new Random();

        while (true) {
            int gx = random.nextInt(mapWidthInTiles);
            int gy = random.nextInt(mapHeightInTiles);

            // 如果这个格子不是墙 → 地板
            if (!wallSet.contains(gx + "," + gy)) {
                float x = gx * Wall.TILE_SIZE;
                float y = gy * Wall.TILE_SIZE;
                return new Key(x, y);
            }
        }
    }

    private Door spawnRandomDoor() {
        Random random = new Random();

        int tilesX = (int)(mapPixelWidth / Wall.TILE_SIZE);
        int tilesY = (int)(mapPixelHeight / Wall.TILE_SIZE);

        int side = random.nextInt(4); // 0上 1下 2左 3右
        int gridX = 0, gridY = 0;

        switch (side) {
            case 0: // 上
                gridX = random.nextInt(tilesX - 2) + 1;
                gridY = tilesY - 1;
                break;
            case 1: // 下
                gridX = random.nextInt(tilesX - 2) + 1;
                gridY = 0;
                break;
            case 2: // 左
                gridX = 0;
                gridY = random.nextInt(tilesY - 2) + 1;
                break;
            case 3: // 右
                gridX = tilesX - 1;
                gridY = random.nextInt(tilesY - 2) + 1;
                break;
        }

        return new Door(
                gridX * Wall.TILE_SIZE,
                gridY * Wall.TILE_SIZE,
                Wall.TILE_SIZE,
                Wall.TILE_SIZE
        );
    }
    // ========== 新增：初始化方法 ==========
    private void initEnemies() {
        enemies.add(new NineTailedFox(150, 100));
        enemies.add(new QiongQi(200, 150));
        enemies.add(new ZhuLong(250, 200));

        System.out.println("Initialized " + enemies.size + " enemies");
    }

    private void initPlayer() {
        if (player == null) {
            player = new Player(100, 100); // 仅测试用
            System.out.println("Dummy player initialized for testing!");
        }
    }
    // =====================================

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        // ========== 清理资源 ==========
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        // ========== 新增：清理陷阱资源 ==========
        if (traps != null) {
            for (Trap trap : traps) {
                if (trap instanceof Fog) {
                    ((Fog) trap).dispose();
                } else if (trap instanceof MechanismTrap) {
                    ((MechanismTrap) trap).dispose();
                }
            }
        }

        if (door != null){
            door.dispose();
        }

        if (key != null){
            key.dispose();
        }

        if (shapeRenderer != null) shapeRenderer.dispose();

        // ========== 清理UI ==========
        if (uiStage != null) {
            uiStage.dispose();
        }
    }

    // ========== 新增：Getter方法 ==========
    public List<Wall> getWalls() {
        return walls;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }
    public AStarPathFinder getPathFinder() {
        return pathFinder;
    }

    public Array<Trap> getTraps() {
        return traps;
    }

    public int[][] getCollisionMap() {
        return collisionMap;
    }
    private void updateCameraFollowPlayer() {
        if (player == null) return;

        float targetX = player.getPosition().x;
        float targetY = player.getPosition().y;

        float halfW = camera.viewportWidth * 0.5f * camera.zoom;
        float halfH = camera.viewportHeight * 0.5f * camera.zoom;

        float newX;
        if (mapPixelWidth < camera.viewportWidth * camera.zoom) {

            newX = mapPixelWidth / 2f;
        } else {
            newX = Math.max(halfW, Math.min(targetX, mapPixelWidth - halfW));
        }

        // 4. 计算 Y 轴位置 (同理)
        float newY;
        if (mapPixelHeight < camera.viewportHeight * camera.zoom) {
            newY = mapPixelHeight / 2f;
        } else {
            newY = Math.max(halfH, Math.min(targetY, mapPixelHeight - halfH));
        }
        // 5. 应用新位置
        camera.position.set(newX, newY, 0);

    }
    // ========== 新增：构建碰撞地图 ==========
    private void buildCollisionMap(int width, int height) {
        collisionMap = new int[height][width];

        // 初始化所有格子为可通行（0）
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                collisionMap[y][x] = 0;
            }
        }

        // 将墙壁标记为障碍（1）
        for (Wall wall : walls) {
            if (wall.gridX >= 0 && wall.gridX < width && wall.gridY >= 0 && wall.gridY < height) {
                collisionMap[wall.gridY][wall.gridX] = 1;
            }
        }

        System.out.println("Collision map built: " + width + "x" + height);
    }

    // ========== 新增：初始化PathFinder ==========
    private void initPathFinder() {
        if (collisionMap != null) {
            pathFinder = new AStarPathFinder(collisionMap);
            System.out.println("AStarPathFinder initialized");
        } else {
            System.out.println("Warning: Collision map not built, PathFinder not initialized");
        }
    }

    // ========== 新增：初始化陷阱 ==========
    private void initTraps() {
        traps = new Array<>();

        // 在随机位置生成陷阱（避开玩家起始位置）
        int trapCount = 3 + levelNumber; // 随关卡增加陷阱数量

        for (int i = 0; i < trapCount; i++) {
            // 随机位置，但确保不在墙上
            float x, y;
            boolean validPosition;
            int attempts = 0;

            do {
                validPosition = true;
                x = (float) (Math.random() * (mapPixelWidth - 64));
                y = (float) (Math.random() * (mapPixelHeight - 64));

                // 检查是否在墙上
                int gridX = (int)(x / Wall.TILE_SIZE);
                int gridY = (int)(y / Wall.TILE_SIZE);

                if (collisionMap != null && gridY < collisionMap.length && gridX < collisionMap[0].length) {
                    if (collisionMap[gridY][gridX] == 1) {
                        validPosition = false;
                    }
                }

                // 检查是否太靠近玩家起始位置
                if (player != null) {
                    float distance = (float) Math.sqrt(
                            Math.pow(x - player.getPosition().x, 2) +
                                    Math.pow(y - player.getPosition().y, 2)
                    );
                    if (distance < 100) {
                        validPosition = false;
                    }
                }

                attempts++;
            } while (!validPosition && attempts < 100);

            if (validPosition) {
                // 随机选择陷阱类型
                if (Math.random() > 0.5) {
                    traps.add(new MechanismTrap(x, y));
                } else {
                    traps.add(new Fog(x, y));
                }
            }
        }

        System.out.println("Initialized " + traps.size + " traps");
    }

    private void drawTraps(SpriteBatch batch) {
        if (traps == null) return;

        // ① 先画有贴图的陷阱
        batch.begin();
        for (Trap trap : traps) {
            if (trap.hasTexture()) {
                trap.render(batch);
            }
        }
        batch.end();

        // ② 再画没有贴图的陷阱（fallback 方块）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Trap trap : traps) {
            if (!trap.hasTexture()) {
                shapeRenderer.setColor(
                        trap.isActivated() ? Color.ORANGE : Color.GRAY
                );
                Rectangle b = trap.getBounds();
                shapeRenderer.rect(b.x, b.y, b.width, b.height);
            }
        }
        shapeRenderer.end();
    }
    // Additional methods and logic can be added as needed for the game screen

}
