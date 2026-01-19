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
import de.tum.cit.fop.maze.enemies.Projectile;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable; // 需要导入
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.items.Key;
import de.tum.cit.fop.maze.traps.Trap;
import de.tum.cit.fop.maze.traps.Fog;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import de.tum.cit.fop.maze.progression.SkillTreeScreen;
import de.tum.cit.fop.maze.progression.SkillTree;
import de.tum.cit.fop.maze.progression.ExperienceSystem;
import de.tum.cit.fop.maze.progression.SkillManager;

import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import de.tum.cit.fop.maze.items.Item;
import de.tum.cit.fop.maze.items.Xiandan;
import de.tum.cit.fop.maze.items.Yufengfu;
import de.tum.cit.fop.maze.items.Jingangfu;

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
    private OrthographicCamera camera;
    private BitmapFont font;
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
    private int levelNumber;
    private String currentMapPath;
    private InputController controller;
    private Exit exit;
    private List<Key> keys;
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
    private int minX, maxX, minY, maxY;

    private PlayerStats previousStats;// for store the last level



    private Texture floorTexture;
    private Texture wallTexture;
    private Texture fireballTexture; // 火球图片
    private Array<Projectile> projectiles; // 管理所有飞行的火球


    private Music mapMusic;
    private Music pauseMusic;

    private com.badlogic.gdx.audio.Sound attackSound;
    private com.badlogic.gdx.audio.Sound fogSound;
    private com.badlogic.gdx.audio.Sound bonusSound;
    private com.badlogic.gdx.audio.Sound buttonSound;
    private com.badlogic.gdx.audio.Sound mechanismSound; // 机关声音
    private com.badlogic.gdx.audio.Sound keySound;
    private com.badlogic.gdx.audio.Sound fireballSound;   // 火球音效 (Q)
    private com.badlogic.gdx.audio.Sound lightningSound;  // 闪电音效 (R)
    private com.badlogic.gdx.audio.Sound healSkillSound;  // 治疗音效 (E)
    private com.badlogic.gdx.audio.Sound swingSound;      // 挥剑空气声 (攻击动作)
    private float fogSoundTimer = 0f;// 迷雾声音的冷却计时器
    private Texture texHpFrame;
    private Texture texHpBar;
    private Texture texTaiji;
    private List<Item> items;
    private Texture healTexture;


    private DeveloperConsole console; // 声明控制台

    private AchievementManager achievementManager;
    private Texture[] floorVariants;
    private byte[][] floorPick;

    // 新增：按钮样式相关
    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;
    private Texture keyIconTexture;

    // 新增：HUD 背景框 Texture
    private Texture hudFrameTexture;

    private boolean isInitialized = false;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this(game, 1,null);// 默认进 Level 1
    }


    public GameScreen(MazeRunnerGame game, int levelNumber,PlayerStats prevStats) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.currentMapPath = "maps/level-" + levelNumber + ".properties";
        this.previousStats = prevStats;

        if (this.previousStats != null) {
            this.previousStats.setBonusKey(0); // 重置加分钥匙数量
            this.previousStats.setHasKey(false); // 重置通关钥匙状态
        }
        initCommon();
    }

    private void initCommon() {
        settingsManager = new SettingsManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(240, 160, 0);
        camera.zoom = 0.45f;
        font = new BitmapFont();
        font.getData().setScale(1f);
        font = new BitmapFont();

        controller = new InputController(settingsManager);
        enemies = new Array<>();
        uiStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        currentState = GameState.RUNNING;

        // 1. 先初始化按钮样式
        createButtonStyle();
        // 2. 再创建菜单
        createPauseMenu();
        layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    }

    public GameScreen(MazeRunnerGame game, String mapFilePath) {
        this.game = game;
        this.levelNumber = 0;
        this.currentMapPath = mapFilePath;
        initCommon();

    }

    // 新增：创建按钮样式的方法 (只在 initCommon 里调用一次)
    private void createButtonStyle() {
        commonButtonStyle = new TextButton.TextButtonStyle();

        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        commonButtonStyle.font = baseFont;

        commonButtonStyle.fontColor = Color.WHITE;
        commonButtonStyle.downFontColor = Color.LIGHT_GRAY;

        try {
            if (buttonBg == null) {
                buttonBg = new Texture(Gdx.files.internal("button2.png"));
            }
            TextureRegionDrawable drawable = new TextureRegionDrawable(buttonBg);

            commonButtonStyle.up = drawable;
            commonButtonStyle.down = drawable.tint(Color.LIGHT_GRAY);

        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Button texture (button2.png) not found! Reverting to default.");
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    // creat pauseSetting
    private void createPauseMenu() {
        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();
        pauseMenuTable.setDebug(false);
        pauseMenuTable.setVisible(false);

        Label pauseLable = new Label("Game PAUSED", game.getSkin(), "title");
        pauseMenuTable.add(pauseLable).padBottom(40).row();

        // Resume Button (已修改样式)
        TextButton resumeButton = new TextButton("Resume", commonButtonStyle);
        resumeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                togglePause();
            }
        });
        pauseMenuTable.add(resumeButton).width(350).height(80).padBottom(15).row(); // 调整尺寸

        // Music Button (已修改样式)
        TextButton musicButton = new TextButton("Music:ON/OFF", commonButtonStyle);
        musicButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                Music music = game.getBackgroundMusic();
                if (music != null) {
                    if (music.isPlaying()) music.pause();
                    else music.play();
                }

            }
        });
        pauseMenuTable.add(musicButton).width(350).height(80).padBottom(15).row(); // 调整尺寸

        // Quit Button (已修改样式)
        TextButton quitButton = new TextButton("Exit to Menu", commonButtonStyle);
        quitButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();

                if (pauseMusic != null) {
                    pauseMusic.stop();
                }
                if (mapMusic != null) {
                    mapMusic.stop();
                }
                game.playMenuMusic();

                game.goToMenu();

            }
        });
        pauseMenuTable.add(quitButton).width(350).height(80).row(); // 调整尺寸

        uiStage.addActor(pauseMenuTable);
    }

    // switch the pauseState
    private void togglePause() {
        Music music = game.getBackgroundMusic();
        if (currentState == GameState.RUNNING) {
            currentState = GameState.PAUSED;
            pauseMenuTable.setVisible(true);
            Gdx.input.setInputProcessor(uiStage);
            if (mapMusic != null) mapMusic.pause();
            if (pauseMusic != null) pauseMusic.play();

        } else {
            currentState = GameState.RUNNING;
            pauseMenuTable.setVisible(false);
            Gdx.input.setInputProcessor(null);
            if (pauseMusic != null) pauseMusic.stop();
            if (mapMusic != null) mapMusic.play();

        }
    }

    // Screen interface methods with necessary functionality
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            console.toggleConsole();

            if (console.isVisible()) {
                Gdx.input.setInputProcessor(uiStage);
            } else {
                // 关闭控制台时，检查当前是不是暂停状态
                if (currentState == GameState.PAUSED) {
                    Gdx.input.setInputProcessor(uiStage); // 保持 UI 输入
                } else {
                    Gdx.input.setInputProcessor(null); // 恢复角色控制
                }
            }


        }


        // 1. 全局输入检测 (ESC暂停)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // ============================================================

        // 我把你分散的几个 if 块合并了，确保顺序正确
        // ============================================================
        if (currentState == GameState.RUNNING&&!console.isVisible()) {

            // --- A. 输入处理 ---
            controller.update();
            handleSkillInput(); // 处理 T 键技能树 和 Shift 冲刺

            // 处理 Q/E/R 技能
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) useSkill1();
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) useSkill2();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) useSkill3();

            // 处理 Space 攻击 (Todolist 2)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (player != null) {
                    player.performAttack();
                    if (swingSound != null) swingSound.play(0.5f);
                    // if (attackSound != null) attackSound.play(0.5f);
                }
            }

            // --- B. 相机缩放 ---
            if (controller.zoomChange != 0) {
                camera.zoom += controller.zoomChange;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 2.0f);
            }

            // --- C. 计时器更新 ---
            gameTime += delta;
            if (spawnInvulnTimer > 0f) spawnInvulnTimer -= delta;
            if (fogSoundTimer > 0) fogSoundTimer -= delta;

            // --- D. 实体位置更新 (先让大家动起来) ---
            updateTraps(delta);
            updateEnemies(delta);
            updatePlayer(delta);

            if (projectiles != null) {
                for (int i = projectiles.size - 1; i >= 0; i--) {
                    Projectile p = projectiles.get(i);
                    p.update(delta);

                    // 检查是否击中敌人
                    if (p.checkEnemyHit(enemies)) {
                        System.out.println("Fireball hit enemy!");
                        // 这里可以加一个击中音效，比如 attackSound.play();
                    }

                    // 如果火球销毁了（击中或超时），从列表中移除
                    if (!p.isActive()) {
                        projectiles.removeIndex(i);
                    }
                }
            }

            // --- E. 碰撞与交互判定 (要在移动之后做) ---
            checkCollisions();
            checkTrapActivation();

            checkPlayerAttackHit();
            checkSkillCollisions(delta);
            updateKeys();
            updateItems();

            // --- F. 技能与视效 ---
            if (player != null && player.getStats() != null) {
                SkillManager skillManager = player.getStats().getSkillManager();
                if (skillManager != null) {
                    skillManager.update(delta);
                }
            }

            // 处理迷雾
            float visibilityFactor = 1.0f;
            if (traps != null) {
                for (Trap trap : traps) {
                    if (trap instanceof Fog) {
                        Fog fog = (Fog) trap;
                        if (fog.isPlayerInFog(player)) {
                            visibilityFactor = fog.getVisibilityReduction();
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


        // ============================================================
        // 🎨 渲染绘制部分 (Rendering)
        // 保留了你原本的 ShapeRenderer 和 SpriteBatch 结构
        // ============================================================

        ScreenUtils.clear(0, 0, 0, 1);
        SpriteBatch batch = game.getSpriteBatch();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {

                Texture tex = floorVariants[floorPick[x][y]];  // 0= floor.png, 1=flower, 2=grass
                batch.draw(
                        tex,
                        x * Wall.TILE_SIZE,
                        y * Wall.TILE_SIZE,
                        Wall.TILE_SIZE,
                        Wall.TILE_SIZE
                );
            }
        }

        batch.end();

        // --- 2. ShapeRenderer (调试框/无图物体) ---
        // 这一层在贴图层下面，如果有贴图会被盖住，这很好
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (door != null) {
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(door.getX(), door.getY(), door.getWidth(), door.getHeight());
        }


        shapeRenderer.end();


        // --- 3. SpriteBatch (实体贴图) ---
        // 这里的顺序决定遮挡关系 (画家算法：后画的盖住先画的)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Layer A: 墙壁
        if (walls != null) {
            for (Wall wall : walls) {
                batch.draw(wallTexture, wall.worldX, wall.worldY, Wall.TILE_SIZE, Wall.TILE_SIZE);
            }
        }

        if (traps != null) {
            for (Trap trap : traps) {
                if (trap.hasTexture()) trap.render(batch);
            }
        }

        // Layer C: 道具 & 钥匙 (画在地上)
        if (items != null) {
            for (Item item : items) {
                // 直接调用 Item 自己的渲染方法，它知道怎么画自己（包括 fallback 颜色）
                item.render(batch);
            }
        }

        if (keys != null) {
            for (Key k : keys) {
                if (k != null && !k.isCollected()) {
                    k.render(batch);
                }
            }
        }

        // Layer D: 门 (如果有贴图)
        if (door != null) door.render(batch);

        // Layer E: 生物 (敌人 和 玩家) -> 画在最上面
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        if (player != null) {
            player.render(batch);

            if (player.getStats() != null) {
                SkillManager sm = player.getStats().getSkillManager();
                // 同样的判断逻辑，但是做的事情不一样
                if (sm != null && "heal".equals(sm.getCurrentSkillEffect()) && sm.getSkillEffectTimer() > 0) {

                    if (healTexture != null) {
                        // 计算透明度
                        float alpha = MathUtils.clamp(sm.getSkillEffectTimer() / 0.5f, 0f, 1f);
                        batch.setColor(1f, 1f, 1f, alpha);

                        // 开启发光混合模式 (可选)
                        batch.setBlendFunction(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE);

                        // 计算位置 (飘在头顶)
                        float floatOffset = (0.5f - sm.getSkillEffectTimer()) * 40f;
                        float drawX = player.getPosition().x + player.getHitbox().width / 2 - 32; // 假设图片宽64，偏移32居中
                        float drawY = player.getPosition().y + player.getHitbox().height / 2 - 32 + floatOffset;

                        // 画图片！
                        batch.draw(healTexture, drawX, drawY, 64, 64);

                        // 还原设置
                        batch.setBlendFunction(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
                        batch.setColor(Color.WHITE);
                    }
                }
            }



        }

        if (projectiles != null) {
            for (Projectile p : projectiles) {
                // 只有当火球处于激活状态，且图片存在时才画
                if (p.isActive() && fireballTexture != null) {
                    batch.draw(fireballTexture,
                            p.getPosition().x - 16, // 修正坐标，让图片居中
                            p.getPosition().y - 16,
                            32, 32);                // 强制设置大小为 32x32
                }
            }
        }

        batch.end();


        Gdx.gl.glEnable(Gdx.gl.GL_BLEND); // 开启透明度混合
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // 实心模式



        // 2. 画火球/闪电 (如果技能正在生效)
        if (player != null && player.getStats() != null) {
            SkillManager sm = player.getStats().getSkillManager();
            if (sm != null && sm.getSkillEffectTimer() > 0) {
                Vector2 pos = sm.getSkillEffectPosition(); // 技能释放位置
                String effect = sm.getCurrentSkillEffect();

//                if ("fireball".equals(effect)) {
//                    shapeRenderer.setColor(1f, 0.2f, 0f, 0.8f); // 橙红色火球
//                    shapeRenderer.circle(pos.x, pos.y, 20); // 画个圆
//                }
//                else if ("lightning".equals(effect)) {
//                    shapeRenderer.setColor(0.2f, 0.8f, 1f, 0.8f); // 蓝白色闪电
//                    // 画个十字代表闪电
//                    shapeRenderer.rect(pos.x - 40, pos.y - 5, 80, 10);
//                    shapeRenderer.rect(pos.x - 5, pos.y - 40, 10, 80);
//                }
//                else if ("heal".equals(effect)) {
//                    shapeRenderer.setColor(0f, 1f, 0f, 0.5f); // 绿色治疗光环
//                    shapeRenderer.circle(pos.x, pos.y, 30);
//                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND); // 关闭混合


        // UI
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        drawHUD(batch);
        batch.end();

        // --- 5. 暂停遮罩 ---
        if (currentState == GameState.PAUSED) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.5f);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);


        }

        uiStage.act(delta);
        uiStage.draw();


    }


    private void winGame() {
        // 1. 防止重复触发
        if (gameWon) return;
        gameWon = true;

        int levelBonus = 1000 + (int) player.getHealth() * 5 - (int) gameTime;
        if (levelBonus < 0) levelBonus = 0;
        game.globalScore += levelBonus;

        if (mapMusic != null) mapMusic.stop();

        PlayerStats currentStats = player.getStats();


        game.setScreen(new ResultScreen(game, true, levelNumber, game.globalScore, currentStats,achievementManager));
    }

    private void handleSkillInput() {
        if (player == null || player.getStats() == null) return;

        if (currentState == GameState.RUNNING && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            // 保存当前游戏状态
            if (player != null && player.getStats() != null) {
                game.setScreen(new SkillTreeScreen(game, player.getStats(), this));
                currentState = GameState.PAUSED; // 暂停游戏逻辑
            }
        }

        // 按T打开技能树
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (player != null && player.getStats() != null) {
                game.setScreen(new SkillTreeScreen(game, player.getStats(),this));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            useSkill1();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            useSkill2();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            useSkill3();
        }

        // Shift冲刺（需要持续检测）
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            if (player.getStats().getSkillManager() != null &&
                    player.getStats().getSkillManager().canDash()) {
                float dirX = 0, dirY = 0;
                if (controller.up) dirY = 1;
                if (controller.down) dirY = -1;
                if (controller.left) dirX = -1;
                if (controller.right) dirX = 1;
                if (dirX != 0 || dirY != 0) {
                    player.getStats().getSkillManager().performDash(dirX, dirY);
                }
            }
        }
    }


    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            // 敌人移动和AI更新
            enemy.update(delta, walls);

            // 寻路逻辑
            if (player != null && pathFinder != null) {
                float distance = enemy.getPosition().dst(player.getPosition());
                if (distance <= enemy.getDetectionRange()) {
                    enemy.findPathTo(player.getPosition());
                    if (distance <= enemy.getAttackRange() && spawnInvulnTimer <= 0f) {
                        enemy.attack(player);
                    }
                } else {
                    enemy.clearPath();
                }
            }
        }

        resolveEnemyOverlaps(delta);

        for (int i = enemies.size - 1; i >= 0; i--) {
            if (!enemies.get(i).isAlive()) { // 如果怪物死了

                // 1. 只有玩家存在且数据存在时，才加经验
                if (player != null && player.getStats() != null) {
                    // 获取怪物名字 (例如 "NineTailedFox")
                    String enemyType = enemies.get(i).getClass().getSimpleName();

                    // 调用加经验的方法
                    player.getStats().gainExpFromKill(enemyType);

                    // 记录击杀成就
                    if (achievementManager != null) {
                        achievementManager.trackKill();
                    }

                    // 控制台打印，确保代码运行了
                    System.out.println("killed " + enemyType + ", gained XP!");
                }

                // 2. 从游戏中移除怪物
                enemies.removeIndex(i);
            }
        }
    }


    private void checkCollisions() {
        if (player == null) return;

        if (door != null && player.getHitbox().overlaps(door.getBounds())) {

            if (player.getStats().hasKey()) {
                door.tryOpen(player); // 让门变成开启状态（如果有动画的话）
                System.out.println("Door opened! Level Completed!");

                winGame();
            } else {
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
                if (attackSound != null) attackSound.play();
                player.triggerDamageVFX();

            }
        }

        // 在碰撞检测部分
        for (Enemy enemy : enemies) {
            if (enemy instanceof NineTailedFox && enemy.isAlive()) {
                NineTailedFox fox = (NineTailedFox) enemy;
                System.out.println("Fox state: Form=" + fox.getCurrentForm() +
                        ", HasEncountered=" + fox.hasEncounteredPlayer());
            }
        }

        if (player.getHealth() <= 0) {
            if (mapMusic != null) {
                mapMusic.stop();
            }
            HighScoreManager.saveScore(game.globalScore);
            game.resetGlobalScore();
            game.setScreen(new ResultScreen(game, false, levelNumber, 0,null,achievementManager));
        }
    }


    private void buildWalkableGrid() {
        // 从地图数据构建可行走网格
        // 假设地图尺寸为 20x20 个瓦片
        int gridWidth = (int) (mapPixelWidth / Wall.TILE_SIZE);
        int gridHeight = (int) (mapPixelHeight / Wall.TILE_SIZE);


        walkableGrid = new boolean[gridWidth][gridHeight];

        // 初始化所有格子为可行走
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                walkableGrid[x][y] = true;
            }
        }

        // 将墙壁位置标记为不可行走
        for (Wall wall : walls) {
            int gridX = (int) (wall.worldX / Wall.TILE_SIZE);
            int gridY = (int) (wall.worldY / Wall.TILE_SIZE);

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
        if (player != null && controller != null) {
            boolean up = controller.up;
            boolean down = controller.down;
            boolean left = controller.left;
            boolean right = controller.right;
            boolean run = controller.run;

            player.update(delta, up, down, left, right, run, walls);

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

    private void drawXianxiaHealthBar(SpriteBatch batch, float x, float y, float totalWidth) {
        if (texHpFrame == null || texHpBar == null || texTaiji == null || player == null) return;


        float frameHeight = 50;
        float frameWidth = totalWidth;

        float barOffsetX = 14f;
        float barOffsetY = 18f;
        float maxBarWidth = frameWidth - 14f-25f;
        float barHeight = 14f;

        // 太极图的大小和位置偏移
        float taijiSize = 54f;
        float taijiOffsetX = -45f;
        float taijiOffsetY = -2f;

        batch.setColor(Color.WHITE);
        batch.draw(texHpFrame, x, y, frameWidth, frameHeight);

        float hpPercent = player.getHealth() / player.getMaxHealth();
        hpPercent = MathUtils.clamp(hpPercent, 0f, 1f);

        if (hpPercent > 0) {
            float currentBarWidth = maxBarWidth * hpPercent;


            batch.draw(texHpBar,
                    x + barOffsetX,
                    y + barOffsetY,
                    currentBarWidth,
                    barHeight
            );
        }

        float rotation = -gameTime * 80f; // 逆时针旋转

        batch.draw(texTaiji,
                x + taijiOffsetX, y + taijiOffsetY,    // 绘制坐标 X, Y
                taijiSize / 2f, taijiSize / 2f,        // 旋转中心点 (OriginX, OriginY)
                taijiSize, taijiSize,                  // 绘制宽高 (Width, Height)
                1f, 1f,                                // 缩放 (ScaleX, ScaleY)
                rotation,                              // 旋转角度 (Rotation)
                0, 0,                                  // 源图片起始点 (SrcX, SrcY)
                texTaiji.getWidth(), texTaiji.getHeight(), // 源图片宽高 (SrcWidth, SrcHeight)
                false, false                           // 是否翻转 (FlipX, FlipY)
        );

        String hpText = (int)player.getHealth() + " / " + (int)player.getMaxHealth();
        font.getData().setScale(0.8f);
        drawShadowText(batch, hpText, x + frameWidth / 2f + 20, y + frameHeight / 2f - 5);
        font.getData().setScale(1f);
    }

    private void drawShadowText(SpriteBatch batch, String text, float x, float y) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(Color.WHITE);
        font.draw(batch, text, x, y);
    }








    private void drawHUD(SpriteBatch batch) {
        // 初始状态：白色，用于绘制图片原色
        batch.setColor(Color.WHITE);

        float uiH = uiStage.getViewport().getWorldHeight();
        float uiW = uiStage.getViewport().getWorldWidth();

        // 1. 血条 (位置不变)
        drawXianxiaHealthBar(batch, 60, uiH - 70, 300);

        if (keyIconTexture != null && player != null && player.getStats() != null) {
            float keyIconSize = 48;
            float startX = 75;        // X坐标：与血条左对齐稍微偏移
            float startY = uiH - 130; // Y坐标：在血条下方 (血条约在 uiH-70)
            float gap = 60;           // 图标间距

            int bonusKeys = player.getStats().getBonusKey();
            boolean hasExitKey = player.getStats().hasKey();

            // 循环绘制 3 把钥匙
            for (int i = 0; i < 3; i++) {
                if (i < 2) {
                    // 前两把是 Bonus Key
                    if (i < bonusKeys) {
                        batch.setColor(Color.GREEN); // 已收集：绿色
                    } else {
                        batch.setColor(Color.DARK_GRAY); // 未收集：深灰色
                    }
                } else {
                    // 第三把是 Exit Key
                    if (hasExitKey) {
                        batch.setColor(Color.RED); // 已收集：红色
                    } else {
                        batch.setColor(Color.DARK_GRAY); // 未收集：深灰色
                    }
                }
                // 绘制图标
                batch.draw(keyIconTexture, startX + (i * gap), startY, keyIconSize, keyIconSize);
            }
            // 重置颜色，防止影响后续绘制
            batch.setColor(Color.WHITE);
        }


        // ============================================================
        // 2. 右上角信息框 (XP 显示)
        // ============================================================
        float frame1W = 260;
        float frame1H = 200;
        float frame1X = uiW - frame1W + 10;
        float frame1Y = uiH - frame1H + 10;

        drawHUDFrame(batch, frame1X, frame1Y, frame1W, frame1H);

        float textX = frame1X + 55;
        float textY = uiH - 55;
        float lineGap = 18;

        font.setColor(0.25f, 0.25f, 0.25f, 1f); // 深灰色
        font.getData().setScale(0.75f);

        font.draw(batch, "LEVEL " + levelNumber, textX, textY);
        textY -= lineGap;
        font.draw(batch, "TIME: " + (int) gameTime + "s", textX, textY);
        textY -= lineGap;

        if (player != null && player.getStats() != null) {
            // 显示当前 XP
            int currentXP = player.getStats().getExpSystem().getCurrentExp();
            font.draw(batch, "SOULS(XP): " + currentXP, textX, textY);
            textY -= lineGap;
            // 如果想显示总获得 XP，也可以用 getTotalExp()
        }

        // ============================================================
        // 3. 左下角控制说明框 (核心修改：动态变色技能显示)
        // ============================================================
        float frame2W = 320; // 稍微宽一点，防止文字超出
        float frame2H = 230;
        float frame2X = -10;
        float frame2Y = -10;

        batch.setColor(Color.WHITE); // 确保框是白的
        drawHUDFrame(batch, frame2X, frame2Y, frame2W, frame2H);

        textX = frame2X + 65;
        textY = (frame2Y + frame2H) - 85;

        font.setColor(0.25f, 0.25f, 0.25f, 1f); // 标题深灰色
        font.getData().setScale(0.8f);
        font.draw(batch, "--- CONTROLS ---", textX, textY);
        textY -= lineGap;

        font.getData().setScale(0.7f);

        // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ 核心逻辑修改开始 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
        if (player != null && player.getStats() != null) {
            SkillTree skillTree = player.getStats().getSkillTree();

            if (skillTree != null) {
                // --- Q 技能 (Fireball) ---
                if (skillTree.hasQSkill()) {
                    if (skillTree.getQCooldown() > 0) {
                        font.setColor(Color.ORANGE); // 冷却中显示橙色/黄色
                        font.draw(batch, String.format("Q: Fireball (%.1fs)", skillTree.getQCooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f); // 深绿色 (Ready)
                        font.draw(batch, "Q: Fireball (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY); // 没解锁显示灰色
                    font.draw(batch, "Q: Locked (Req. 150 XP)", textX, textY);
                }
                textY -= lineGap;

                // --- E 技能 (Heal) ---
                if (skillTree.hasESkill()) {
                    if (skillTree.getECooldown() > 0) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, String.format("E: Heal (%.1fs)", skillTree.getECooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f); // 深绿色
                        font.draw(batch, "E: Heal (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "E: Locked (Req. 120 XP)", textX, textY);
                }
                textY -= lineGap;

                // --- R 技能 (Lightning) ---
                if (skillTree.hasRSkill()) {
                    if (skillTree.getRCooldown() > 0) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, String.format("R: Lightning (%.1fs)", skillTree.getRCooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f); // 深绿色
                        font.draw(batch, "R: Lightning (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "R: Locked (Req. 200 XP)", textX, textY);
                }
                textY -= lineGap;

                // --- 其他按键 (恢复深灰色) ---
                font.setColor(0.25f, 0.25f, 0.25f, 1f);
                font.draw(batch, "SPACE - Attack", textX, textY);
                textY -= lineGap;

                if (skillTree.hasDash()) {
                    font.draw(batch, "SHIFT - Dash", textX, textY);
                    textY -= lineGap;
                }
            }
        }
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ 核心逻辑修改结束 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        // ============================================================
        // 4. 罗盘 (保持不变)
        // ============================================================
        // === 4. 罗盘 (修正版) ===
        // === 4. 罗盘 (逻辑与比例修正版) ===
        if (exitPosition != null && arrowRegion != null && player != null) {
            batch.setColor(Color.WHITE);

            // 1. 计算目标点像素坐标
            float tx = exitPosition.x * Wall.TILE_SIZE;
            float ty = exitPosition.y * Wall.TILE_SIZE;

            // 2. 计算角度
            float dx = tx - player.getPosition().x;
            float dy = ty - player.getPosition().y;
            float currentAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;

            // 3. 尺寸与中心
            float imgW = arrowRegion.getRegionWidth();
            float imgH = arrowRegion.getRegionHeight();
            float originX = imgW / 2f;
            float originY = imgH / 2f;

            // 4. 缩放比例
            float uiScale = 0.2f;

            // 5. 修正反向问题：使用 + 90f
            float finalRotation = currentAngle + 90f;

            // 6. 绘制坐标
            float drawX = uiW - 100;
            float drawY = 100;

            batch.draw(arrowRegion,
                    drawX - originX, drawY - originY,
                    originX, originY,
                    imgW, imgH,
                    uiScale, uiScale,
                    finalRotation
            );

            // 7. 文字标注 (略微下移，避免遮挡箭头)
            font.getData().setScale(0.8f);
            font.setColor(Color.WHITE);
            // 使用 drawX 确保文字相对于箭头居中
            font.draw(batch, "EXIT", drawX - 20, drawY - 45);
        }
    }

    // 新增：绘制 HUD 背景框的辅助方法
    private void drawHUDFrame(SpriteBatch batch, float x, float y, float width, float height) {
        if (hudFrameTexture == null) return;
        batch.draw(hudFrameTexture, x, y, width, height);
    }

    // 移除 drawSkillHUD 方法，因为它的内容已经合并到 drawHUD 中了
    // private void drawSkillHUD(SpriteBatch batch) { ... }

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

                boolean wasActivated = trap.isActivated();
                trap.checkActivation(player);
                boolean isActivatedNow = trap.isActivated();
                if (trap instanceof MechanismTrap) {
                    if (!wasActivated && isActivatedNow) {
                        if (mechanismSound != null) {
                            mechanismSound.play(1.0f);
                        }
                    }
                } else if (trap instanceof Fog) {
                    if (((Fog) trap).isPlayerInFog(player)) {

                        if (fogSoundTimer <= 0) {
                            if (fogSound != null) {
                                fogSound.play(0.6f);
                            }

                            fogSoundTimer = 4.0f;
                        }
                    }
                }
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
        if (!isInitialized) {
            // --- 1. 资源加载 (保持原样) ---
            if (Gdx.files.internal("fireball.png").exists()) {
                fireballTexture = new Texture(Gdx.files.internal("fireball.png"));
            }
            projectiles = new Array<>();
            wallTexture = new Texture(Gdx.files.internal("wall.png"));
            try {
                healTexture = new Texture(Gdx.files.internal("heal.png"));
                hudFrameTexture = new Texture(Gdx.files.internal("scroll_1.png"));
                keyIconTexture = new Texture(Gdx.files.internal("key.png"));
            } catch (Exception e) {
                Gdx.app.log("GameScreen", "Texture load error: " + e.getMessage());
            }

            // --- 2. 地图全量加载 (核心修改) ---
            MapLoader loader = new MapLoader();
            shapeRenderer = new ShapeRenderer();

            String actualPathToLoad;
            if (levelNumber > 5 || !Gdx.files.local("maps/level-" + levelNumber + ".properties").exists()) {
                actualPathToLoad = "maps/level-5.properties";
            } else {
                actualPathToLoad = "maps/level-" + levelNumber + ".properties";
            }

            // 读取地图数据
            MapLoader.LevelData data = loader.loadLevel(actualPathToLoad,this.levelNumber);

            this.walls = data.walls;
            this.entryPosition = data.entryPosition;
            this.exitPosition = data.exitPosition;

            // 🔥 提取敌人 (注意转换 List 到 Array) 🔥
            this.enemies = new Array<>();
            if (data.enemies != null) {
                for (Enemy e : data.enemies) this.enemies.add(e);
            }

            // 🔥 提取陷阱 🔥
            this.traps = new Array<>();
            if (data.traps != null) {
                for (Trap t : data.traps) this.traps.add(t);
            }

            // 🔥 提取钥匙并设置 Bonus 🔥
            this.keys = new ArrayList<>();
            if (data.keys != null) {
                this.keys.addAll(data.keys);
                for (int i = 0; i < this.keys.size(); i++) {
                    // 只有最后一个是 false (通关钥匙)，其余全是 true (Bonus)
                    this.keys.get(i).setBonus(i != this.keys.size() - 1);
                }
            }

            // --- 3. 初始化门和出口 ---
            if (this.exitPosition != null) {
                this.exit = new Exit();
                door = new Door(
                        exitPosition.x * Wall.TILE_SIZE,
                        exitPosition.y * Wall.TILE_SIZE,
                        Wall.TILE_SIZE,
                        Wall.TILE_SIZE
                );
                this.exitArea = new Rectangle(door.getX(), door.getY(), door.getWidth(), door.getHeight());
            }

            // --- 4. 构建寻路网格 ---
            int maxX = 0, maxY = 0;
            for (Wall w : walls) {
                if (w.gridX > maxX) maxX = w.gridX;
                if (w.gridY > maxY) maxY = w.gridY;
            }
            mapPixelWidth = (maxX + 1) * Wall.TILE_SIZE;
            mapPixelHeight = (maxY + 1) * Wall.TILE_SIZE;
            mapWidthInTiles = maxX + 1;
            mapHeightInTiles = maxY + 1;

            buildCollisionMap(maxX + 1, maxY + 1);
            buildWalkableGrid();
            initPathFinder();

            // --- 5. 地板贴图 ---
            floorVariants = new Texture[]{
                    new Texture(Gdx.files.internal("floor.png")),
                    new Texture(Gdx.files.internal("floor_flower.png")),
                    new Texture(Gdx.files.internal("floor_grass.png"))
            };
            floorPick = new byte[mapWidthInTiles][mapHeightInTiles];
            Random rngg = new Random(levelNumber * 99991L);
            for (int x = 0; x < mapWidthInTiles; x++) {
                for (int y = 0; y < mapHeightInTiles; y++) {
                    int idx = 0;
                    float r = rngg.nextFloat();
                    if (r < 0.06f) idx = 1; else if (r < 0.12f) idx = 2;
                    floorPick[x][y] = (byte) idx;
                }
            }

            // --- 6. 随机道具生成 (保留旧逻辑) ---
            this.items = new ArrayList<>();
            if (data.items != null && !data.items.isEmpty()) {
                // 🔥 改为从地图数据中添加
                this.items.addAll(data.items);
                System.out.println("Items loaded from map: " + items.size());
            } else {
                // 可选：如果地图没写道具，你可以保留一个保底的随机逻辑，或者干脆空着
                System.out.println("No items defined in map properties.");
            }

            // --- 7. 实体初始化 ---
            try {
                arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
                arrowRegion = new TextureRegion(arrowTexture);
            } catch (Exception e) {}

            initPlayer();
            initEnemies(); // 现在只负责配置 AI

            // --- 8. UI 和 音效 ---
            achievementManager = new AchievementManager();
            console = new DeveloperConsole(game.getSkin(), uiStage, player, this);

            // 双重保险：给所有敌人设置 Pathfinder
            for (Enemy enemy : enemies) {
                enemy.setPathFinder(pathFinder);
            }

            game.stopMenuMusic();
            try {
                if (mapMusic != null) { mapMusic.stop(); mapMusic.dispose(); mapMusic = null; }
                mapMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/mapbackground.mp3"));
                mapMusic.setLooping(true); mapMusic.setVolume(0.4f); mapMusic.play();

                pauseMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/pausegameSound.mp3"));
                pauseMusic.setLooping(true); pauseMusic.setVolume(0.3f);

                attackSound = Gdx.audio.newSound(Gdx.files.internal("Sound/attack.mp3"));
                fogSound = Gdx.audio.newSound(Gdx.files.internal("Sound/fog.mp3"));
                bonusSound = Gdx.audio.newSound(Gdx.files.internal("Sound/video-game-bonus-323603.mp3"));
                buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
                mechanismSound = Gdx.audio.newSound(Gdx.files.internal("Sound/trap1.mp3"));
                keySound = Gdx.audio.newSound(Gdx.files.internal("Sound/key-get-39925.mp3"));
                fireballSound = Gdx.audio.newSound(Gdx.files.internal("Sound/fire.wav"));
                lightningSound = Gdx.audio.newSound(Gdx.files.internal("Sound/lightning.mp3"));
                healSkillSound = Gdx.audio.newSound(Gdx.files.internal("Sound/heal.wav"));
                swingSound = Gdx.audio.newSound(Gdx.files.internal("Sound/swing.wav"));
            } catch (Exception e) { System.out.println("Sound load error: " + e.getMessage()); }

            try {
                texHpFrame = new Texture(Gdx.files.internal("HUD/hp_frame.png"));
                texHpBar = new Texture(Gdx.files.internal("HUD/hp_bar.png"));
                texTaiji = new Texture(Gdx.files.internal("HUD/taiji_gold.png"));
            } catch (Exception e) {}

            currentState = GameState.RUNNING;
            isInitialized = true;
        }

        // --- 暂停恢复逻辑 ---
        if (isInitialized && currentState == GameState.PAUSED) {
            currentState = GameState.RUNNING;
            Gdx.input.setInputProcessor(null);
            if (pauseMenuTable != null) pauseMenuTable.setVisible(false);
        } else if (currentState == GameState.PAUSED) {
            Gdx.input.setInputProcessor(uiStage);
        } else {
            Gdx.input.setInputProcessor(null);
        }

        if (mapMusic != null && !mapMusic.isPlaying() && currentState == GameState.RUNNING) {
            mapMusic.play();
        }
    }


    private void resolveEnemyOverlaps(float delta) {
        for (int i = 0; i < enemies.size; i++) {
            Enemy e1 = enemies.get(i);
            if (!e1.isAlive()) continue;

            for (int j = i + 1; j < enemies.size; j++) {
                Enemy e2 = enemies.get(j);
                if (!e2.isAlive()) continue;

                // 计算中心点
                float r1 = e1.getWidth() / 2f;
                float r2 = e2.getWidth() / 2f;
                float cx1 = e1.getX() + r1;
                float cy1 = e1.getY() + e1.getHeight() / 2f;
                float cx2 = e2.getX() + r2;
                float cy2 = e2.getY() + e2.getHeight() / 2f;

                float dst = Vector2.dst(cx1, cy1, cx2, cy2);
                float minDist = r1 + r2 - 4f; // 允许 4 像素的微小重叠，避免抖动

                if (dst < minDist) {
                    if (dst == 0) dst = 0.01f; // 防止除以0

                    float overlap = minDist - dst;
                    // 推力稍微小一点，让它们慢慢分开，不要瞬间弹飞
                    float pushForce = overlap * 0.5f;

                    float pushX = (cx1 - cx2) / dst * pushForce;
                    float pushY = (cy1 - cy2) / dst * pushForce;

                    // 尝试推 e1
                    float nextX1 = e1.getX() + pushX;
                    float nextY1 = e1.getY() + pushY;
                    if (!collidesWithAnyWall(new Rectangle(nextX1, nextY1, e1.getWidth(), e1.getHeight()))) {
                        e1.setPosition(nextX1, nextY1);
                    }

                    // 尝试推 e2 (反方向)
                    float nextX2 = e2.getX() - pushX;
                    float nextY2 = e2.getY() - pushY;
                    if (!collidesWithAnyWall(new Rectangle(nextX2, nextY2, e2.getWidth(), e2.getHeight()))) {
                        e2.setPosition(nextX2, nextY2);
                    }
                }
            }
        }
    }



    // =========================================================
    // 🔥 修改后：适配简单写法的敌人生成逻辑
    // =========================================================
    // 修改后的 initEnemies：不再生成，只负责配置
    private void initEnemies() {
        System.out.println("Configuring " + enemies.size + " enemies loaded from map...");

        for (Enemy enemy : enemies) {
            // 1. 设置难度
            enemy.adjustDifficulty(this.levelNumber);

            enemy.setMapLimits(mapPixelWidth, mapPixelHeight);

            // 2. 赋予 AI 寻路能力
            enemy.setWalkableGrid(walkableGrid);
            enemy.setPathFinder(pathFinder);
            enemy.setTargetPlayer(player);

            // 3. 特殊怪物的特殊设置
            if (enemy instanceof ZhuLong) {
                if (player != null) {
                    ((ZhuLong) enemy).setTargetPosition(player.getPosition());
                }
            }
            if (enemy instanceof NineTailedFox) {
                ((NineTailedFox) enemy).resetEncounterState();
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
        } else if (exitPosition != null) {

            spawnX = exitPosition.x * Wall.TILE_SIZE;
            spawnY = exitPosition.y * Wall.TILE_SIZE;
            System.out.println("Spawned at Map Exit (Type 2): " + spawnX + "," + spawnY);
        } else {
            spawnX = 50;
            spawnY = 50;
            System.out.println("No Entry/Exit found, using default: 50,50");
        }

        player = new Player(spawnX, spawnY,previousStats);

        previousStats = null;

        if (player.getStats() != null) {
            SkillManager skillManager = player.getStats().getSkillManager();
            if (skillManager != null) {
                skillManager.setGameScreen(this);  // 设置 GameScreen 引用
            }
        }

        spawnInvulnTimer = SPAWN_INVULN_DURATION;

        if (player.getStats() != null) {
            player.getStats().heal(100);
        }
        player.syncPositionToHitbox();

    }


    @Override
    public void hide() {
    }

    private void useSkill1() {
        // 1. 基础检查
        if (player == null || player.getStats() == null) return;

        SkillManager skillManager = player.getStats().getSkillManager();
        if (skillManager == null) return;

        // 2. 检查是否解锁 (XP >= 150)
        if (!skillManager.hasQSkill()) {
            System.out.println("Q Skill not unlocked yet! (Need 150 Total XP)");
            return;
        }

        // 3. 检查冷却
        if (skillManager.canUseQSkill()) {
            // 触发冷却
            skillManager.useSkill("Q");

            // 播放音效
            if (fireballSound != null) fireballSound.play(0.7f);

            // --- 核心发射逻辑 ---
            if (projectiles != null) {
                float dmg = skillManager.calculateFireballDamage(30f);

                // A. 寻找最近敌人 (自动瞄准)
                Enemy target = null;
                float minDst = Float.MAX_VALUE;
                for (Enemy e : enemies) {
                    if (!e.isAlive()) continue; // 忽略死人
                    float dst = player.getPosition().dst(e.getPosition());
                    if (dst < minDst && dst < 400f) { // 搜索范围扩大到 400
                        minDst = dst;
                        target = e;
                    }
                }

                // B. 决定飞行方向
                Vector2 direction;
                if (target != null) {
                    // 有敌人：朝敌人飞
                    direction = new Vector2(target.getPosition()).sub(player.getPosition()).nor();
                    System.out.println("Fireball aiming at enemy!");
                } else {
                    // 没敌人：默认向右飞 (1, 0)
                    // 或者你可以改成 player.getDirection() 如果你有这个变量
                    direction = new Vector2(1, 0);
                    System.out.println("Fireball shooting blindly (Right)");
                }

                // C. 生成火球
                // 注意：这里我们让火球从玩家中心稍微偏移一点，避免看起来像从脚底发出来的
                float offsetX = player.getHitbox().width / 2f;
                float offsetY = player.getHitbox().height / 2f;
                Projectile fireball = new Projectile(
                        player.getPosition().x,
                        player.getPosition().y,
                        direction.x, direction.y,
                        300f, // 速度
                        dmg,
                        Color.ORANGE
                );

                projectiles.add(fireball);
                System.out.println(">>> Fireball CREATED and ADDED to list!");
            }
        } else {
            System.out.println("Q Skill is on Cooldown...");
        }
    }

    private void useSkill2() {
        if (player != null && player.getStats() != null) {
            SkillManager skillManager = player.getStats().getSkillManager();

            if (skillManager != null && skillManager.hasESkill()) {
                if (skillManager.useSkill("E")) {
                    if (healSkillSound != null) healSkillSound.play(0.8f);
                    System.out.println(" E Skill - Healing cast successfully!");
                    // Add visual feedback here if needed
                } else {
                    float cooldown = skillManager.getECooldown();
                    if (cooldown > 0) {
                        System.out.println(" E Skill cooling down: " + String.format("%.1f", cooldown) + "s");
                    } else {
                        System.out.println(" E Skill not available");
                    }
                }
            } else {
                System.out.println(" E Skill not unlocked. Press T to open Skill Tree");
            }
        }
    }

    private void useSkill3() {
        if (player != null && player.getStats() != null) {
            SkillManager skillManager = player.getStats().getSkillManager();

            if (skillManager != null && skillManager.hasRSkill()) {
                if (skillManager.useSkill("R")) {
                    if (lightningSound != null) lightningSound.play(1.0f);
                    System.out.println(" R Skill - Lightning cast successfully!");
                    // Add visual feedback here if needed
                } else {
                    float cooldown = skillManager.getRCooldown();
                    if (cooldown > 0) {
                        System.out.println(" R Skill cooling down: " + String.format("%.1f", cooldown) + "s");
                    } else {
                        System.out.println(" R Skill not available");
                    }
                }
            } else {
                System.out.println(" R Skill not unlocked. Press T to open Skill Tree");
            }
        }
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


        if (door != null) {
            door.dispose();
        }

        if (keys != null) {
            for (Key k : keys) {
                k.dispose();
            }
        }


        if (shapeRenderer != null) shapeRenderer.dispose();

        // ========== 清理UI ==========
        if (uiStage != null) {
            uiStage.dispose();
        }


        if (mapMusic != null) mapMusic.dispose();
        if (pauseMusic != null) pauseMusic.dispose();
        if (attackSound != null) attackSound.dispose();
        if (fogSound != null) fogSound.dispose();
        if (bonusSound != null) bonusSound.dispose();
        if (buttonSound != null) buttonSound.dispose();
        if (keySound != null) keySound.dispose();
        if (mechanismSound != null) mechanismSound.dispose();


        if (texHpFrame != null) texHpFrame.dispose();
        if (texHpBar != null) texHpBar.dispose();
        if (texTaiji != null) texTaiji.dispose();

        // 新增：清理按钮和 HUD 背景资源
        if (buttonBg != null) buttonBg.dispose();
        if (hudFrameTexture != null) hudFrameTexture.dispose();

        if (fireballTexture != null) fireballTexture.dispose();
        if (healTexture != null) healTexture.dispose();

        if (arrowTexture != null) arrowTexture.dispose();
        if (hudFrameTexture != null) hudFrameTexture.dispose();
        if (buttonBg != null) buttonBg.dispose();

        if (fireballSound != null) fireballSound.dispose();
        if (lightningSound != null) lightningSound.dispose();
        if (healSkillSound != null) healSkillSound.dispose();
        if (swingSound != null) swingSound.dispose();


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

    public List<Enemy> getEnemiesList() {
        List<Enemy> enemyList = new ArrayList<>();
        for (int i = 0; i < enemies.size; i++) {
            enemyList.add(enemies.get(i));
        }
        return enemyList;
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



    private Vector2 getRandomEmptyTile() {
        // 1. 如果碰撞地图还没生成，暂时没法找，返回 null
        if (collisionMap == null) return null;

        List<Vector2> validTiles = new ArrayList<>();

        int h = collisionMap.length;
        int w = collisionMap[0].length;

        // 2. 遍历整个地图，只收集 value 为 0 (空地) 的格子
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // collisionMap[y][x] == 0 代表是路，1 代表是墙
                if (collisionMap[y][x] == 0) {
                    validTiles.add(new Vector2(x, y));
                }
            }
        }

        // 3. 如果有空地，随机挑一个
        if (!validTiles.isEmpty()) {
            Random random = new Random();
            return validTiles.get(random.nextInt(validTiles.size()));
        }

        // 4. 如果整个地图全是墙（不可能发生），返回 null，绝对不要返回 (1,1)！
        return null;
    }


    // ==========================================
    // 🔥 新增：专门用来检测钥匙拾取的方法
    // ==========================================
    private void updateKeys() {
        if (keys == null || player == null) return;

        for (Key k : keys) {
            // 如果钥匙还在（没被捡走）
            if (k != null && !k.isCollected()) {
                // 检测碰撞
                if (player.getHitbox().overlaps(k.getBounds())) {

                    // 播放音效
                    if (keySound != null) keySound.play(1.0f);

                    // 执行捡钥匙逻辑 (Key 类里应该有这个方法)
                    k.checkPickup(player);

                    System.out.println("Key collected!");
                }
            }
        }
    }








    private void checkPlayerAttackHit() {
        // 如果玩家没有在攻击，直接返回
        if (player == null || !player.isAttacking()) return;

        // 获取玩家的攻击判定框 (看不见的剑气)
        Rectangle attackBox = player.getAttackHitbox();

        // 获取玩家当前的攻击力 (基础 + 技能加成)
        float damage = player.getStats().getActualAttackDamage();

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && attackBox.overlaps(enemy.getBounds())) {
                // 敌人扣血 (Enemy 类里需要确保有 takeDamage 方法)
                enemy.takeDamage(damage);
                achievementManager.trackKill();

                // 播放命中音效 (复用 attackSound 或者你可以加一个新的 hitSound)
                // if (attackSound != null) attackSound.play(0.3f);
            }
        }
    }

    
    private void updateItems() {
        if (items == null || player == null) return;

        // 倒序遍历，因为要执行删除操作
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            // 检测玩家是否碰到道具
            if (player.getHitbox().overlaps(item.getBounds())) {
                item.onPickup(player);

                // 播放捡东西音效
                if (bonusSound != null) bonusSound.play();

                // 从列表中移除已被捡起的道具
                items.remove(i);
                System.out.println("Item collected!");
            }
        }
    }



    // ==========================================
    // 🔥 优化后的技能伤害判定逻辑
    // ==========================================
    private void checkSkillCollisions(float delta) {
        if (player == null || player.getStats() == null) return;
        SkillManager sm = player.getStats().getSkillManager();

        // 1. 基础检查：没有特效、或者特效已结束，直接返回
        if (sm == null || sm.getSkillEffectTimer() <= 0) return;

        // 2. 核心检查：如果这个技能已经造成过伤害了，不再重复判定
        // (这一步防止了每帧扣血的 BUG)
        if (sm.hasDealtDamage()) return;

        // 获取技能信息
        String currentSkill = sm.getCurrentSkillEffect();
        Vector2 skillPos = sm.getSkillEffectPosition(); // 技能释放时的位置

        float damage = 0f;
        float range = 0f;
        boolean isAreaEffect = false; // 是否是群体伤害

        // 3. 根据技能类型设定伤害和范围 (数值已根据之前的平衡调整)
        if ("fireball".equals(currentSkill)) {
            // 火球：基础40 + 50%攻击力加成
            damage = 40f + player.getStats().getActualAttackDamage() * 0.5f;
            range = 40f; // 判定半径 (稍微加大一点，更容易打中)
            isAreaEffect = false; // 单体
        }
        else if ("lightning".equals(currentSkill)) {
            // 闪电：基础25 + 30%攻击力加成
            damage = 25f + player.getStats().getActualAttackDamage() * 0.3f;
            range = 120f; // 大范围 AOE
            isAreaEffect = true; // 群体
        }
        else {
            return; // 治疗术(heal)不需要检测敌人，护盾(shield)也不需要
        }

        boolean hitAnyone = false;

        // 4. 遍历所有敌人
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            // --- 距离计算优化 ---
            // 获取敌人中心点，而不是左下角 (x, y)
            float enemyCenterX = enemy.getX() + enemy.getWidth() / 2f;
            float enemyCenterY = enemy.getY() + enemy.getHeight() / 2f;

            // 计算技能中心到敌人中心的距离
            float dist = Vector2.dst(skillPos.x, skillPos.y, enemyCenterX, enemyCenterY);

            // 如果在范围内
            if (dist <= range) {
                // 造成伤害！
                enemy.takeDamage(damage);

                // 如果打死了，记录击杀
                if (!enemy.isAlive()) {
                    achievementManager.trackKill();
                }

                hitAnyone = true;

                System.out.println("Skill [" + currentSkill + "] hit enemy for " + (int)damage + " dmg!");

                // 如果不是群体伤害（火球），打中第一个人就停止循环
                if (!isAreaEffect) {
                    break;
                }
            }
        }

        // 5. 命中后处理
        if (hitAnyone) {
            // 标记该次技能已造成伤害，后续帧不再判定
            sm.setHasDealtDamage(true);

            // 播放命中音效 (如果有的话)
            // if (attackSound != null) attackSound.play(0.5f);
        }
    }








}