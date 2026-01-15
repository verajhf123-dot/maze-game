package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;


public class HighScoreScreen implements Screen {
    private Stage stage;
    private MazeRunnerGame game;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;



    public HighScoreScreen(MazeRunnerGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Sound file not found!");
        }


        table.add(new Label("--- Top 5 Records ---", game.getSkin(), "title")).padBottom(30).row();

        String[] scores = HighScoreManager.getTopScores();
        if (scores.length == 0) {
            table.add(new Label("No records yet, go play!", game.getSkin())).row();
        } else {
            for (int i = 0; i < scores.length; i++) {
                table.add(new Label((i+1) + ". Score: " + scores[i], game.getSkin())).padBottom(10).row();
            }
        }

        TextButton back = new TextButton("Back to Menu", game.getSkin());
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.setScreen(new MenuScreen(game, game.getSettingsManager()));
            }
        });
        table.add(back).padTop(30);
    }

    @Override public void show() {

        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));


    }
    @Override
    public void render(float delta) {
        // 1) 清屏（可以留黑，不影响，因为马上会画背景图）
        ScreenUtils.clear(0, 0, 0, 1);

        // 2) 先画背景图（一定要在 stage.draw 之前）
        batch.begin();
        batch.draw(menuBg, 0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        batch.end();

        // 3) 再画 UI（按钮、文字）
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() {
        stage.dispose();
        if (buttonSound != null) {
            buttonSound.dispose();
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}