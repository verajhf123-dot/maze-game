package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.FileHandler;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Table table;



    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        this.table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table);// Add the table to the stage
        showMainMenu();
    }
    private void showMainMenu() {
        table.clear();
        // Add a label as a title
        table.add(new Label("Maze Runner", game.getSkin(), "title")).padBottom(80).row();

        // Create and add a button to go to the game screen
        TextButton goToGameButton = new TextButton("Go To Game", game.getSkin());
        table.add(goToGameButton).width(300).row();
        goToGameButton.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Preferences prefs = Gdx.app.getPreferences("MazeRunnerGame");
                int maxLevel = prefs.getInteger("maxLevel", 1);
                if(maxLevel == 1) {
                    game.goToGame(1);

                }else {
                   showLevelSelection(maxLevel);
                }

            }// 如果只有第一关，直接开始，如果解锁很多关，显示选择页面。
        });

        TextButton loadGameButton = new TextButton("Load Map ", game.getSkin());
        table.add(loadGameButton).width(300).padTop(10).row();
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLevelSelection(5);


            }
        });
        TextButton settingButton = new TextButton("Settings", game.getSkin());
        table.add(settingButton).width(300).padTop(10).row();
        settingButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                Gdx.app.log("MenuScreen", "Settings");

            }
        });


        TextButton highScoresButton = new TextButton("High Scores", game.getSkin());
        table.add(highScoresButton).width(300).padTop(10).row();
        highScoresButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "High Scores");
            }
        });
        TextButton HelpButton = new TextButton("Help", game.getSkin());
        table.add(HelpButton).width(300).padTop(10).row();
        HelpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "Help");
            }
        });

        TextButton exitButton = new TextButton("Exit", game.getSkin());
        table.add(exitButton).width(300).padTop(10).row();
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    private void showLevelSelection(int maxNumber) {
        table.clear();
        table.add(new Label(" Select Level " ,game.getSkin(),"title")).padBottom(40).row();
        for(int i = 1; i <= maxNumber; i++) {
            final int level = i;
            TextButton levelButton = new TextButton("Level " + level, game.getSkin());
            table.add(levelButton).width(300).padTop(10).row();
            levelButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToGame(level);
                }
            });
        }

        TextButton backButton = new TextButton("Back", game.getSkin());
        table.add(backButton).width(300).padTop(20).row();
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showMainMenu();
            }
        });
    }

    private void chooseMapFile() {
        var fileChooser = game.getFileChooser();
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();

        config.directory = Gdx.files.absolute(System.getProperty("user.home"));

        config.nameFilter = new  FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".map") || name.endsWith(".properties");
            }
        };

        config.title = "Select Map";
        fileChooser.chooseFile(config, new NativeFileChooserCallback() {

            public void onFileChosen(FileHandle  file) {
                Gdx.app.log("MenuScreen", "File chosen: " + file.path());
                game.goToGame(file.path());
            }

            public void onCancellation() {
                Gdx.app.log("MenuScreen", "Cancelled");
            }

            public void onError(Exception e) {
                Gdx.app.log("MenuScreen", "Error choosing map " + e.getMessage());
            }
        });
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
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
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
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
}
