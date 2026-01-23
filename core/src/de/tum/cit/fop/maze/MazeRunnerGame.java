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
    private MenuScreen menuScreen;
    private GameScreen gameScreen;


    private SettingsManager settingsManager;
    private SpriteBatch spriteBatch;
    private Skin skin;


    public int globalScore =0;



    private Animation<TextureRegion> characterDownAnimation;
    private Music backgroundMusic;
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

        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        settingsManager = new SettingsManager();

        this.loadCharacterAnimation();

        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/menu_music.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
            playMenuMusic();
        } catch (Exception e) {
            System.out.println("Error loading menu music: " + e.getMessage());
        }


        setScreen(new StoryScreen(this));
    }

    /**
     * control the background music.
     */
    public void playMenuMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void stopMenuMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }


    public Music getBackgroundMusic() {
        return backgroundMusic;
    }
    public void goToMenu() {
        this.setScreen(new MenuScreen(this, settingsManager));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }


    /**
     * Loads the character animation from the character.png file.
     */
    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);
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
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }

    public void goToGame() {
        goToGame(1,null);
    }

    public void goToGame(int levelNumber,PlayerStats stats) {
        this.setScreen(new GameScreen(this, levelNumber, stats));

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

    public void resetGlobalScore() {
        globalScore=0;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

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
