package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation; // ✅ 必须导入
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.items.Key;
import de.tum.cit.fop.maze.traps.Trap;
import de.tum.cit.fop.maze.traps.Fog;
import de.tum.cit.fop.maze.traps.MechanismTrap;
import de.tum.cit.fop.maze.progression.SkillTreeScreen;
import de.tum.cit.fop.maze.progression.SkillTree;
import de.tum.cit.fop.maze.progression.SkillManager;

import java.util.Random;
import java.util.ArrayList;
import de.tum.cit.fop.maze.items.Item;

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

    // ==== 新增：路径寻找和陷阱系统 ====
    private AStarPathFinder pathFinder;
    private Array<Trap> traps;
    private int[][] collisionMap;
    private Rectangle exitArea;

    // ==== 新添加的敌人相关变量 ====
    private Array<Enemy> enemies;
    private Player player;
    private boolean[][] walkableGrid;

    private GameState currentState;
    private Stage uiStage;
    private Table pauseMenuTable;

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

    private float spawnInvulnTimer = 0f;
    private static final float SPAWN_INVULN_DURATION = 1.0f;
    private SettingsManager settingsManager;
    private PlayerStats previousStats;

    private Texture floorTexture;
    private Texture wallTexture;

    // 🔥 修改：把 Texture 改为 Animation
    private Texture fireballTexture; // 保留原始Texture用于切割
    private Animation<TextureRegion> fireballAnimation; // 🔥 新增动画对象

    private Texture lightningTexture;
    private Array<Projectile> projectiles;

    private Music mapMusic;
    private Music pauseMusic;

    private com.badlogic.gdx.audio.Sound attackSound;
    private com.badlogic.gdx.audio.Sound fogSound;
    private com.badlogic.gdx.audio.Sound bonusSound;
    private com.badlogic.gdx.audio.Sound buttonSound;
    private com.badlogic.gdx.audio.Sound mechanismSound;
    private com.badlogic.gdx.audio.Sound keySound;
    private com.badlogic.gdx.audio.Sound fireballSound;
    private com.badlogic.gdx.audio.Sound lightningSound;
    private com.badlogic.gdx.audio.Sound healSkillSound;
    private com.badlogic.gdx.audio.Sound swingSound;
    private float fogSoundTimer = 0f;

    private Texture texHpFrame;
    private Texture texHpBar;
    private Texture texTaiji;
    private List<Item> items;
    private Texture healTexture;

    private DeveloperConsole console;
    private AchievementManager achievementManager;
    private Texture[] floorVariants;
    private byte[][] floorPick;

    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;
    private Texture keyIconTexture;
    private Texture hudFrameTexture;
    private boolean isInitialized = false;

    public GameScreen(MazeRunnerGame game) {
        this(game, 1,null);
        // 不要在构造函数里加载 Texture，移到 show() 里统一加载更安全
    }

    public GameScreen(MazeRunnerGame game, int levelNumber, PlayerStats prevStats) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.currentMapPath = "maps/level-" + levelNumber + ".properties";
        this.previousStats = prevStats;

        if (this.previousStats != null) {
            this.previousStats.setBonusKey(0);
            this.previousStats.setHasKey(false);
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

        controller = new InputController(settingsManager);
        enemies = new Array<>();
        uiStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        currentState = GameState.RUNNING;

        createButtonStyle();
        createPauseMenu();
    }

    public GameScreen(MazeRunnerGame game, String mapFilePath) {
        this.game = game;
        this.levelNumber = 0;
        this.currentMapPath = mapFilePath;
        initCommon();
    }

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
            Gdx.app.log("GameScreen", "Button texture error: " + e.getMessage());
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    private void createPauseMenu() {
        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();
        pauseMenuTable.setVisible(false);

        Label pauseLable = new Label("Game PAUSED", game.getSkin(), "title");
        pauseMenuTable.add(pauseLable).padBottom(40).row();

        TextButton resumeButton = new TextButton("Resume", commonButtonStyle);
        resumeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                togglePause();
            }
        });
        pauseMenuTable.add(resumeButton).width(350).height(80).padBottom(15).row();

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
        pauseMenuTable.add(musicButton).width(350).height(80).padBottom(15).row();

        TextButton quitButton = new TextButton("Exit to Menu", commonButtonStyle);
        quitButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                if (pauseMusic != null) pauseMusic.stop();
                if (mapMusic != null) mapMusic.stop();
                game.playMenuMusic();
                game.goToMenu();
            }
        });
        pauseMenuTable.add(quitButton).width(350).height(80).row();

        uiStage.addActor(pauseMenuTable);
    }

    private void togglePause() {
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

    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            console.toggleConsole();
            if (console.isVisible()) {
                Gdx.input.setInputProcessor(uiStage);
            } else {
                if (currentState == GameState.PAUSED) {
                    Gdx.input.setInputProcessor(uiStage);
                } else {
                    Gdx.input.setInputProcessor(null);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        if (currentState == GameState.RUNNING && !console.isVisible()) {
            controller.update();
            handleSkillInput();

            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) useSkill1();
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) useSkill2();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) useSkill3();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (player != null) {
                    player.performAttack();
                    if (swingSound != null) swingSound.play(0.5f);
                }
            }

            if (controller.zoomChange != 0) {
                camera.zoom += controller.zoomChange;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 2.0f);
            }

            gameTime += delta;
            if (spawnInvulnTimer > 0f) spawnInvulnTimer -= delta;
            if (fogSoundTimer > 0) fogSoundTimer -= delta;

            updateTraps(delta);
            updateEnemies(delta);
            updatePlayer(delta);

            if (projectiles != null) {
                for (int i = projectiles.size - 1; i >= 0; i--) {
                    Projectile p = projectiles.get(i);
                    p.update(delta);

                    if (p.checkEnemyHit(enemies)) {
                        if (p.getBounds().width < 40) {
                            projectiles.removeIndex(i);
                            continue;
                        }
                        else {
                            System.out.println("Lightning hit, but persists!");
                        }
                    }

                    if (p.getBounds().width < 40 && !p.isActive()) {
                        projectiles.removeIndex(i);
                    }
                    else if (p.getBounds().width >= 40 && p.getPosition().y < player.getPosition().y - 400) {
                        projectiles.removeIndex(i);
                    }
                }
            }

            checkCollisions();
            checkTrapActivation();
            checkPlayerAttackHit();
            checkSkillCollisions(delta);
            updateKeys();
            updateItems();

            SkillManager skillManager = null;
            if (player != null && player.getStats() != null) {
                skillManager = player.getStats().getSkillManager();
                if (skillManager != null) {
                    skillManager.update(delta);
                }
            }

            String skillEffect = "";
            if (skillManager != null) {
                skillEffect = skillManager.getCurrentSkillEffect();
            }



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

        ScreenUtils.clear(0, 0, 0, 1);
        SpriteBatch batch = game.getSpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                Texture tex = floorVariants[floorPick[x][y]];
                batch.draw(tex, x * Wall.TILE_SIZE, y * Wall.TILE_SIZE, Wall.TILE_SIZE, Wall.TILE_SIZE);
            }
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (door != null) {
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(door.getX(), door.getY(), door.getWidth(), door.getHeight());
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

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
        if (items != null) {
            for (Item item : items) {
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
        if (door != null) door.render(batch);

        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        if (player != null) {
            player.render(batch);
            if (player.getStats() != null) {
                SkillManager sm = player.getStats().getSkillManager();
                if (sm != null && "heal".equals(sm.getCurrentSkillEffect()) && sm.getSkillEffectTimer() > 0) {
                    if (healTexture != null) {
                        float alpha = MathUtils.clamp(sm.getSkillEffectTimer() / 0.5f, 0f, 1f);
                        batch.setColor(1f, 1f, 1f, alpha);
                        batch.setBlendFunction(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE);
                        float floatOffset = (0.5f - sm.getSkillEffectTimer()) * 40f;
                        float drawX = player.getPosition().x + player.getHitbox().width / 2 - 32;
                        float drawY = player.getPosition().y + player.getHitbox().height / 2 - 32 + floatOffset;
                        batch.draw(healTexture, drawX, drawY, 64, 64);
                        batch.setBlendFunction(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
                        batch.setColor(Color.WHITE);
                    }
                }
            }
        }

        if (projectiles != null) {
            for (Projectile p : projectiles) {
                if (p.isActive()) {
                    p.render(batch);
                }
            }
        }

        batch.end();

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        drawHUD(batch);
        batch.end();

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
        if (gameWon) return;
        gameWon = true;
        int levelBonus = 1000 + (int) player.getHealth() * 5 - (int) gameTime;
        if (levelBonus < 0) levelBonus = 0;
        game.globalScore += levelBonus;
        if (mapMusic != null) mapMusic.stop();
        PlayerStats currentStats = player.getStats();
        game.setScreen(new ResultScreen(game, true, levelNumber, game.globalScore, currentStats,achievementManager));
    }

    private Enemy findNearestEnemy() {
        Enemy nearest = null;
        float minDistance = Float.MAX_VALUE;
        if (enemies == null) return null;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = player.getPosition().dst(enemy.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = enemy;
                }
            }
        }
        return nearest;
    }

    private void handleSkillInput() {
        if (player == null || player.getStats() == null) return;
        if (currentState == GameState.RUNNING && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (player != null && player.getStats() != null) {
                game.setScreen(new SkillTreeScreen(game, player.getStats(), this));
                currentState = GameState.PAUSED;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) useSkill1();
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) useSkill2();
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) useSkill3();

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            if (player.getStats().getSkillManager() != null && player.getStats().getSkillManager().canDash()) {
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
            enemy.update(delta, walls);
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
            if (!enemies.get(i).isAlive()) {
                if (player != null && player.getStats() != null) {
                    String enemyType = enemies.get(i).getClass().getSimpleName();
                    player.getStats().gainExpFromKill(enemyType);
                    if (achievementManager != null) achievementManager.trackKill();
                    System.out.println("killed " + enemyType + ", gained XP!");
                }
                enemies.removeIndex(i);
            }
        }
    }

    private void checkCollisions() {
        if (player == null) return;
        if (door != null && player.getHitbox().overlaps(door.getBounds())) {
            if (player.getStats().hasKey()) {
                door.tryOpen(player);
                System.out.println("Door opened! Level Completed!");
                winGame();
            } else {
                float delta = Gdx.graphics.getDeltaTime();
                com.badlogic.gdx.math.Vector2 velocity = player.getVelocity();
                player.getHitbox().x -= velocity.x * delta;
                player.getHitbox().y -= velocity.y * delta;
                player.syncPositionToHitbox();
            }
        }
        if (spawnInvulnTimer > 0f) return;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getBounds().overlaps(player.getHitbox())) {
                enemy.attack(player);
                if (attackSound != null) attackSound.play();
                player.triggerDamageVFX();
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy instanceof NineTailedFox && enemy.isAlive()) {
                NineTailedFox fox = (NineTailedFox) enemy;
                // System.out.println("Fox state: Form=" + fox.getCurrentForm() + ", HasEncountered=" + fox.hasEncounteredPlayer());
            }
        }
        if (player.getHealth() <= 0) {
            if (mapMusic != null) mapMusic.stop();
            HighScoreManager.saveScore(game.globalScore);
            game.resetGlobalScore();
            game.setScreen(new ResultScreen(game, false, levelNumber, 0,null,achievementManager));
        }
    }

    private void buildWalkableGrid() {
        int gridWidth = (int) (mapPixelWidth / Wall.TILE_SIZE);
        int gridHeight = (int) (mapPixelHeight / Wall.TILE_SIZE);
        walkableGrid = new boolean[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                walkableGrid[x][y] = true;
            }
        }
        for (Wall wall : walls) {
            int gridX = (int) (wall.worldX / Wall.TILE_SIZE);
            int gridY = (int) (wall.worldY / Wall.TILE_SIZE);
            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                walkableGrid[gridX][gridY] = false;
            }
        }
        for (Enemy enemy : enemies) {
            enemy.setWalkableGrid(walkableGrid);
        }
    }

    private void updatePlayer(float delta) {
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
            if (hb.overlaps(wall.getBounds())) return true;
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
        float taijiSize = 54f;
        float taijiOffsetX = -45f;
        float taijiOffsetY = -2f;

        batch.setColor(Color.WHITE);
        batch.draw(texHpFrame, x, y, frameWidth, frameHeight);

        float hpPercent = player.getHealth() / player.getMaxHealth();
        hpPercent = MathUtils.clamp(hpPercent, 0f, 1f);

        if (hpPercent > 0) {
            float currentBarWidth = maxBarWidth * hpPercent;
            batch.draw(texHpBar, x + barOffsetX, y + barOffsetY, currentBarWidth, barHeight);
        }

        float rotation = -gameTime * 80f;
        batch.draw(texTaiji, x + taijiOffsetX, y + taijiOffsetY, taijiSize / 2f, taijiSize / 2f, taijiSize, taijiSize, 1f, 1f, rotation, 0, 0, texTaiji.getWidth(), texTaiji.getHeight(), false, false);

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
        batch.setColor(Color.WHITE);
        float uiH = uiStage.getViewport().getWorldHeight();
        float uiW = uiStage.getViewport().getWorldWidth();
        drawXianxiaHealthBar(batch, 60, uiH - 70, 300);

        if (keyIconTexture != null && player != null && player.getStats() != null) {
            float keyIconSize = 48;
            float startX = 75;
            float startY = uiH - 130;
            float gap = 60;
            int bonusKeys = player.getStats().getBonusKey();
            boolean hasExitKey = player.getStats().hasKey();
            for (int i = 0; i < 3; i++) {
                if (i < 2) {
                    if (i < bonusKeys) batch.setColor(Color.GREEN);
                    else batch.setColor(Color.DARK_GRAY);
                } else {
                    if (hasExitKey) batch.setColor(Color.RED);
                    else batch.setColor(Color.DARK_GRAY);
                }
                batch.draw(keyIconTexture, startX + (i * gap), startY, keyIconSize, keyIconSize);
            }
            batch.setColor(Color.WHITE);
        }

        float frame1W = 260;
        float frame1H = 200;
        float frame1X = uiW - frame1W + 10;
        float frame1Y = uiH - frame1H + 10;
        drawHUDFrame(batch, frame1X, frame1Y, frame1W, frame1H);
        float textX = frame1X + 55;
        float textY = uiH - 55;
        float lineGap = 18;
        font.setColor(0.25f, 0.25f, 0.25f, 1f);
        font.getData().setScale(0.75f);
        font.draw(batch, "LEVEL " + levelNumber, textX, textY);
        textY -= lineGap;
        font.draw(batch, "TIME: " + (int) gameTime + "s", textX, textY);
        textY -= lineGap;
        if (player != null && player.getStats() != null) {
            int currentXP = player.getStats().getExpSystem().getCurrentExp();
            font.draw(batch, "SOULS(XP): " + currentXP, textX, textY);
            textY -= lineGap;
        }

        float frame2W = 320;
        float frame2H = 230;
        float frame2X = -10;
        float frame2Y = -10;
        batch.setColor(Color.WHITE);
        drawHUDFrame(batch, frame2X, frame2Y, frame2W, frame2H);
        textX = frame2X + 65;
        textY = (frame2Y + frame2H) - 85;
        font.setColor(0.25f, 0.25f, 0.25f, 1f);
        font.getData().setScale(0.8f);
        font.draw(batch, "--- CONTROLS ---", textX, textY);
        textY -= lineGap;
        font.getData().setScale(0.7f);

        if (player != null && player.getStats() != null) {
            SkillTree skillTree = player.getStats().getSkillTree();
            if (skillTree != null) {
                if (skillTree.hasQSkill()) {
                    if (skillTree.getQCooldown() > 0) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, String.format("Q: Fireball (%.1fs)", skillTree.getQCooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f);
                        font.draw(batch, "Q: Fireball (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "Q: Locked (Req. 150 XP)", textX, textY);
                }
                textY -= lineGap;
                if (skillTree.hasESkill()) {
                    if (skillTree.getECooldown() > 0) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, String.format("E: Heal (%.1fs)", skillTree.getECooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f);
                        font.draw(batch, "E: Heal (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "E: Locked (Req. 120 XP)", textX, textY);
                }
                textY -= lineGap;
                if (skillTree.hasRSkill()) {
                    if (skillTree.getRCooldown() > 0) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, String.format("R: Lightning (%.1fs)", skillTree.getRCooldown()), textX, textY);
                    } else {
                        font.setColor(0f, 0.5f, 0f, 1f);
                        font.draw(batch, "R: Lightning (READY)", textX, textY);
                    }
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "R: Locked (Req. 200 XP)", textX, textY);
                }
                textY -= lineGap;
                font.setColor(0.25f, 0.25f, 0.25f, 1f);
                font.draw(batch, "SPACE - Attack", textX, textY);
                textY -= lineGap;
                if (skillTree.hasDash()) {
                    font.draw(batch, "SHIFT - Dash", textX, textY);
                    textY -= lineGap;
                }
            }
        }

        if (exitPosition != null && arrowRegion != null && player != null) {
            batch.setColor(Color.WHITE);
            float tx = exitPosition.x * Wall.TILE_SIZE;
            float ty = exitPosition.y * Wall.TILE_SIZE;
            float dx = tx - player.getPosition().x;
            float dy = ty - player.getPosition().y;
            float currentAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;
            float imgW = arrowRegion.getRegionWidth();
            float imgH = arrowRegion.getRegionHeight();
            float originX = imgW / 2f;
            float originY = imgH / 2f;
            float uiScale = 0.2f;
            float finalRotation = currentAngle + 90f;
            float drawX = uiW - 100;
            float drawY = 100;
            batch.draw(arrowRegion, drawX - originX, drawY - originY, originX, originY, imgW, imgH, uiScale, uiScale, finalRotation);
            font.getData().setScale(0.8f);
            font.setColor(Color.WHITE);
            font.draw(batch, "EXIT", drawX - 20, drawY - 45);
        }
    }

    private void drawHUDFrame(SpriteBatch batch, float x, float y, float width, float height) {
        if (hudFrameTexture == null) return;
        batch.draw(hudFrameTexture, x, y, width, height);
    }

    private void drawDebugInfo() {
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) {
                    shapeRenderer.rect(enemy.getBounds().x, enemy.getBounds().y, enemy.getBounds().width, enemy.getBounds().height);
                }
            }
            shapeRenderer.end();
        }
    }

    private void updateTraps(float delta) {
        if (traps != null) {
            for (Trap trap : traps) {
                trap.update(delta);
            }
        }
    }

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

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiStage.getViewport().update(width, height, false);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        if (!isInitialized) {
            if (Gdx.files.internal("fireball.png").exists()) {
                // 🔥🔥 核心修改：加载火球并切割成动画 🔥🔥
                fireballTexture = new Texture(Gdx.files.internal("fireball.png"));

                // 假设火球是 1 行 4 列 (如果是 5 列请把 4 改成 5)
                int FRAME_COLS = 1;
                int FRAME_ROWS = 1;
                TextureRegion[][] tmp = TextureRegion.split(fireballTexture,
                        fireballTexture.getWidth() / FRAME_COLS,
                        fireballTexture.getHeight() / FRAME_ROWS);

                // 创建动画 (0.1f 是每帧间隔时间)
                fireballAnimation = new Animation<>(0.1f, tmp[0]);

            } else {
                System.out.println("Warning: fireball.png not found!");
            }

            if (Gdx.files.internal("lightning.png").exists()) {
                lightningTexture = new Texture(Gdx.files.internal("lightning.png"));
            } else {
                System.out.println("Warning: lightning.png not found!");
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

            MapLoader loader = new MapLoader();
            shapeRenderer = new ShapeRenderer();

            String actualPathToLoad;
            if (levelNumber > 5 || !Gdx.files.internal("maps/level-" + levelNumber + ".properties").exists()) {
                actualPathToLoad = "maps/level-5.properties";
            } else {
                actualPathToLoad = "maps/level-" + levelNumber + ".properties";
            }
            System.out.println("Loading Map: " + actualPathToLoad);


            System.out.println("[BEFORE loadLevel] levelNumber=" + levelNumber + " path=" + currentMapPath);
            MapLoader.LevelData data = loader.loadLevel(actualPathToLoad,this.levelNumber);

            this.walls = data.walls;
            this.entryPosition = data.entryPosition;
            this.exitPosition = data.exitPosition;

            this.enemies = new Array<>();
            if (data.enemies != null) {
                for (Enemy e : data.enemies) this.enemies.add(e);
            }

            this.traps = new Array<>();
            if (data.traps != null) {
                for (Trap t : data.traps) this.traps.add(t);
            }

            this.keys = new ArrayList<>();
            if (data.keys != null) {
                this.keys.addAll(data.keys);
                for (int i = 0; i < this.keys.size(); i++) {
                    this.keys.get(i).setBonus(i != this.keys.size() - 1);
                }
            }

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

            this.items = new ArrayList<>();
            if (data.items != null && !data.items.isEmpty()) {
                this.items.addAll(data.items);
                System.out.println("Items loaded from map: " + items.size());
            } else {
                System.out.println("No items defined in map properties.");
            }

            try {
                arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
                arrowRegion = new TextureRegion(arrowTexture);
            } catch (Exception e) {}

            initPlayer();
            initEnemies();

            achievementManager = new AchievementManager();
            console = new DeveloperConsole(game.getSkin(), uiStage, player, this);

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
                float r1 = e1.getWidth() / 2f;
                float r2 = e2.getWidth() / 2f;
                float cx1 = e1.getX() + r1;
                float cy1 = e1.getY() + e1.getHeight() / 2f;
                float cx2 = e2.getX() + r2;
                float cy2 = e2.getY() + e2.getHeight() / 2f;
                float dst = Vector2.dst(cx1, cy1, cx2, cy2);
                float minDist = r1 + r2 - 4f;
                if (dst < minDist) {
                    if (dst == 0) dst = 0.01f;
                    float overlap = minDist - dst;
                    float pushForce = overlap * 0.5f;
                    float pushX = (cx1 - cx2) / dst * pushForce;
                    float pushY = (cy1 - cy2) / dst * pushForce;
                    float nextX1 = e1.getX() + pushX;
                    float nextY1 = e1.getY() + pushY;
                    if (!collidesWithAnyWall(new Rectangle(nextX1, nextY1, e1.getWidth(), e1.getHeight()))) {
                        e1.setPosition(nextX1, nextY1);
                    }
                    float nextX2 = e2.getX() - pushX;
                    float nextY2 = e2.getY() - pushY;
                    if (!collidesWithAnyWall(new Rectangle(nextX2, nextY2, e2.getWidth(), e2.getHeight()))) {
                        e2.setPosition(nextX2, nextY2);
                    }
                }
            }
        }
    }

    private void initEnemies() {
        System.out.println("Configuring " + enemies.size + " enemies loaded from map...");
        for (Enemy enemy : enemies) {
            enemy.adjustDifficulty(this.levelNumber);
            enemy.setMapLimits(mapPixelWidth, mapPixelHeight);
            enemy.setWalkableGrid(walkableGrid);
            enemy.setPathFinder(pathFinder);
            enemy.setTargetPlayer(player);
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
                skillManager.setGameScreen(this);
            }
        }
        spawnInvulnTimer = SPAWN_INVULN_DURATION;
        if (player.getStats() != null) {
            player.getStats().heal(100);
        }
        player.syncPositionToHitbox();
    }

    @Override
    public void hide() {}

    private void useSkill1() {
        if (player == null || player.getStats() == null) return;
        SkillManager skillManager = player.getStats().getSkillManager();
        if (skillManager == null) return;

        if (!skillManager.hasQSkill()) {
            System.out.println("Q Skill not unlocked yet! (Need 150 Total XP)");
            return;
        }

        if (skillManager.canUseQSkill()) {
            skillManager.useSkill("Q");
            if (fireballSound != null) fireballSound.play(0.7f);
            if (projectiles != null) {
                float dmg = skillManager.calculateFireballDamage(30f);
                Enemy target = null;
                float minDst = Float.MAX_VALUE;
                for (Enemy e : enemies) {
                    if (!e.isAlive()) continue;
                    float dst = player.getPosition().dst(e.getPosition());
                    if (dst < minDst && dst < 400f) {
                        minDst = dst;
                        target = e;
                    }
                }
                Vector2 direction;
                if (target != null) {
                    direction = new Vector2(target.getPosition()).sub(player.getPosition()).nor();
                    System.out.println("Fireball aiming at enemy!");
                } else {
                    direction = new Vector2(0, -1);
                    if (player.getVelocity().len() > 0.1f) {
                        direction.set(player.getVelocity()).nor();
                    }
                    System.out.println("Fireball shooting in move direction");
                }

                // 🔥🔥 核心修改：使用 fireballAnimation 构造火球 🔥🔥
                if (fireballAnimation != null) {
                    Projectile fireball = new Projectile(
                            player.getPosition().x,
                            player.getPosition().y,
                            direction.x, direction.y,
                            300f,
                            dmg,
                            fireballAnimation, // 传入动画
                            40, 40 // 大小
                    );
                    projectiles.add(fireball);
                } else {
                    // Fallback if animation failed
                    Projectile fireball = new Projectile(
                            player.getPosition().x, player.getPosition().y,
                            direction.x, direction.y, 300f, dmg, Color.ORANGE
                    );
                    projectiles.add(fireball);
                }

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
        if (player == null || !player.getStats().getSkillManager().canUseRSkill()) return;
        Enemy nearestEnemy = null;
        float minDistance = Float.MAX_VALUE;
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                float dst = player.getPosition().dst(enemy.getPosition());
                if (dst < minDistance && dst < 600f) {
                    minDistance = dst;
                    nearestEnemy = enemy;
                }
            }
        }
        if (nearestEnemy != null) {
            if (player.getStats().getSkillManager().useSkill("R")) {
                float dmg = 25f;
                if (player.getStats().getSkillTree().getRSkill() != null) {
                    dmg = player.getStats().getSkillTree().getRSkill().skillValue;
                }
                float lightWidth = 64f;
                float lightHeight = 150f;
                float targetX = nearestEnemy.getPosition().x + nearestEnemy.getBounds().width / 2f;
                float targetY = nearestEnemy.getPosition().y;
                float startX = targetX - (lightWidth / 2f);
                float startY = targetY + 100f;

                // 闪电不需要动图，所以还是用 Texture 构造
                Projectile lightning = new Projectile(
                        startX, startY,
                        0, -1,
                        900f,
                        dmg,
                        lightningTexture,
                        lightWidth, lightHeight
                );
                if (projectiles != null) projectiles.add(lightning);
                if (lightningSound != null) lightningSound.play();
            }
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (traps != null) {
            for (Trap trap : traps) {
                if (trap instanceof Fog) ((Fog) trap).dispose();
                else if (trap instanceof MechanismTrap) ((MechanismTrap) trap).dispose();
            }
        }
        if (door != null) door.dispose();
        if (keys != null) for (Key k : keys) k.dispose();
        if (uiStage != null) uiStage.dispose();
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
        if (buttonBg != null) buttonBg.dispose();
        if (hudFrameTexture != null) hudFrameTexture.dispose();
        if (fireballTexture != null) fireballTexture.dispose();
        if (healTexture != null) healTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
        if (fireballSound != null) fireballSound.dispose();
        if (lightningSound != null) lightningSound.dispose();
        if (healSkillSound != null) healSkillSound.dispose();
        if (swingSound != null) swingSound.dispose();
    }

    public List<Wall> getWalls() { return walls; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Array<Enemy> getEnemies() { return enemies; }
    public List<Enemy> getEnemiesList() {
        List<Enemy> enemyList = new ArrayList<>();
        for (int i = 0; i < enemies.size; i++) {
            enemyList.add(enemies.get(i));
        }
        return enemyList;
    }
    public AStarPathFinder getPathFinder() { return pathFinder; }
    public Array<Trap> getTraps() { return traps; }
    public int[][] getCollisionMap() { return collisionMap; }
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
        float newY;
        if (mapPixelHeight < camera.viewportHeight * camera.zoom) {
            newY = mapPixelHeight / 2f;
        } else {
            newY = Math.max(halfH, Math.min(targetY, mapPixelHeight - halfH));
        }
        camera.position.set(newX, newY, 0);
    }
    private void buildCollisionMap(int width, int height) {
        collisionMap = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                collisionMap[y][x] = 0;
            }
        }
        for (Wall wall : walls) {
            if (wall.gridX >= 0 && wall.gridX < width && wall.gridY >= 0 && wall.gridY < height) {
                collisionMap[wall.gridY][wall.gridX] = 1;
            }
        }
        System.out.println("Collision map built: " + width + "x" + height);
    }
    private void initPathFinder() {
        if (collisionMap != null) {
            pathFinder = new AStarPathFinder(collisionMap);
            System.out.println("AStarPathFinder initialized");
        } else {
            System.out.println("Warning: Collision map not built, PathFinder not initialized");
        }
    }
    private Vector2 getRandomEmptyTile() {
        if (collisionMap == null) return null;
        List<Vector2> validTiles = new ArrayList<>();
        int h = collisionMap.length;
        int w = collisionMap[0].length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (collisionMap[y][x] == 0) {
                    validTiles.add(new Vector2(x, y));
                }
            }
        }
        if (!validTiles.isEmpty()) {
            Random random = new Random();
            return validTiles.get(random.nextInt(validTiles.size()));
        }
        return null;
    }
    private void updateKeys() {
        if (keys == null || player == null) return;
        for (Key k : keys) {
            if (k != null && !k.isCollected()) {
                if (player.getHitbox().overlaps(k.getBounds())) {
                    if (keySound != null) keySound.play(1.0f);
                    k.checkPickup(player);
                    System.out.println("Key collected!");
                }
            }
        }
    }
    private void checkPlayerAttackHit() {
        if (player == null || !player.isAttacking()) return;
        Rectangle attackBox = player.getAttackHitbox();
        float damage = player.getStats().getActualAttackDamage();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && attackBox.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                achievementManager.trackKill();
            }
        }
    }
    private void updateItems() {
        if (items == null || player == null) return;
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            if (player.getHitbox().overlaps(item.getBounds())) {
                item.onPickup(player);
                if (bonusSound != null) bonusSound.play();
                items.remove(i);
                System.out.println("Item collected!");
            }
        }
    }
    private void checkSkillCollisions(float delta) {
        if (player == null || player.getStats() == null) return;
        SkillManager sm = player.getStats().getSkillManager();
        if (sm == null || sm.getSkillEffectTimer() <= 0) return;
        if (sm.hasDealtDamage()) return;
        String currentSkill = sm.getCurrentSkillEffect();
        Vector2 skillPos = sm.getSkillEffectPosition();
        float damage = 0f;
        float range = 0f;
        boolean isAreaEffect = false;
        if ("fireball".equals(currentSkill)) {
            damage = 40f + player.getStats().getActualAttackDamage() * 0.5f;
            range = 40f;
            isAreaEffect = false;
        }
        else if ("lightning".equals(currentSkill)) {
            damage = 25f + player.getStats().getActualAttackDamage() * 0.3f;
            range = 120f;
            isAreaEffect = true;
        }
        else {
            return;
        }
        boolean hitAnyone = false;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float enemyCenterX = enemy.getX() + enemy.getWidth() / 2f;
            float enemyCenterY = enemy.getY() + enemy.getHeight() / 2f;
            float dist = Vector2.dst(skillPos.x, skillPos.y, enemyCenterX, enemyCenterY);
            if (dist <= range) {
                enemy.takeDamage(damage);
                if (!enemy.isAlive()) {
                    achievementManager.trackKill();
                }
                hitAnyone = true;
                System.out.println("Skill [" + currentSkill + "] hit enemy for " + (int)damage + " dmg!");
                if (!isAreaEffect) {
                    break;
                }
            }
        }
        if (hitAnyone) {
            sm.setHasDealtDamage(true);
        }
    }
}