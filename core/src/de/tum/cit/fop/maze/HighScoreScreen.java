package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class HighScoreScreen implements Screen {
    private Stage stage;
    private MazeRunnerGame game;

    public HighScoreScreen(MazeRunnerGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


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
                game.setScreen(new MenuScreen(game, game.getSettingsManager()));
            }
        });
        table.add(back).padTop(30);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() { stage.dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}