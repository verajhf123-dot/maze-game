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

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;

import java.util.List;
import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    private float mapPixelWidth;
    private float mapPixelHeight;


    private final MazeRunnerGame game;
    private  OrthographicCamera camera;
    private  BitmapFont font;
    private List<Wall> walls;
    private ShapeRenderer shapeRenderer;
    private float sinusInput = 0f;

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
    private PlayerController controller;
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
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(240,160,0);
        camera.zoom = 0.75f;
        font = new BitmapFont();
        font.getData().setScale(1f);

        controller = new PlayerController(settingsManager);
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
        // deal with the esc
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }
        //only under running that can update the game logic;
        if(currentState==GameState.RUNNING) {
           //关键：每帧更新控制器状态

            // 更新游戏时间
            gameTime += delta;
            // 更新敌人
            updateEnemies(delta);
            // 更新玩家（由组员2实现）
            updatePlayer(delta);
            // 碰撞检测
            checkCollisions();

            updateCameraFollowPlayer();
            camera.update();
        }
        //draw the game picture;

        ScreenUtils.clear(0,0,0,1);

        if(walls!=null&& !walls.isEmpty()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GRAY);
            for (Wall wall : walls) {
                shapeRenderer.rect(wall.worldX,wall.worldY, Wall.TILE_SIZE, Wall.TILE_SIZE);
            }
            shapeRenderer.end();
        }
        if (player != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED); // 显眼的红色
            // 使用 32x32 的大小，方便看
            shapeRenderer.rect(player.getPosition().x, player.getPosition().y, 32, 32);
            shapeRenderer.end();
        }//用这个表示就是player。 把他画出来。

        // 设置投影矩阵
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        //draw the text


        //draw game element;
        drawEnemies(game.getSpriteBatch());
        game.getSpriteBatch().end();

        game.getSpriteBatch().setProjectionMatrix(uiStage.getCamera().combined);
        game.getSpriteBatch().begin();
        drawHUD(game.getSpriteBatch());
        game.getSpriteBatch().end();


        //drawDebugInfo();

        if(currentState==GameState.PAUSED) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
            // draw the button
            uiStage.act(delta);
            uiStage.draw();
        }

    }
    // ===== 画墙结束 =====
    // ========== 新增：敌人相关方法 =========
    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.update(delta);
                // 传递玩家位置给敌人用于AI决策
                if (player != null) {
                    enemy.setTargetPosition(player.getPosition());
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
        if (player != null && controller != null) {

            boolean up = controller.up();
            boolean down = controller.down();
            boolean left = controller.left();
            boolean right = controller.right();
            boolean run = controller.run();

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
        System.out.println("Loading Map from: " + currentMapPath);
        walls = loader.loadWalls(currentMapPath);
        int maxX = 0, maxY = 0;
        for (Wall w : walls) {
            if (w.gridX > maxX) maxX = w.gridX;
            if (w.gridY > maxY) maxY = w.gridY;
        }
// +1 因为 grid 从 0 开始
        mapPixelWidth  = (maxX + 1) * Wall.TILE_SIZE;
        mapPixelHeight = (maxY + 1) * Wall.TILE_SIZE;

        System.out.println("Loaded level " + levelNumber + " walls: " + walls.size());

        System.out.println("Loaded walls: " + walls.size());

        shapeRenderer = new ShapeRenderer();

        // ========== 新增：初始化敌人和玩家 ==========
        initEnemies();
        initPlayer();
        buildWalkableGrid();
        // =====================================
        //remark code (store the original code)
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
        // ========== 新增：清理资源 ==========
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        // =====================================
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


    // Additional methods and logic can be added as needed for the game screen
}
