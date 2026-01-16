package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import com.badlogic.gdx.audio.Sound;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Table table;
    private final SettingsManager settingsManager;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;

    // Textures for button styling
    private Texture buttonBg;
    private TextButton.TextButtonStyle menuButtonStyle;

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game, SettingsManager settingsManager) {
        this.game = game;
        this.settingsManager = settingsManager;

        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        this.table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table);// Add the table to the stage

        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("MenuScreen", "Sound file not found!");
        }
    }

    /**
     * Configures the button style to match HighScoreScreen (White text, button2.png background).
     */
    private void createButtonStyle() {
        menuButtonStyle = new TextButton.TextButtonStyle();

        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        menuButtonStyle.font = baseFont;

        menuButtonStyle.fontColor = Color.WHITE;
        menuButtonStyle.downFontColor = Color.LIGHT_GRAY;

        try {
            if (buttonBg == null) {
                buttonBg = new Texture(Gdx.files.internal("button2.png"));
            }
            TextureRegionDrawable drawable = new TextureRegionDrawable(buttonBg);

            menuButtonStyle.up = drawable;
            menuButtonStyle.down = drawable.tint(Color.LIGHT_GRAY);

        } catch (Exception e) {
            Gdx.app.log("MenuScreen", "Button texture (button2.png) not found!");
            menuButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            menuButtonStyle.fontColor = Color.BLACK;
        }
    }

    /**
     * Helper to add a button with the custom style and original sizing parameters.
     */
    private void addMenuButton(String text, ChangeListener listener) {
        TextButton button = new TextButton(text, menuButtonStyle);
        button.getLabel().setFontScale(1.1f); // Matches HighScoreScreen font scale
        button.addListener(listener);

        // MODIFIED HERE:
        // Width: 450 (Wider than before)
        // Height: 110 (Taller/Bigger)
        // padBottom: 10 (Tight spacing to keep them concentrated in the middle)
        table.add(button).width(400).height(110).padBottom(10).row();
    }

    private void showMainMenu() {
        table.clear();

        // MODIFIED HERE:
        // padTop(60): Moves the title down from the top edge.
        // padBottom(20): Reduces gap between title and first button.
        table.add(new Label("Maze Runner", game.getSkin(), "title")).padTop(60).padBottom(20).row();

        if (SaveManager.hasSaveFile()) {
            addMenuButton("Continued", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (buttonSound != null) buttonSound.play();
                    handleContinueGame();
                }
            });
        }

        // Create and add a button to go to the game screen
        addMenuButton("New Game", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();

                Preferences prefs = Gdx.app.getPreferences("MazeRunnerGame");
                game.resetGlobalScore();
                game.goToGame(1, null);
            }
        });

        addMenuButton("Select level ", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                showLevelSelection(5);
            }
        });

        addMenuButton("Load Custom Map", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                chooseMapFile();
            }
        });

        addMenuButton("Settings", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.setScreen(new SettingsScreen(game, settingsManager));
            }
        });

        addMenuButton("High Scores", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.setScreen(new HighScoreScreen(game));
            }
        });

        addMenuButton("Help", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.setScreen(new HelpScreen(game));
            }
        });

        addMenuButton("Exit", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                Gdx.app.exit();
            }
        });
    }

    private void handleContinueGame() {
        SaveData data = SaveManager.loadGame();
        if (data != null) {
            Gdx.app.log("MenuScreen", "Loading Save Game...");

            // 1. Rebuild PlayerStats object
            PlayerStats loadedStats = new PlayerStats();

            // 2. Restore basic attributes
            loadedStats.setCurrentHealth(data.getCurrentHealth());

            // 3. Restore experience system
            if (loadedStats.getExpSystem() != null) {
                loadedStats.getExpSystem().setLevel(data.getCharLevel());
                loadedStats.getExpSystem().setCurrentExp(data.getCurrentExp());
                loadedStats.getExpSystem().setSkillPoints(data.getSkillPoints());
            }

            // 4. Restore skill tree
            if (data.getUnlockedSkillIds() != null) {
                for (String skillId : data.getUnlockedSkillIds()) {
                    loadedStats.getSkillTree().forceUnlock(skillId);
                }
            }

            // 5. Recalculate attribute bonuses
            loadedStats.applySkillEffects();

            Gdx.app.log("MenuScreen", "Loaded Level: " + data.getCurrentLevelMap() + ", Char Level: " + data.getCharLevel());

            game.goToGame(data.getCurrentLevelMap(), loadedStats);

        } else {
            Gdx.app.log("MenuScreen", "Error loading save file.");
        }
    }

    private void showLevelSelection(int maxNumber) {
        table.clear();
        table.add(new Label(" Select Level ", game.getSkin(), "title")).padTop(60).padBottom(20).row();
        for (int i = 1; i <= maxNumber; i++) {
            final int level = i;
            TextButton levelButton = new TextButton("Level " + level, menuButtonStyle);
            levelButton.getLabel().setFontScale(1.0f);

            // Match sizing with main menu
            table.add(levelButton).width(450).height(100).padBottom(10).row();

            levelButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (buttonSound != null) buttonSound.play();
                    game.goToGame(level, null);
                }
            });
        }

        TextButton backButton = new TextButton("Back", menuButtonStyle);
        backButton.getLabel().setFontScale(1.1f);

        table.add(backButton).width(450).height(110).padTop(10).row();

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                showMainMenu();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);// Clear the screen

        batch.begin();
        if (menuBg != null) {
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        // Dispose of the stage when screen is disposed
        stage.dispose();
        if (menuBg != null) menuBg.dispose();
        if (buttonBg != null) buttonBg.dispose();
        if (buttonSound != null) buttonSound.dispose();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));

        // Initialize style logic
        createButtonStyle();

        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        game.playMenuMusic();
        showMainMenu();
    }

    // The following methods are part of the Screen interface but are not used in this screen.
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    private void chooseMapFile() {
        var fileChooser = game.getFileChooser();
        if (fileChooser == null) {
            Gdx.app.log("MenuScreen", "FileChooser not initialized (probably Web or Android?)");
            return;
        }

        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();
        config.directory = Gdx.files.absolute(System.getProperty("user.home"));

        config.nameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".map") || name.endsWith(".properties");
            }
        };
        config.title = "Select Custom Map";

        fileChooser.chooseFile(config, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                Gdx.app.log("MenuScreen", "File chosen: " + file.path());
                game.goToGame(file.path());
            }

            @Override
            public void onCancellation() {
                Gdx.app.log("MenuScreen", "Cancelled");
            }

            @Override
            public void onError(Exception e) {
                Gdx.app.log("MenuScreen", "Error choosing map: " + e.getMessage());
            }
        });
    }
}