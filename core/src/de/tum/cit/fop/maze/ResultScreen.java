package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import java.util.Map;
import java.util.Map.Entry;
import de.tum.cit.fop.maze.AchievementManager;

/**
 * Result screen shown after a level ends (victory or game over).
 * It displays the result, score (and optional achievement stats),
 * and gives buttons to continue or return to menu.
 */

public class ResultScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final boolean isVictory;
    private final int currentLevel;
    private final int score;
    private Music resultMusic;
    private Sound buttonSound;
    private PlayerStats savedStats;
    private AchievementManager achievementManager;

    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;

    private SpriteBatch batch;
    private Texture menuBg;

    /**
     * Creates a result screen for either victory or defeat.
     *
     * @param game main game instance (used for switching screens and shared batch)
     * @param isVictory true = victory screen, false = game over screen
     * @param currentLevel current level number
     * @param score total score to display
     * @param stats player stats that should be carried to the next level
     * @param achievementManager provides extra end-screen stats
     */

    public ResultScreen(MazeRunnerGame game, boolean isVictory, int currentLevel, int score, PlayerStats stats, AchievementManager achievementManager) {
        this.game = game;
        this.isVictory = isVictory;
        this.currentLevel = currentLevel;
        this.score = score;
        this.savedStats = stats;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.achievementManager = achievementManager;
    }

    /**
     * createButtonStyle
     * Uses "button2.png" as background, and falls back to skin default if missing.
     *
     */
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
            Gdx.app.log("ResultScreen", "Button texture (button2.png) not found! Reverting to default.");
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    /**
     * Builds the UI, loads background, music, and sets input to this stage.
     * Shows "Next Level" on victory, and "Retry Level" on defeat, plus "Back to Menu".
     */

    @Override
    public void show() {
        batch = new SpriteBatch();
        try {
            String bgPath = isVictory ? "victory.png" : "defeated.png";
            menuBg = new Texture(Gdx.files.internal(bgPath));
        } catch (Exception e) {
            Gdx.app.log("ResultScreen", "Background texture not found!");
        }

        createButtonStyle();

        Gdx.input.setInputProcessor(stage);

        try {
            if (isVictory) {
                resultMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/orchestral-win-331233.mp3"));
            } else {
                resultMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/game-over-417465.mp3"));
            }
            resultMusic.setLooping(false);
            resultMusic.play();

            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));

        } catch (Exception e) {
            System.out.println("Error loading result music: " + e.getMessage());
        }

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        String titleText = isVictory ? "VICTORY!" : "GAME OVER";
        Label titleLabel = new Label(titleText, game.getSkin(), "title");
        titleLabel.setColor(isVictory ? Color.GOLD : Color.RED);
        table.add(titleLabel).padBottom(20).row();

        Label scoreLabel = new Label("Total Journey Scores: " + score, game.getSkin());
        table.add(scoreLabel).padBottom(40).row();


        if (achievementManager != null) {
            Label statsLabel = new Label("--- Cultivation Summary ---", game.getSkin());
            statsLabel.setColor(Color.CYAN);
            table.add(statsLabel).padBottom(5).row();

            Label killLabel = new Label("Enemies Defeated: " + achievementManager.getTotalKills(), game.getSkin());
            table.add(killLabel).padBottom(5).row();

            Label expLabel = new Label("Spirit Energy Gained: " + achievementManager.getTotalExpGained(), game.getSkin());
            table.add(expLabel).padBottom(10).row();

            String achievements = "Achievements: ";
            for (Map.Entry<String, Boolean> entry : achievementManager.getUnlockedStatus().entrySet()) {
                if (entry.getValue()) achievements += entry.getKey() + "  ";
            }
            Label achieveList = new Label(achievements, game.getSkin());
            achieveList.setFontScale(0.8f);
            achieveList.setColor(Color.YELLOW);
            table.add(achieveList).padBottom(30).row();
        }

        if (isVictory) {
            TextButton nextBtn = new TextButton("Next Level", commonButtonStyle);
            nextBtn.getLabel().setFontScale(1.1f);
            nextBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                    game.goToGame(currentLevel+1,savedStats);

                }
            });
            table.add(nextBtn).width(350).height(100).padBottom(20).row();
        } else {
            TextButton retryBtn = new TextButton("Retry Level", commonButtonStyle);
            retryBtn.getLabel().setFontScale(1.1f);
            retryBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                    game.resetGlobalScore();
                    game.goToGame(1,null);
                }
            });
            table.add(retryBtn).width(350).height(100).padBottom(20).row();
        }

        TextButton menuBtn = new TextButton("Back to Menu", commonButtonStyle);
        menuBtn.getLabel().setFontScale(1.1f);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playClickAndStopMusic();
                game.goToMenu();
            }
        });
        table.add(menuBtn).width(350).height(100).row();
    }

    /**
     * Plays the button click sound (if available) and stops the result music.
     * This is called before switching screens.
     */

    private void playClickAndStopMusic() {
        if (buttonSound != null) buttonSound.play();
        if (resultMusic != null) resultMusic.stop();
    }
    /**
     * Clears the screen, draws the background image, then renders the stage UI.
     *
     * @param delta time since last frame
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (batch != null && menuBg != null) {
            batch.begin();
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }
    /**
     * Updates the viewport on resize.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        if (buttonBg != null) buttonBg.dispose();
        if (menuBg != null) menuBg.dispose();
        if (batch != null) batch.dispose();
    }
}