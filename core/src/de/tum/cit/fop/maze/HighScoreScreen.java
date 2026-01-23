package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;

/**
 * Screen that displays the top 5 high scores.
 * It reads scores from HighScoreManager and shows them in a simple UI with a back button.
 */


public class HighScoreScreen implements Screen {
    private Stage stage;
    private MazeRunnerGame game;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;
    private Texture scroll;
    private Texture ButtonBg;


    /**
     * Builds the UI for the high score screen.
     * Scores are loaded once here and added as labels to the table.
     *
     * @param game main game
     */

    public HighScoreScreen(MazeRunnerGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);
        Table scrollTable = new Table();

        try {
            scroll = new Texture(Gdx.files.internal("scroll.png"));
            scrollTable.setBackground(new TextureRegionDrawable(scroll));
        } catch (Exception e) {
            Gdx.app.log("HighScore", "Scroll texture not found.");
        }


        Label titleLabel = new Label("Top 5 Records", game.getSkin(), "title");
        titleLabel.setFontScale(0.75f);
        scrollTable.add(titleLabel).padBottom(25).row();
//score->Label->add to Label-> stage.draw()
        String[] scores = HighScoreManager.getTopScores();
        if (scores.length == 0) {
            Label noRec = new Label("No records yet, go play!", game.getSkin());
            noRec.setColor(Color.WHITE);
            noRec.setFontScale(0.9f);
            scrollTable.add(noRec).row();
        } else {
            for (int i = 0; i < scores.length; i++) {
                Label scoreLabel = new Label((i+1) + ". Score: " + scores[i], game.getSkin());
                scoreLabel.setColor(Color.WHITE);
                scoreLabel.setFontScale(1.1f);
                scrollTable.add(scoreLabel).padBottom(10).row();
            }
        }


        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Sound file not found!");
        }
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        btnStyle.font = baseFont;

        btnStyle.fontColor = Color.WHITE;
        btnStyle.downFontColor = Color.LIGHT_GRAY;


        try {
            ButtonBg = new Texture(Gdx.files.internal("button2.png"));
            TextureRegionDrawable drawable = new TextureRegionDrawable(ButtonBg);
            btnStyle.up = drawable;
            btnStyle.down = drawable.tint(Color.LIGHT_GRAY);

        } catch (Exception e) {
            Gdx.app.log("HighScore", "Button texture (button2.png) not found!");
            btnStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            btnStyle.fontColor = Color.BLACK;
        }


        TextButton back = new TextButton("Back to Menu",btnStyle);
        back.getLabel().setFontScale(1.1f);
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.setScreen(new MenuScreen(game, game.getSettingsManager()));
            }
        });
        scrollTable.add(back).width(420).height(110).padTop(50).padBottom(40);


        rootTable.add(scrollTable).width(500).height(650);
    }
    /**
     * Called when this screen becomes active.
     * Sets the input processor and loads the background texture.
     */
    @Override public void show() {
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));
    }

    /**
     * Draws the background and the stage every frame.
     *
     * @param delta time since last frame
     */

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        batch.draw(menuBg, 0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        batch.end();
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() {
        stage.dispose();
        if (buttonSound != null) {
            buttonSound.dispose();
        }
        if (batch != null) batch.dispose();
        if (menuBg != null) menuBg.dispose();
        if (scroll != null) scroll.dispose();
        if (ButtonBg != null) ButtonBg.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}