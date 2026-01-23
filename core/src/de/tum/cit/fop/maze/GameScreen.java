package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.AddAction;
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
 * The GameScreen class is responsible rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    //=======================================================================
    //camera and Rendering
    //========================================================================

    /** The screen camera  */
    private OrthographicCamera camera;
    /** drawing text on the screen.*/
    private BitmapFont font;
    /**  debug lines and solid shapes  */
    private ShapeRenderer shapeRenderer;
    /** access global resources. */
    private  MazeRunnerGame game;

    // =================================================================
    // Build Map and Environment.
    // =================================================================
    /** draw */
    private float mapWidth;
    private float mapHeight;
    /** logical */
    private int mapWidth1;
    private int mapHeight1;

    /** List of wall objects for collision detection. */
    private List<Wall> walls;
    /**  exit object. */
    private Exit exit;
    /** The door object that requires keys to open. */
    private Door door;
    /** The area triggering the level exit. */
    private Rectangle exitArea;

    private boolean gameWon = false;
    /** Textures for floor*/
    private Texture[] floorTexture;
    private int[][] floorMap;

    // =================================================================
    // Game Entities (Player, Enemies, Items)
    // =================================================================
    /**  character. */
    private Player player;
    /**  list of all  enemies. */
    private Array<Enemy> enemies;
    /** list of  projectiles like  fireballs, lightning, etc.. */
    private Array<Projectile> projectiles;
    /** list of collectible items like Jingangfu,Xiandan,Yufengfu . */
    private List<Item> items;
    /** list of keys. */
    private List<Key> keys;
    /**  list of traps . */
    private Array<Trap> traps;
    // =================================================================
    // Logic,including AIPathFinding,item,collision,level,time,etc.
    // =================================================================
    /** A* pathfinder used by enemies. */
    private AStarPathFinder pathFinder;

    /** Binary map for collision*/
    private int[][] collisionMap;
    /** Boolean grid for AI movement checks. */
    private boolean[][] canWalk;
    /** current level*/
    private int levelNumber;
    /** current map properties file. */
    private String currentMapPath;
    /** back to the previousStats*/
    private PlayerStats previousStats;
    /** total using time in the game level also used to calculate the grades*/
    private float gameTime = 0f;

    // =================================================================
    // UI, State and Input
    // =================================================================
    private Stage uiStage;
    /** create an pauseMenu Table in the game*/
    private Table pauseMenuTable;
    /**the current stage of the game,like RUNNING,PAUSED */
    private GameState currentState;
    /** keyboard Input*/
    private InputController controller;
    /** console that can control the game*/
    private DeveloperConsole console;
    /** for manager the setting*/
    private SettingsManager settingsManager;
    /** achievement system*/
    private AchievementManager achievementManager;
    private boolean isInitialized = false;
    private Texture buttonBg;
    /** Unify the ButtonStyle */
    private TextButton.TextButtonStyle ButtonStyle;

    // =================================================================
    // Assets: Textures & Audio
    // =================================================================
    // Textures
    private Texture wallTexture;
    private Texture arrowTexture;
    private TextureRegion arrowRegion;
    private Texture fireballTexture;

    private Texture lightningTexture;
    private Texture texHpFrame;
    private Texture texHpBar;
    private Texture texTaiji;
    private Texture healTexture;
    private Texture keyIconTexture;
    private Texture hudFrameTexture;
    private Animation<TextureRegion> fireballAnimation;
    //define Position to check and find the Positions of the exit and entry;
    private Vector2 exitPosition;
    private Vector2 entryPosition;
    // Audio
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
    //Timers
    private float fogSoundTimer = 0f;
    private float spawnTimer = 0f;
    private static final float SPAWN_INVULN_DURATION = 1.0f;
    //Decorations
    private List<Decoration>decorations;

    // =================================================================
    // Constructors
    // =================================================================
    /**
     * Constructs a new GameScreen with default settings (Level 1).
     *
     * @param game The main game class containing shared resources.
     */

    public GameScreen(MazeRunnerGame game) {
        this(game, 1,null);
    }

    /**
     * GameScreen with specific level and stats.
     *
     * @param game  main game class.
     * @param levelNumber load level to the game;.
     * @param prevStats Player stats from previous.
     */

    public GameScreen(MazeRunnerGame game, int levelNumber, PlayerStats prevStats) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.currentMapPath = "maps/level-" + levelNumber + ".properties";
        this.previousStats = prevStats;
