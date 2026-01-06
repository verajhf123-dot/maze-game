package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;

import java.util.List;
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
import de.tum.cit.fop.maze.items.Item;
import de.tum.cit.fop.maze.items.Xiandan;
import de.tum.cit.fop.maze.items.Yufengfu;
import de.tum.cit.fop.maze.items.Jingangfu;
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
    private List<Key> keys;
    private List<Item> items;
    private Door door;
    private Vector2 exitPosition;
    private Vector2 entryPosition;
    private Texture arrowTexture;
    private TextureRegion arrowRegion;
    private float aiTimer = 0f;
    private float spawnInvulnTimer = 0f; // 出生无敌计时器
    private static final float SPAWN_INVULN_DURATION = 1.0f; // 1秒
    private com.badlogic.gdx.graphics.g2d.GlyphLayout layout;
    private SettingsManager settingsManager;



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
        settingsManager = new SettingsManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(240,160,0);
        camera.zoom = 0.45f;
        font = new BitmapFont();
        font.getData().setScale(1f);
        font =  new BitmapFont();

        controller = new InputController(settingsManager);
        enemies = new Array<>();
        uiStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        currentState = GameState.RUNNING;
        createPauseMenu();
        layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

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
            if (controller.zoomChange != 0) {
                camera.zoom += controller.zoomChange;
                // 限制缩放范围，防止缩太小或太大
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 2.0f);
            }

            gameTime += delta;
            if (spawnInvulnTimer > 0f) {
                spawnInvulnTimer -= delta;
            }

            updateTraps(delta);
            updateEnemies(delta);
            updatePlayer(delta);
            updateItems();
            checkCollisions();
            checkTrapActivation();


            float visibilityFactor = 1.0f;
            if (traps != null) {
                for (Trap trap : traps) {
                    if (trap instanceof Fog) {
                        Fog fog = (Fog) trap;
                        if (fog.isPlayerInFog(player)) {
                            visibilityFactor = fog.getVisibilityReduction(); // 获取能见度 (例如 0.4)
                            fog.activate(player);
                        }
                    }
                }
            }

            if (visibilityFactor < 1.0f) {
                float targetZoom = 0.2f;
                camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, 0.05f);
            }

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

        if (door != null) {
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(
                    door.getX(),
                    door.getY(),
                    door.getWidth(),
                    door.getHeight()
            );
        }


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

        if (traps != null) {
            for (Trap trap : traps) {
                if (!trap.hasTexture()) {
                    shapeRenderer.setColor(trap.isActivated() ? Color.ORANGE : Color.GRAY);
                    Rectangle b = trap.getBounds();
                    shapeRenderer.rect(b.x, b.y, b.width, b.height);
                }
            }
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (traps != null) {
            for (Trap trap : traps) {
                if (trap.hasTexture()) trap.render(batch);
            }
        }

        // 敌人（有贴图的）
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        // ===== 新增：绘制 Item =====
        if (items != null) {
            for (Item item : items) {
                item.render(batch);
            }
        }


        if (keys != null) {
            for (Key k : keys) {

                if (k != null && !k.isCollected()) {
                    k.render(batch);

                    if (player != null) {
                        k.checkPickup(player);
                    }
                }
            }
        }

        if (door != null) door.render(batch);

        if (player != null) {
            player.render(batch);
        }

        batch.end();


        shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barX = 20;
        float barY = uiStage.getViewport().getWorldHeight() - 40; // 位置稍微调高一点，别挡住字
        float barW = 200;
        float barH = 20;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barW, barH);

        if (player != null) {
            float hpPercent = player.getHealth() / player.getMaxHealth();
            if (hpPercent < 0) hpPercent = 0;
            if (hpPercent > 1) hpPercent = 1;

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(barX, barY, barW * hpPercent, barH);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        drawHUD(batch);
        batch.end();

        if(currentState==GameState.PAUSED) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.5f);
            shapeRenderer.rect(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
            // draw the button
            uiStage.act(delta);
            uiStage.draw();
        }

    }







    private void winGame() {
        // 1. 防止重复触发
        if (gameWon) return;
        gameWon = true;

        int levelBonus = 1000 + (int)player.getHealth() * 5 - (int)gameTime;
        if (levelBonus < 0) levelBonus = 0;
        game.globalScore += levelBonus;


        game.setScreen(new ResultScreen(game, true, levelNumber, game.globalScore));
    }



    private void  updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (! enemy.isAlive()) {
              continue;
            }
                enemy.update(delta);

                // 如果敌人有PathFinder，让它寻找路径到玩家位置
                if (player != null && pathFinder != null) {
                    // 检查玩家是否在检测范围内
                    float distance = enemy.getPosition().dst(player.getPosition());

                    if (distance <= enemy.getDetectionRange()) {
                        // 玩家在检测范围内，开始寻路追击
                        enemy.findPathTo(player.getPosition());

                        // 如果玩家在攻击范围内，攻击
                        if (distance <= enemy.getAttackRange() && spawnInvulnTimer<= 0f) {
                            enemy.attack(player);
                        }
                    } else {
                        // 玩家不在检测范围，清空路径
                        enemy.clearPath();
                    }
                }
            }

        for (int i = enemies.size - 1; i >= 0; i--){
        if (!enemies.get(i).isAlive()) enemies.removeIndex(i);}
    }



    private void checkCollisions() {
    if (player == null) return;

        if (door != null && player.getHitbox().overlaps(door.getBounds())) {

            if (player.getStats().hasKey()) {
                door.tryOpen(player); // 让门变成开启状态（如果有动画的话）
                System.out.println("Door opened! Level Completed!");

                winGame();
            }
            else {
                float delta = Gdx.graphics.getDeltaTime();
                com.badlogic.gdx.math.Vector2 velocity = player.getVelocity();

                // 物理回弹：把玩家推回上一帧的位置
                player.getHitbox().x -= velocity.x * delta;
                player.getHitbox().y -= velocity.y * delta;
                player.syncPositionToHitbox();

            }
        }

        // 2) 敌人碰撞伤害
        if (spawnInvulnTimer > 0f) return;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getBounds().overlaps(player.getHitbox())) {
                enemy.attack(player);
                player.triggerDamageVFX();

            }
        }

        if(player.getHealth() <= 0) {
            HighScoreManager.saveScore(game.globalScore);
            game.resetGlobalScore();
            game.setScreen(new ResultScreen(game, false, levelNumber, 0));
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

           player.update(delta, up, down, left, right, run,walls);


       }


    }



    private boolean collidesWithAnyWall(Rectangle hb) {
        if (walls == null) return false;

        for (Wall wall : walls) {
            if (hb.overlaps(wall.getBounds())) {
                return true;
            }
        }
        return false;
    }



    private void drawHUD(SpriteBatch batch) {
        batch.setColor(Color.WHITE); // 重置颜色状态 [cite: 157]

        // 使用 UI 视口的高度，确保 UI 不随相机缩放而变小 [cite: 114]
        float uiH = uiStage.getViewport().getWorldHeight();
        float uiW = uiStage.getViewport().getWorldWidth();

        font.getData().setScale(1.5f); // 保持字体清晰

        font.draw(batch, "HP: " + (int)player.getHealth(), 20, uiH - 40);

        String keyLabel = player.getStats().hasKey() ? "KEY: FOUND" : "KEY: MISSING";
        font.setColor(player.getStats().hasKey() ? Color.GOLD : Color.FIREBRICK);
        font.draw(batch, keyLabel, 20, uiH - 90);

        font.setColor(Color.WHITE);
        String levelText = "LEVEL " + levelNumber;
        layout.setText(font, levelText);
        float levelTextWidth = layout.width;

        font.draw(batch, levelText, uiW - levelTextWidth - 20, uiH - 20);
        String timeText = "TIME: " + (int)gameTime + "s";
        layout.setText(font, timeText);
        float timeTextWidth = layout.width;
        font.draw(batch, timeText, uiW - timeTextWidth - 20, uiH - 50);

        if (exitPosition != null && arrowRegion != null&& player!=null) {
            float tx = exitPosition.x * Wall.TILE_SIZE;
            float ty = exitPosition.y * Wall.TILE_SIZE;
            float dx = tx - player.getPosition().x;
            float dy = ty - player.getPosition().y;

            float currentAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;

            // 在屏幕右下角绘制旋转箭头
            batch.draw(arrowRegion,
                    uiW - 100, 100,
                    16, 16,
                    32, 32,
                    1.2f, 1.2f,
                    currentAngle
            );
            font.draw(batch, "EXIT", uiW - 110, 60);
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
       uiStage.getViewport().update(width, height, false);
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

        String actualPathToLoad;

        if (levelNumber > 5 || !Gdx.files.local("maps/level-" + levelNumber + ".properties").exists()) {
            System.out.println("Entering Endless Mode: Base Map is Level 5");
            actualPathToLoad = "maps/level-5.properties";
        } else {
            actualPathToLoad = "maps/level-" + levelNumber + ".properties";
        }
        MapLoader.LevelData data = loader.loadLevel(actualPathToLoad);

        this.walls = data.walls;
        this.entryPosition = data.entryPosition;
        this.exitPosition = data.exitPosition;
        if (this.exitPosition != null) {
            this.exit = new Exit();
            door = new Door(
                    exitPosition.x * Wall.TILE_SIZE,
                    exitPosition.y * Wall.TILE_SIZE,
                    Wall.TILE_SIZE,
                    Wall.TILE_SIZE
            );

            this.exitArea = new Rectangle(
                    exitPosition.x * Wall.TILE_SIZE,
                    exitPosition.y * Wall.TILE_SIZE,
                    Wall.TILE_SIZE,
                    Wall.TILE_SIZE);

        } else {
            System.out.println("Warning: No exit position found in map file!");
        }

        this.enemies.clear();
        int maxEnemies = levelNumber+1;
        int currentEnemies = 0;
        for(Enemy e : data.enemies) {
            if(currentEnemies < maxEnemies) {
                this.enemies.add(e);
                currentEnemies++;
            }
        }
        System.out.println("Loaded " + currentEnemies + " enemies");



        if (levelNumber > 5) {
            Random rand = new Random();
            for (int i = walls.size() - 1; i >= 0; i--) {
                if (rand.nextFloat() < 0.1f) {
                    walls.remove(i);
                }
            }
            for (int i = 0; i < 5; i++) {
                Vector2 pos = getRandomEmptyTile();
                walls.add(new Wall((int)pos.x, (int)pos.y));
            }
        }


        int maxX = 0, maxY = 0;
        for (Wall w : walls) {
            if (w.gridX > maxX) maxX = w.gridX;
            if (w.gridY > maxY) maxY = w.gridY;
        }
        mapPixelWidth = (maxX + 1) * Wall.TILE_SIZE;
        mapPixelHeight = (maxY + 1) * Wall.TILE_SIZE;
        mapWidthInTiles = maxX + 1;
        mapHeightInTiles = maxY +1;

        this.keys = new ArrayList<>(); // 初始化列表


        keys.add(spawnSafeKey(false));

        for (int i = 0; i < 2; i++) {
            keys.add(spawnSafeKey(true));
        }

        items = new ArrayList<>();

        // 测试用：先直接生成几个
        Vector2 p1 = getRandomEmptyTile();
        items.add(new Xiandan(p1.x * Wall.TILE_SIZE, p1.y * Wall.TILE_SIZE));

        Vector2 p2 = getRandomEmptyTile();
        items.add(new Yufengfu(p2.x * Wall.TILE_SIZE, p2.y * Wall.TILE_SIZE));

        Vector2 p3 = getRandomEmptyTile();
        items.add(new Jingangfu(p3.x * Wall.TILE_SIZE, p3.y * Wall.TILE_SIZE));

        System.out.println("Loaded level " + levelNumber + " walls: " + walls.size());

        System.out.println("Loaded walls: " + walls.size());

        try {
            arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
            arrowRegion = new TextureRegion(arrowTexture);
        } catch(Exception e) {
            System.out.println("No arrow.png found, arrow will not show.");
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fillTriangle(0, 0, 0, 32, 32, 16);
            arrowTexture = new Texture(pixmap);
            arrowRegion = new TextureRegion(arrowTexture);
            pixmap.dispose();

        }

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


    // ========== 新增：初始化方法 ==========
    private void initEnemies() {
        enemies.clear();
        int count = 3 + (levelNumber / 2);

        for (int i = 0; i < count; i++) {

            Vector2 smartPos = getSmartSpawnPosition();

            if (smartPos != null) {
                float rand = (float)Math.random();
                Enemy e;
                if (rand < 0.33f) {
                    e = new NineTailedFox(smartPos.x, smartPos.y);
                } else if (rand < 0.66f) {
                    e = new QiongQi(smartPos.x, smartPos.y);
                } else {
                    e = new ZhuLong(smartPos.x, smartPos.y);
                }

                e.adjustDifficulty(this.levelNumber);
                enemies.add(e);
            }
        }
    }

    private void initPlayer() {
        if (player != null) return;

        float spawnX;
        float spawnY;

        if (entryPosition != null) {
            spawnX = entryPosition.x;
            spawnY = entryPosition.y;
            System.out.println("Spawned at Map Entry (Type 1): " + spawnX + "," + spawnY);
        }

        else if (exitPosition != null) {

            spawnX = exitPosition.x * Wall.TILE_SIZE;
            spawnY = exitPosition.y * Wall.TILE_SIZE;
            System.out.println("Spawned at Map Exit (Type 2): " + spawnX + "," + spawnY);
        }
        else {
            spawnX = 50;
            spawnY = 50;
            System.out.println("No Entry/Exit found, using default: 50,50");
        }

        player = new Player(spawnX, spawnY);

        spawnInvulnTimer = SPAWN_INVULN_DURATION;

        if (player.getStats() != null) {
            player.getStats().heal(100);
        }
        player.syncPositionToHitbox();
    }



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

        if (keys != null){
            for (Key k : keys) {
                k.dispose();
            }
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

        int trapCount = Math.min(3 + levelNumber, 15);

        for (int i = 0; i < trapCount; i++) {

            Vector2 smartPos = getSmartSpawnPosition();

            if (smartPos != null) {

                if (Math.random() > 0.5) {
                    traps.add(new MechanismTrap(smartPos.x, smartPos.y));
                } else {
                    traps.add(new Fog(smartPos.x, smartPos.y));
                }
            }
        }

        System.out.println("Initialized " + traps.size + " smart traps");
    }


    private Vector2 getRandomEmptyTile() {
        List<Vector2> emptyTiles = new ArrayList<>();
        HashSet<String> occupied = new HashSet<>();

        if (walls != null) {
            for (Wall w : walls) {
                occupied.add(w.gridX + "," + w.gridY);
            }
        }
        if (entryPosition != null) occupied.add((int)entryPosition.x + "," + (int)entryPosition.y);
        if (exitPosition != null) occupied.add((int)exitPosition.x + "," + (int)exitPosition.y);


        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                String key = x + "," + y;
                if (!occupied.contains(key)) {
                    emptyTiles.add(new Vector2(x, y));
                }
            }
        }

        if (!emptyTiles.isEmpty()) {
            Random random = new Random();
            return emptyTiles.get(random.nextInt(emptyTiles.size()));
        }
        return new Vector2(1, 1);
    }

    private Key spawnSafeKey(boolean isBonus) {
        Vector2 pos = getRandomEmptyTile();

        return new Key(pos.x * Wall.TILE_SIZE, pos.y * Wall.TILE_SIZE, isBonus);
    }


    private Vector2 getSmartSpawnPosition() {
        int attempts = 0;
        while (attempts < 150) {
            Vector2 tilePos = getRandomEmptyTile(); // 获取一个没墙的格子坐标
            float worldX = tilePos.x * Wall.TILE_SIZE;
            float worldY = tilePos.y * Wall.TILE_SIZE;

            if (player != null && player.getPosition().dst(worldX, worldY) < 250) {
                attempts++;
                continue;
            }

            if (exitPosition != null) {
                float exitWorldX = exitPosition.x * Wall.TILE_SIZE;
                float exitWorldY = exitPosition.y * Wall.TILE_SIZE;
                if (Vector2.dst(worldX, worldY, exitWorldX, exitWorldY) < 100) {
                    attempts++;
                    continue;
                }
            }

            boolean tooCloseToOthers = false;
            for (Enemy e : enemies) {
                if (e.getPosition().dst(worldX, worldY) < 120) {
                    tooCloseToOthers = true;
                    break;
                }
            }
            if (tooCloseToOthers) {
                attempts++;
                continue;
            }

            return new Vector2(worldX, worldY); // 找到了完美位置！
        }
        return null;
    }

    private void updateItems() {
        if (items == null || player == null) return;

        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            if (player.getHitbox().overlaps(item.getBounds())) {
                item.onPickup(player);
                items.remove(i); // 拾取后消失
            }
        }
    }



}
