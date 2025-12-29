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
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Exit;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.items.Key;
import de.tum.cit.fop.maze.traps.Trap;
import de.tum.cit.fop.maze.traps.Fog;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import java.util.ArrayList;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Rectangle;



import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    private float mapPixelWidth;
    private float mapPixelHeight;
    private boolean gameWon = false;
    private Key key;
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
    private Vector2 exitPosition;
    private Texture arrowTexture;
    private TextureRegion arrowRegion;
    private float aiTimer = 0f;
    private float spawnInvulnTimer = 0f; // 出生无敌计时器
    private static final float SPAWN_INVULN_DURATION = 1.0f; // 1秒
    private com.badlogic.gdx.graphics.g2d.GlyphLayout layout;



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
        camera.zoom = 0.45f;
        font = new BitmapFont();
        font.getData().setScale(1f);
        font =  new BitmapFont();

        controller = new InputController();
        enemies = new Array<>();
        traps = new Array<>();
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

    private void drawHealthBar(){
        if (player == null) return;


        shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barX = 10;
        float barY = camera.viewportHeight - 60;
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        if (player.getMaxHealth() > 0) {
            float hpPercent = player.getHealth() / player.getMaxHealth();
            if (hpPercent < 0) hpPercent = 0;
            if (hpPercent > 1) hpPercent = 1;

            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
        }

        shapeRenderer.end();

    }



    // Screen interface methods with necessary functionality
    @Override

    public void render(float delta) {
        // deal with the esc
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }
        //only under running that can update the game logic;
        if (currentState == GameState.RUNNING) {
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += 0.01f; // 缩小 (看更多)
            if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= 0.01f; // 放大 (看细节)
            camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 1.2f);

    // 关键：每帧更新控制器状态
    controller.update();
    // 更新游戏时间
    gameTime += delta;
            if (spawnInvulnTimer > 0f) {
                spawnInvulnTimer -= delta;
            }


            updateTraps(delta);
    // 更新敌人
    updateEnemies(delta);
    // 更新玩家
    updatePlayer(delta);
    // 碰撞检测
    checkCollisions();
    // 陷阱激活检测
    checkTrapActivation();
    // 让玩家永远在屏幕正中间（你的镜头跟随逻辑）
    updateCameraFollowPlayer();
    camera.update();

}

        //draw the game picture;
        if (key != null && !key.isCollected()) {
            key.checkPickup(player);
        }
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
        drawHealthBar();

        // 设置投影矩阵
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        //draw the text

        drawTraps(game.getSpriteBatch());
        //draw game element;
        drawEnemies(game.getSpriteBatch());
        drawPlayer(game.getSpriteBatch());
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
            // 1. 防止重复触发 (比如一帧内多次碰撞)
            if (gameWon) return;
            gameWon = true;

            int nextLevel = levelNumber + 1;
            SaveManager.saveGame(nextLevel, 100f, false);
            System.out.println("Saving Progress: Unlocked Level " + nextLevel);

            int score = (int) (gameTime * 10);


            game.setScreen(new ResultScreen(game, true, levelNumber, score));

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
    if (player == null) return;

        if (exitArea != null && player.getHitbox().overlaps(exitArea)) {
            // to see if player has a key
            if (player.getStats().hasKey()) {
                exit.onPlayerReach();
                if (exit.isReached()) {
                    winGame();
                }
            } else {
                float delta = Gdx.graphics.getDeltaTime();
                // 获取玩家当前的速度
                com.badlogic.gdx.math.Vector2 velocity = player.getVelocity();

                // 将玩家的 hitbox 坐标往回拉（抵消本帧的移动）
                player.getHitbox().x -= velocity.x * delta;
                player.getHitbox().y -= velocity.y * delta;

                // 同步玩家的逻辑坐标到 hitbox
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
            game.setScreen(new ResultScreen(game, false, levelNumber, 0));
        }
}





    private void buildWalkableGrid() {
        int gridWidth  = (int)(mapPixelWidth / Wall.TILE_SIZE);
        int gridHeight = (int)(mapPixelHeight / Wall.TILE_SIZE);


        walkableGrid = new boolean[gridWidth][gridHeight];


        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                walkableGrid[x][y] = true;
            }
        }

        for (Wall wall : walls) {
            int gridX = (int)(wall.worldX / Wall.TILE_SIZE);
            int gridY = (int)(wall.worldY / Wall.TILE_SIZE);

            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                walkableGrid[gridX][gridY] = false;
            }
        }

        for (Enemy enemy : enemies) {
            enemy.setWalkableGrid(walkableGrid);
        }
    }


    private void updateEnemies(float delta) {
        aiTimer += delta;
        boolean shouldRecalculatePath = false;

        if (aiTimer >= 0.5f) {
            shouldRecalculatePath = true;
            aiTimer = 0f; // 重置计时器
        }

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.update(delta);
                if (shouldRecalculatePath && player != null && pathFinder != null) {

                    float distance = enemy.getPosition().dst(player.getPosition());

                    if (distance <= enemy.getDetectionRange()) {
                        enemy.findPathTo(player.getPosition());
                        if (distance <= enemy.getAttackRange()) {
                            enemy.attack(player);
                        }
                    } else {
                        enemy.clearPath();
                    }
                }
            }
        }

        // 移除死亡的敌人
        removeDeadEnemies();
    }

    private void updatePlayer(float delta) {
        if (player == null || controller == null) return;

        controller.update(); // 每帧刷新按键状态

        player.update(delta,
                controller.up,
                controller.down,
                controller.left,
                controller.right,
                controller.run
        );

        // 用速度算本帧要走多少
        float dx = player.getVelocity().x * delta;
        float dy = player.getVelocity().y * delta;

        moveEntityWithWallCollision(player, dx, dy);
    }
    private void moveEntityWithWallCollision(CollidableEntity entity, float dx, float dy) {
        Rectangle hb = entity.getHitbox();

        float oldX = hb.x;
        hb.x += dx;
        if (collidesWithAnyWall(hb)|| hb.x < 0 || hb.x + hb.width > mapPixelWidth) {
            hb.x = oldX;
        }

        // 尝试 Y 轴移动
        float oldY = hb.y;
        hb.y += dy;
        if (collidesWithAnyWall(hb)|| hb.y < 0 || hb.y + hb.height > mapPixelHeight) {
            hb.y = oldY;
        }

        entity.syncPositionToHitbox();
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




    private void drawPlayer(SpriteBatch batch) {
        // 由组员2实现\
        if (player != null) {
            player.render(batch);

        }
    }

    private void drawHUD(SpriteBatch batch) {
        batch.setColor(Color.WHITE); // 重置颜色状态 [cite: 157]

        // 使用 UI 视口的高度，确保 UI 不随相机缩放而变小 [cite: 114]
        float uiH = uiStage.getViewport().getWorldHeight();
        float uiW = uiStage.getViewport().getWorldWidth();

        font.getData().setScale(1.5f); // 保持字体清晰

        // 1. 绘制生命值 (Lives) [cite: 45, 46]
        font.draw(batch, "HP: " + (int)player.getHealth(), 20, uiH - 40);

        // 2. 绘制钥匙状态 [cite: 45, 47]
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
        if (exitPosition != null && arrowRegion != null) {

            float tx = exitPosition.x * Wall.TILE_SIZE;
            float ty = exitPosition.y * Wall.TILE_SIZE;
            float dx = tx - player.getPosition().x;
            float dy = ty - player.getPosition().y;

            // 使用 MathUtils 计算角度（弧度转角度）
            float currentAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;

            // 在屏幕右下角绘制旋转箭头
            batch.draw(arrowRegion,
                    uiW - 100, 100,           // 屏幕位置
                    16, 16,                   // 旋转中心 (Region中心)
                    32, 32,                   // 渲染尺寸
                    1.2f, 1.2f,               // 缩放
                    currentAngle              // 计算出的实时角度
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
    private void drawTraps(SpriteBatch batch) {
        if (traps != null) {
            for (Trap trap : traps) {
                trap.render(batch);
            }
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
        key = new Key(300, 200);
        System.out.println("Loading Map from: " + currentMapPath);

        MapLoader.LevelData data = loader.loadLevel(currentMapPath);

        this.walls = data.walls;
        this.exitPosition = data.exitPosition;

        if (this.exitPosition != null) {
            this.exit = new Exit();
            this.exitArea = new Rectangle(exitPosition.x, exitPosition.y, Wall.TILE_SIZE, Wall.TILE_SIZE);

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

        this.traps.clear();
        int maxTraps = levelNumber+2;
        int currentTraps = 0;
        for(Trap t : data.traps) {
            if(currentTraps < maxTraps) {
                this.traps.add(t);
                currentTraps++;
            }
        }
        int maxX = 0, maxY = 0;
        for (Wall w : walls) {
            if (w.gridX > maxX) maxX = w.gridX;
            if (w.gridY > maxY) maxY = w.gridY;
        }
// +1 因为 grid 从 0 开始
        mapPixelWidth  = (maxX + 1) * Wall.TILE_SIZE;
        mapPixelHeight = (maxY + 1) * Wall.TILE_SIZE;

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
        shapeRenderer = new ShapeRenderer();
        buildCollisionMap(maxX + 1, maxY + 1);
        initPathFinder();

        initPlayer();
        buildWalkableGrid();
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
        enemies.add(new NineTailedFox(150, 100));
        enemies.add(new QiongQi(200, 150));
        enemies.add(new ZhuLong(250, 200));

        System.out.println("Initialized " + enemies.size + " enemies");
    }

    private void initPlayer() {
        if (player != null) return;

        float hbW = 24f;
        float hbH = 24f;

        if (collisionMap == null) {
            player = new Player(50, 50);
            spawnInvulnTimer = SPAWN_INVULN_DURATION;

            player.getStats().heal(100);
            System.out.println("Warning: collisionMap null, fallback spawn");
            return;
        }

        int margin = 6;

        float px = 0;
        float py = 0;
        int x = 0;
        int y;
        for (y = margin; y < collisionMap.length - margin; y++) {
            for (x = margin; x < collisionMap[0].length - margin; x++) {
                if (collisionMap[y][x] == 0) {

                    px = x * Wall.TILE_SIZE + (Wall.TILE_SIZE - hbW) / 2f;
                    py = y * Wall.TILE_SIZE + (Wall.TILE_SIZE - hbH) / 2f;

                    float minDist = 6f * Wall.TILE_SIZE;     // 至少离敌人2格
                    if (!farFromEnemies(px, py, minDist)) {  // 不满足就继续找
                        continue;
                    }

                    player = new Player(px, py);
                    player.getStats().heal(100);
                    player.syncPositionToHitbox();

                    System.out.println(
                            "Player spawned at tile (" + x + "," + y + ") -> (" + px + "," + py + ")"
                    );
                    return;
                }
            }

        }

        // 兜底
        player = new Player(px, py);
        player.getStats().heal(100);
        player.syncPositionToHitbox();

        System.out.println(
                "Player spawned at tile (" + x + "," + y + ") -> (" + px + "," + py + ")"
        );
        System.out.println("Spawn HP = " + player.getHealth() + " / " + player.getMaxHealth());
        return;


    }

    private boolean farFromEnemies(float px, float py, float minDist) {
        if (enemies == null) return true;

        Vector2 p = new Vector2(px, py);
        for (Enemy e : enemies) {
            if (e != null && e.isAlive()) {
                if (e.getPosition().dst(p) < minDist) return false;
            }
        }
        return true;
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

        float cx = player.getPosition().x + 16f;
        float cy = player.getPosition().y + 16f;

        camera.position.set(cx, cy, 0f);
        camera.update();
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





    // Additional methods and logic can be added as needed for the game screen
}
