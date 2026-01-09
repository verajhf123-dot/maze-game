package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;



public class HelpScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    public HelpScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("HOW TO PLAY", game.getSkin(), "title")).padBottom(40).row();

        String instructions =
                "CONTROLS:\n" +
                        "Move: WASD or Arrow Keys\n" +
                        "Run: Hold SHIFT\n" +
                        "Pause: ESC\n" +
                        "Zoom: I / O\n\n" +
                        "OBJECTIVE:\n" +
                        "1. Find the Key to unlock the Exit.\n" +
                        "2. Avoid monsters and traps.\n" +
                        "3. Reach the Exit to win!";

        Label infoLabel = new Label(instructions, game.getSkin());
        table.add(infoLabel).padBottom(40).row();


        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(backButton).width(300).height(60);
    }

    @Override
    public void render(float delta) {
        // 清屏 (黑色背景)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    //这几个方法暂时用不到，留空即可
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}