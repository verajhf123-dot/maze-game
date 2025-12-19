package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final SettingsManager settings;
    private Stage stage;

    public SettingsScreen(MazeRunnerGame game, SettingsManager settings) {
        this.game = game;
        this.settings = settings;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10);

        TextButton upButton =
                new TextButton("Move Up: " + settings.getKey("move_up"), game.getSkin());
        TextButton downButton =
                new TextButton("Move Down: " + settings.getKey("move_down"), game.getSkin());
        TextButton leftButton =
                new TextButton("Move Left: " + settings.getKey("move_left"), game.getSkin());
        TextButton rightButton =
                new TextButton("Move Right: " + settings.getKey("move_right"), game.getSkin());
        TextButton runButton =
                new TextButton("Run: " + settings.getKey("run"), game.getSkin());

        TextButton backButton =
                new TextButton("Back", game.getSkin());

        // 暂时先不做重绑定，后面一步一步来
        upButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {}
        });
        downButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {}
        });
        leftButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {}
        });
        rightButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {}
        });
        runButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {}
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        table.add(upButton).row();
        table.add(downButton).row();
        table.add(leftButton).row();
        table.add(rightButton).row();
        table.add(runButton).row();
        table.add(backButton).padTop(30).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
