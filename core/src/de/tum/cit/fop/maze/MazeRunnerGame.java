package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SettingsManager settingsManager;
    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;
    // UI Skin
    private Skin skin;

    // Character animation downwards
    private Animation<TextureRegion> characterDownAnimation;
    // update background Music
    private Music backgroundMusic;

    // 保存文件选择器；
    private NativeFileChooser fileChooser;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {

        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        settingsManager = new SettingsManager();



        this.loadCharacterAnimation(); // Load character animation

        // Play some background music
        // Background sound
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        goToMenu(); // Navigate to the menu screen
    }

    // to update Music;
    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this, settingsManager));

        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }



    /**
     * Switches to the game screen.
     */


    /**
     * Loads the character animation from the character.png file.
     */private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;

        // libGDX internal Array instead of ArrayList because of performance
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        // Add all frames to the animation
        for (int col = 0; col < animationFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }

        characterDownAnimation = new Animation<>(0.1f, walkFrames);
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }

        if (skin != null) {
            skin.dispose();
        }
    }

    public void goToGame() {
        goToGame(1);
    }

    public void goToGame(int levelNumber) {
        this.setScreen(new GameScreen(this, levelNumber));

        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    public void goToGame(String mapFilePath){
        this.setScreen(new GameScreen(this,mapFilePath));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }



    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public NativeFileChooser getFileChooser() {
        return fileChooser;
    }
}
