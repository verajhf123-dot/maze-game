package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;

import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.enemies.NineTailedFox;
import de.tum.cit.fop.maze.enemies.QiongQi;
import de.tum.cit.fop.maze.enemies.ZhuLong;

import java.util.List;
import com.badlogic.gdx.graphics.Color;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private List<Wall> walls;
    private ShapeRenderer shapeRenderer;
    private float sinusInput = 0f;

    // ==== 新添加的敌人相关变量 ====
    private Array<Enemy> enemies;
    private Player player; // 假设组员2会创建Player类
    private boolean[][] walkableGrid; // A*寻路需要的地图网格

    // ==== 其他变量 ====
    private float gameTime = 0f;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(240,160,0);
        camera.zoom = 0.75f;

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        // 初始化敌人列表
        enemies = new Array<>();
    }


    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        // 更新游戏时间
        gameTime += delta;
        // 更新敌人
        updateEnemies(delta);
        // 更新玩家（由组员2实现）
        updatePlayer(delta);
        // 碰撞检测
        checkCollisions();

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen
        camera.update(); // Update the camera


        // Move text in a circular path to have an example of a moving object
        sinusInput += delta;
        float textX = (float) (camera.position.x + Math.sin(sinusInput) * 100);
        float textY = (float) (camera.position.y + Math.cos(sinusInput) * 100);

        // Set up and begin drawing with the sprite batch
        game.getSpriteBatch().setProjectionMatrix(camera.combined);

        game.getSpriteBatch().begin(); // Important to call this before drawing anything

        // Render the text
        font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);

        // Draw the character next to the text :) / We can reuse sinusInput here
        game.getSpriteBatch().draw(
                game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                textX - 96,
                textY - 64,
                64,
                128
        );

        // ========== 新增：绘制敌人 ==========
        drawEnemies(game.getSpriteBatch());
        // ========== 新增：绘制玩家 ==========
        drawPlayer(game.getSpriteBatch());
        // ========== 新增：绘制HUD信息 ==========
        drawHUD(game.getSpriteBatch());

        game.getSpriteBatch().end(); // Important to call this after drawing everything

    if (walls!= null && !walls.isEmpty()) {
        // 让 shapeRenderer 使用和 camera 一样的视图矩阵
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);

        for (Wall wall : walls) {
            shapeRenderer.rect(
                    wall.worldX,
                    wall.worldY,
                    Wall.TILE_SIZE,
                    Wall.TILE_SIZE
            );
        }

        shapeRenderer.end();
    }
    // ========== 新增：绘制调试信息（可选）==========
    drawDebugInfo();
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
                if (enemy.isAlive() && enemy.getBounds().overlaps(player.getBounds())) {
                    enemy.attack(player);
                }
            }
        }
    }

    private void buildWalkableGrid() {
        // 从地图数据构建可行走网格
        // 假设地图尺寸为 20x20 个瓦片
        int gridWidth = 20;
        int gridHeight = 20;

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
        if (player != null) {
            player.update(delta);
        }
    }

    private void drawPlayer(SpriteBatch batch) {
        // 由组员2实现
        if (player != null) {
            player.render(batch);
        }
    }

    private void drawHUD(SpriteBatch batch) {
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
        camera.setToOrtho(false,width,height);
        camera.position.set(240,160,0);
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
        walls = loader.loadWalls("maps/level-1.properties");
        System.out.println("Loaded walls: " + walls.size());

        shapeRenderer = new ShapeRenderer();

        // ========== 新增：初始化敌人和玩家 ==========
        initEnemies();
        initPlayer();
        buildWalkableGrid();
        // =====================================
    }

    // ========== 新增：初始化方法 ==========
    private void initEnemies() {
        // 先创建测试敌人（后期改为从地图加载）
        enemies.add(new NineTailedFox(150, 100));
        enemies.add(new QiongQi(200, 150));
        enemies.add(new ZhuLong(250, 200));

        System.out.println("Initialized " + enemies.size + " enemies");
    }

    private void initPlayer() {
        // 由组员2实现
        // player = new Player(startX, startY);
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

    // Additional methods and logic can be added as needed for the game screen
}
