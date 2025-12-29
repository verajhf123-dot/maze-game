package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
       stage.addActor(table);

       table.add(new Label("SETTINGS",game.getSkin(),"title")).padBottom(40).colspan(2).row();

        table.add(new Label("Audio Volume:", game.getSkin())).right().padRight(20);


        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        volumeSlider.setValue(settings.getVolume()); // 读取当前音量


        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = volumeSlider.getValue();
                settings.setVolume(val);

                if (game.getBackgroundMusic() != null) {
                    game.getBackgroundMusic().setVolume(val);
                }
            }
        });
        table.add(volumeSlider).width(200).row();

        table.add(new Label("", game.getSkin())).height(20).row();



        addKeyBindingRow(table, "Move Up", "move_up");
        addKeyBindingRow(table, "Move Down", "move_down");
        addKeyBindingRow(table, "Move Left", "move_left");
        addKeyBindingRow(table, "Move Right", "move_right");
        addKeyBindingRow(table, "Run / Sprint", "run");

        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settings.save(); // 退出时确保保存文件
                game.goToMenu();
            }
        });
        table.add(backButton).colspan(2).padTop(40).width(300).height(50);
    }


    private void addKeyBindingRow(Table table, String displayName, final String keyName) {
        // 左边：功能名称
        table.add(new Label(displayName + ":", game.getSkin())).right().padRight(20).padBottom(10);

        // 右边：显示当前按键的按钮
        int currentKeyCode = settings.getKey(keyName);
        String keyString = Input.Keys.toString(currentKeyCode); // 把数字变成 "W", "UP" 等文字

        final TextButton keyButton = new TextButton(keyString, game.getSkin());

        // 点击按钮后的逻辑
        keyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1. 改变按钮文字，提示用户输入
                keyButton.setText("Press any key...");

                // 2. 临时切换输入处理器，监听下一个按下的键
                Gdx.input.setInputProcessor(new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        // 3. 用户按下了键 (keycode)

                        // 保存新按键到 SettingsManager
                        settings.setKey(keyName, keycode);

                        // 更新按钮显示的文字
                        keyButton.setText(Input.Keys.toString(keycode));

                        // 4. 恢复正常的 UI 输入处理器
                        Gdx.input.setInputProcessor(stage);
                        return true;
                    }
                });
            }
        });

        table.add(keyButton).width(150).padBottom(10).row();
    }

    @Override
    public void render(float delta) {
        // 黑色背景
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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