//when level change, reset the key.initial it to be 0 ;
        if (this.previousStats != null) {
            this.previousStats.setBonusKey(0);
            this.previousStats.setHasKey(false);
        }
        init();
    }

    /**
     * Constructor for GameScreen with a specific map file path.
     *
     * @param game The main game class.
     * @param mapFilePath to the map file.
     */

    public GameScreen(MazeRunnerGame game, String mapFilePath) {
        this.game = game;
        this.levelNumber = 0;
        this.currentMapPath = mapFilePath;
        init();
    }

    /**
     * Initializes
     */

    private void init() {
        settingsManager = new SettingsManager();
        // camera;
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

        ButtonStyle();
        createPauseMenu();
    }

    /**
     * Creates the button style to make decorate button more easily.
     */


    private void ButtonStyle() {
        ButtonStyle = new TextButton.TextButtonStyle();
        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        ButtonStyle.font = baseFont;
        ButtonStyle.fontColor = Color.WHITE;
        ButtonStyle.downFontColor = Color.LIGHT_GRAY;

        if (buttonBg == null) {
            buttonBg = new Texture(Gdx.files.internal("button2.png"));
        }
        TextureRegionDrawable drawable = new TextureRegionDrawable(buttonBg);
        // to show the press effect.
        ButtonStyle.up = drawable;
        ButtonStyle.down = drawable.tint(Color.LIGHT_GRAY);

    }
    /**
     * pause menu UI
     */

    private void createPauseMenu() {
        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();
        pauseMenuTable.setVisible(false);

        Label pauseLable = new Label("GAME PAUSED", game.getSkin(), "title");
        pauseMenuTable.add(pauseLable).padBottom(40).row();

        // Resume Button
        TextButton resumeButton = new TextButton("Resume", ButtonStyle);
        resumeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                toggle();
            }
        });
        pauseMenuTable.add(resumeButton).width(350).height(80).padBottom(15).row();

        // Music Button(when press it could turn on the background Music).
        TextButton musicButton = new TextButton("Music:ON/OFF", ButtonStyle);
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
        //Quit Button(back to screen menu)
        TextButton quitButton = new TextButton("Exit to Menu", ButtonStyle);
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

    /**
     * Toggle the game,switch run to pause.
     */

    private void toggle() {
        if (currentState == GameState.RUNNING) {
            currentState = GameState.PAUSED;
            pauseMenuTable.setVisible(true);
            Gdx.input.setInputProcessor(uiStage);
            // at the same time, change music
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

    /**
     *  Handles input,update ,and draws the screen.
     *
     * @param delta  loop and speed:second
     */

    public void render(float delta) {
        // Toggle
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

        // Escape Key;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            toggle();
        }

        // update game
        if (currentState == GameState.RUNNING && !console.isVisible()) {
            controller.update();
            skillInput();

            //skill use
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) useSkill1();
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) useSkill2();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) useSkill3();
            //player attack
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (player != null) {
                    player.performAttack();
                    if (swingSound != null) swingSound.play(0.5f);
                }
            }

            // Zooming camera
            if (controller.zoomChange != 0) {
                camera.zoom += controller.zoomChange;
                if (camera.zoom < 0.2f) {
                    camera.zoom = 0.2f;
                }
                if (camera.zoom > 2.0f) {
                    camera.zoom = 2.0f;
                }
            }

            //update , to avoid the music overlapping
            gameTime += delta;
            if (spawnTimer > 0f) spawnTimer -= delta;
            if (fogSoundTimer > 0) fogSoundTimer -= delta;
            //update Entities
            if (traps != null) {
                for (int t = 0; t < traps.size; t++) {
                    Trap currentTrap = traps.get(t);
                    currentTrap.update(delta);
                }
            }
            updateEnemies(delta);
            updatePlayer(delta);
            //update Projectiles.
            if (projectiles != null) {
                for (int i = projectiles.size - 1; i >= 0; i--) {
                    Projectile p = projectiles.get(i);
                    p.update(delta);

                    if (p.checkEnemyHit(enemies)) {
                        projectiles.removeIndex(i);
                        continue;
                    }
                    if (p.getBounds().width < 40 && !p.isActive()) {
                        projectiles.removeIndex(i);
                    }
                    else if (p.getBounds().width >= 40 && p.getPosition().y < player.getPosition().y - 400) {
                        projectiles.removeIndex(i);
                    }
                }
            }

            // check Collisions and some interactions between player and item/enemy/....
            checkCollisions();
            checkTrapActivation();
            checkPlayerAttackHit();
            checkSkillCollisions(delta);
            updateKeys();
            updateItems();
            //update SKill
            SkillManager skillManager = null;
            if (player != null && player.getStats() != null) {
                skillManager = player.getStats().getSkillManager();
                if (skillManager != null) {
                    skillManager.update(delta);
                }
            }

            //fog trap ;
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
            // the camera for fog .
            if (visibilityFactor < 1.0f) {
                float targetZoom = 0.2f;
                camera.zoom = 0.2f;
            }

            updateCameraFollowPlayer();
            camera.update();
        }

        // Rendering


        // every time need to clear first and then render again
        ScreenUtils.clear(0, 0, 0, 1);
        SpriteBatch batch = game.getSpriteBatch();
        batch.setProjectionMatrix(camera.combined);


        // draw floor
        batch.begin();
        for (int x = 0; x < mapWidth1; x++) {
            for (int y = 0; y < mapHeight1; y++) {
                Texture tex = floorTexture[floorMap[x][y]];
                batch.draw(tex, x * Wall.TILE_SIZE, y * Wall.TILE_SIZE, Wall.TILE_SIZE, Wall.TILE_SIZE);
            }
        }
        batch.end();

        //draw Door
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (door != null) {
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(door.getX(), door.getY(), door.getWidth(), door.getHeight());
        }
        shapeRenderer.end();

        //draw Game objects
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (walls != null) {
            for (Wall wall : walls) {
                batch.draw(wallTexture, wall.worldX, wall.worldY, Wall.TILE_SIZE, Wall.TILE_SIZE);
            }
        }

        if (decorations != null) {
            for (Decoration d : decorations) {
                d.render(batch);
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
            // draw the heal skill,
            if (player.getStats() != null) {
                SkillManager sm = player.getStats().getSkillManager();
                if (sm != null && "heal".equals(sm.getCurrentSkillEffect()) && sm.getSkillEffectTimer() > 0) {
                    if (healTexture != null) {
                        float drawX = player.getPosition().x + player.getHitbox().width / 2 - 32;
                        float drawY = player.getPosition().y + player.getHitbox().height / 2 - 32 ;
                        batch.draw(healTexture, drawX, drawY, 64, 64);

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


        //HUD draw .
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        drawHUD(batch);
        batch.end();

        // draw pause screen
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

    /**
     * Condition of  game win and the logic of screen transition.
      */

    private void winGame() {
        if (gameWon) return;
        gameWon = true;
        // calculate levelBonus
        int levelBonus = 1000 + (int) player.getHealth() * 5 - (int) gameTime;
        if (levelBonus < 0) levelBonus = 0;
        game.globalScore += levelBonus;
        if (mapMusic != null) mapMusic.stop();
        PlayerStats currentStats = player.getStats();
        game.setScreen(new ResultScreen(game, true, levelNumber, game.globalScore, currentStats,achievementManager));
    }

    /**
     * deal with InputSkill
     */
    private void skillInput() {
        if (player == null || player.getStats() == null) return;
        // press T ,Open skill tree
        if (currentState == GameState.RUNNING && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (player != null && player.getStats() != null) {
                game.setScreen(new SkillTreeScreen(game, player.getStats(), this));
                currentState = GameState.PAUSED;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) useSkill1();
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) useSkill2();
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) useSkill3();

        // dash
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


/**
 * Updates enemy .like movement and chasing player.
 *
 * @param delta Time passed since last frame.
 */

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            enemy.update(delta, walls);
            if (player != null && pathFinder != null) {
                float distance = enemy.getPosition().dst(player.getPosition());
                if (distance <= enemy.getDetectionRange()) {
                    enemy.findPathTo(player.getPosition());
                    if (distance <= enemy.getAttackRange() && spawnTimer <= 0f) {
                        enemy.attack(player);
                    }
                } else {
                    enemy.clearPath();
                }
            }
        }
        // to avoid enemy overlap
        resolveEnemyOverlaps(delta);
        // when enemies die , clear it.
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

    /**
     * Checks  collisions .
     */
    private void checkCollisions() {
        if (player == null) return;
        //check Door Collision
        if (door != null && player.getHitbox().overlaps(door.getBounds())) {
            if (player.getStats().hasKey()) {
                door.tryOpen(player);
                System.out.println("Door opened!");
                winGame();
            } else {
                // player can not go to next level.push back player
                float delta = Gdx.graphics.getDeltaTime();
                com.badlogic.gdx.math.Vector2 velocity = player.getVelocity();
                player.getHitbox().x -= velocity.x * delta;
                player.getHitbox().y -= velocity.y * delta;
                player.syncPositionToHitbox();
            }
        }
        if (spawnTimer > 0f) return;
        //Enemy Collision;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getBounds().overlaps(player.getHitbox())) {
                enemy.attack(player);
                if (attackSound != null) attackSound.play();
                player.triggerDamageVFX();
            }
        }

        //check Player Death,when go to the (ResultScreen)failScreen.
        if (player.getHealth() <= 0) {
            if (mapMusic != null) mapMusic.stop();
            HighScoreManager.saveScore(game.globalScore);
            game.resetGlobalScore();
            game.setScreen(new ResultScreen(game, false, levelNumber, 0,null,achievementManager));
        }
    }

    /**
     * Builds a grid of the map for AI optimization.
     *<p>
     * Each cell correspnds to a tile and indicates whether it can be traversed.
     * This grid is mainly used by enemy AI for pathfinding.
     */
    private void buildWalkableGrid() {
        int gridWidth = (int) (mapWidth / Wall.TILE_SIZE);
        int gridHeight = (int) (mapHeight / Wall.TILE_SIZE);

        canWalk = new boolean[gridWidth][gridHeight];
        // Default (true)
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                canWalk[x][y] = true;
            }
        }
        //wall as false;
        for (Wall wall : walls) {
            int gridX = (int) (wall.worldX / Wall.TILE_SIZE);
            int gridY = (int) (wall.worldY / Wall.TILE_SIZE);

            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                canWalk[gridX][gridY] = false;
            }
        }
        for (Enemy enemy : enemies) {
            enemy.setWalkableGrid(canWalk);
        }
    }

    /**
     * Updates the player's movement press w ,a,s,d ,shift so that
     * realize player move up left,down,run.
     * @param delta Time passed since last frame.
     */
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

    /**
     * boolean return to check if a rectangle collides any wall;
     *
     * @param hb  hitbox help  to check if collision.
     * @return True if collision detected, false otherwise.
     */
    private boolean collidesWithAnyWall(Rectangle hb) {
        if (walls == null) return false;
        for (Wall wall : walls) {
            if (hb.overlaps(wall.getBounds())) return true;
        }
        return false;
    }


    /**
     * Draws the player's health bar
     *
     * @param batch The sprite batch.
     * @param x X position .
     * @param y Y position .
     * @param totalWidth Width of the bar.
     */
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

        //Draw rotating Taiji symbol.
        float rotation = -gameTime * 80f;
        batch.draw(texTaiji, x + taijiOffsetX, y + taijiOffsetY, taijiSize / 2f, taijiSize / 2f, taijiSize, taijiSize, 1f, 1f, rotation, 0, 0, texTaiji.getWidth(), texTaiji.getHeight(), false, false);

        String hpText = (int)player.getHealth() + " / " + (int)player.getMaxHealth();
        font.getData().setScale(0.8f);
        drawShadowText(batch, hpText, x + frameWidth / 2f + 20, y + frameHeight / 2f - 5);
        font.getData().setScale(1f);
    }


    /**
     * Draws shadow text for better visibility.
     * @param batch The sprite batch.
     * @param text The text to draw.
     * @param x X position.
     * @param y Y position.
     */
    private void drawShadowText(SpriteBatch batch, String text, float x, float y) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(Color.WHITE);
        font.draw(batch, text, x, y);
    }

    /**
     * Draws HUD
     *
     * @param batch  sprite batch.
     */
    private void drawHUD(SpriteBatch batch) {
        // draw HealthBar
        batch.setColor(Color.WHITE);
        float uiH = uiStage.getViewport().getWorldHeight();
        float uiW = uiStage.getViewport().getWorldWidth();
        drawXianxiaHealthBar(batch, 60, uiH - 70, 300);
        //draw keys; show red when the collect key gets, else show green.
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

        //draw Scroll as background for better visibility. adjust to find the most suitable position.
        //and add XP in Bar.
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

        // Control Scroll
        float frame2W = 320;
        float frame2H = 230;
        float frame2X = -10;
        float frame2Y = -10;
        batch.setColor(Color.WHITE);
        drawHUDFrame(batch, frame2X, frame2Y, frame2W, frame2H);

        //draw Scroll for skill prompt. different color represent different stats.
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
                        font.draw(batch, "Q: Fireball (" + (int)skillTree.getQCooldown() + "s)", textX, textY);
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
                        font.draw(batch, "E: Heal (" + (int)skillTree.getECooldown() + "s)", textX, textY);
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
                        font.draw(batch, "R: Lightning (" + (int)skillTree.getRCooldown() + "s)", textX, textY);
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

        //Draw Exit Arrow/
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


    /**
     * drawHUD Frame.(Scroll background)
     *
     * @param batch Sprite batch.
     * @param x X position.
     * @param y Y position.
     * @param width Width.
     * @param height Height.
     */

    private void drawHUDFrame(SpriteBatch batch, float x, float y, float width, float height) {
        if (hudFrameTexture == null) return;
        batch.draw(hudFrameTexture, x, y, width, height);
    }
    /**
     * traps attack player..
     */
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
            // load fireball texture.
            if (Gdx.files.internal("fireball.png").exists()) {
                fireballTexture = new Texture(Gdx.files.internal("fireball.png"));

                int FRAME_COLS = 1;
                int FRAME_ROWS = 1;
                //Texture be split for doing animation so that the game because more vivid.
                TextureRegion[][] tmp = TextureRegion.split(fireballTexture,
                        fireballTexture.getWidth() / FRAME_COLS,
                        fireballTexture.getHeight() / FRAME_ROWS);

                fireballAnimation = new Animation<>(0.1f, tmp[0]);

            } else {
                System.out.println("Warning: fireball.png not found!");
            }

            // other textures loading.like lighting,wall
            if (Gdx.files.internal("lightning.png").exists()) {
                lightningTexture = new Texture(Gdx.files.internal("lightning.png"));
            } else {
                System.out.println("Warning: lightning.png not found!");
            }
            projectiles = new Array<>();
            wallTexture = new Texture(Gdx.files.internal("wall.png"));

            healTexture = new Texture(Gdx.files.internal("heal.png"));
            hudFrameTexture = new Texture(Gdx.files.internal("scroll_1.png"));
            keyIconTexture = new Texture(Gdx.files.internal("key.png"));
            // load Map
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
            this.decorations = data.decorations;


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
                //bonus key.
                for (int i = 0; i < this.keys.size(); i++) {
                    this.keys.get(i).setBonus(i != this.keys.size() - 1);
                }
            }

            // to avoid  no exit in the map
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

            //map dimension
            int maxX = 0, maxY = 0;
            for (Wall w : walls) {
                if (w.gridX > maxX) maxX = w.gridX;
                if (w.gridY > maxY) maxY = w.gridY;
            }
            mapWidth = (maxX + 1) * Wall.TILE_SIZE;
            mapHeight = (maxY + 1) * Wall.TILE_SIZE;
            mapWidth1 = maxX + 1;
            mapHeight1 = maxY + 1;

            buildCollisionMap(maxX + 1, maxY + 1);
            buildWalkableGrid();
            initPathFinder();

            //floor set up (Randomized)
            floorTexture = new Texture[]{
                    new Texture(Gdx.files.internal("floor.png")),
                    new Texture(Gdx.files.internal("floor_flower.png")),
                    new Texture(Gdx.files.internal("floor_grass.png"))
            };


            floorMap = new int[mapWidth1][mapHeight1];
            Random rng = new Random();

            // decorated by flower , grass .
            for (int x = 0; x < mapWidth1; x++) {
                for (int y = 0; y < mapHeight1; y++) {
                    float r = rng.nextFloat();
                    if (r < 0.06f) floorMap[x][y] = 1;
                    else if (r < 0.12f) floorMap[x][y] = 2;
                    else floorMap[x][y] = 0;
                }
            }

            //loading  items
            this.items = new ArrayList<>();
            if (data.items != null && !data.items.isEmpty()) {
                this.items.addAll(data.items);
                System.out.println("Items loaded from map: " + items.size());
            } else {
                System.out.println("No items defined in map properties.");
            }


            //add arrow
            try {
                arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
                arrowRegion = new TextureRegion(arrowTexture);
            } catch (Exception e) {}

            //add player and enemy
            initPlayer();
            initEnemies();

            // show achievementManager and console
            achievementManager = new AchievementManager();
            console = new DeveloperConsole(game.getSkin(), uiStage, player, this);

            for (Enemy enemy : enemies) {
                enemy.setPathFinder(pathFinder);
            }

            // music show
            game.stopMenuMusic();

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

                // Asset loading

                texHpFrame = new Texture(Gdx.files.internal("HUD/hp_frame.png"));
                texHpBar = new Texture(Gdx.files.internal("HUD/hp_bar.png"));
                texTaiji = new Texture(Gdx.files.internal("HUD/taiji_gold.png"));

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

    /**
     * avoid the enemyOverlaps.
     * @param delta
     */

    private void resolveEnemyOverlaps(float delta) {
        for (int i = 0; i < enemies.size; i++) {
            for (int j = i + 1; j < enemies.size; j++) {
                Enemy e1 = enemies.get(i);
                Enemy e2 = enemies.get(j);
                if (e1.isAlive() && e2.isAlive() && e1.getBounds().overlaps(e2.getBounds())) {
                    e1.setPosition(e1.getX() + 1, e1.getY() + 1);
                }
            }
        }
    }


    /**
     * init Enemies .make sure is difficulty , location and behavior.
     */

    private void initEnemies() {
        System.out.println("Configuring " + enemies.size + " enemies loaded from map...");
        for (Enemy enemy : enemies) {
            enemy.adjustDifficulty(this.levelNumber);
            enemy.setMapLimits(mapWidth, mapHeight);
            enemy.setWalkableGrid(canWalk);
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


    /**
     * initPlayer update Player's state
     */

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
        spawnTimer = SPAWN_INVULN_DURATION;
        if (player.getStats() != null) {
            player.getStats().heal(100);
        }
        player.syncPositionToHitbox();
    }

    @Override
    public void hide() {}


    /**
     * active the fireball skills .
     */
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

                //using fireballAnimation .
                if (fireballAnimation != null) {
                    Projectile fireball = new Projectile(
                            player.getPosition().x,
                            player.getPosition().y,
                            direction.x, direction.y,
                            300f,
                            dmg,
                            fireballAnimation,
                            40, 40
                    );
                    projectiles.add(fireball);
                } else {

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

    /**
     * active the Heal skill
     */


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

    /**
     * Active lighting skill.
     */

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


    /**
     * keep the player centered on the screen.
     * clamps the camera make it doesn't show outside .
     */

    private void updateCameraFollowPlayer() {
        if (player == null) return;
        float targetX = player.getPosition().x;
        float targetY = player.getPosition().y;
        // bounds
        float halfW = camera.viewportWidth * 0.5f * camera.zoom;
        float halfH = camera.viewportHeight * 0.5f * camera.zoom;
        float newX;
        // if smaller ,center
        if (mapWidth < camera.viewportWidth * camera.zoom) {
            newX = mapWidth / 2f;
        } else {
            // clamp to the map edges
            newX = Math.max(halfW, Math.min(targetX, mapWidth - halfW));
        }
        float newY;
        if (mapHeight < camera.viewportHeight * camera.zoom) {
            newY = mapHeight / 2f;
        } else {
            newY = Math.max(halfH, Math.min(targetY, mapHeight - halfH));
        }
        camera.position.set(newX, newY, 0);
    }

    /**
     * Constructs a collision map based on wall positions.
     *  A* pathfinding algorithm.
     *
     * @param width The width of the map in tiles.
     * @param height The height of the map in tiles.
     */

    private void buildCollisionMap(int width, int height) {
        collisionMap = new int[height][width];
        //initial grid =0
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                collisionMap[y][x] = 0;
            }
        }
        // mark wall as 1
        for (Wall wall : walls) {
            if (wall.gridX >= 0 && wall.gridX < width && wall.gridY >= 0 && wall.gridY < height) {
                collisionMap[wall.gridY][wall.gridX] = 1;
            }
        }
        System.out.println("Collision map built: " + width + "x" + height);
    }

    /**
     * Initializes the pathfinding system if the collision map is ready.
     */

    private void initPathFinder() {
        if (collisionMap != null) {
            pathFinder = new AStarPathFinder(collisionMap);
            System.out.println("AStarPathFinder initialized");
        } else {
            System.out.println("Warning: Collision map not built, PathFinder not initialized");
        }
    }

    /**
     * check if the key is picked up or not
     * Plays a sound effect and updates key status upon collection.
     */
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
    /**
     * check if the player attack enemy .
     * Applies damage to enemies if a hit is detected.
     */

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

    /**
     * check if the item has been picked up
     * also handle items' effect and sound
     */
    private void updateItems() {
        if (items == null ) return;
        if (player == null) return;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            Rectangle itemBounds = item.getBounds();
            Rectangle playerBounds = player.getHitbox();

            if (playerBounds.overlaps(itemBounds)) {
                item.onPickup(player);
                if (bonusSound != null) {
                    bonusSound.play();
                }
                items.remove(i);
                i--;
                System.out.println("Item collected!");
            }
        }
    }

    /**
     * check collisions between active skills (Fireball, Lightning) and enemies.
     * show damage and visual effects in  enemy.
     * @param delta The time passed since the last frame.
     */

    private void checkSkillCollisions(float delta) {
        if (player == null || player.getStats() == null) return;
        SkillManager sm = player.getStats().getSkillManager();
        if (sm == null || sm.getSkillEffectTimer() <= 0) return;
        if (sm.hasDealtDamage()) return;//prevent multiple hits;

        String currentSkill = sm.getCurrentSkillEffect();
        Vector2 skillPos = sm.getSkillEffectPosition();
        float damage = 0f;
        float range = 0f;
        boolean isAreaEffect = false;
        // skill properties
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
            float diffX = skillPos.x - enemyCenterX;
            float diffY = skillPos.y - enemyCenterY;
            // c^2 = a^2 + b^2
            float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
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


    /**
     * getter and setter
     */

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
}