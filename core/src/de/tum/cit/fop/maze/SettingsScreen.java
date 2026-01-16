package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align; // 需要导入 Align
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final SettingsManager settings;
    private Stage stage;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;

    // 按钮样式资源
    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;

    public SettingsScreen(MazeRunnerGame game, SettingsManager settings) {
        this.game = game;
        this.settings = settings;

        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("SettingsScreen", "Button sound not found!");
        }
    }

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
            Gdx.app.log("SettingsScreen", "Button texture (button2.png) not found!");
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));

        createButtonStyle();

        stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 标题
        table.add(new Label("SETTINGS", game.getSkin(), "title")).padBottom(40).colspan(3).row();

        // ------------------ 音量控制部分 (替换了 Slider) ------------------

        // 左侧标签
        table.add(new Label("Audio Volume:", game.getSkin())).right().padRight(20);

        // 创建一个内部表格来横向排列 [-] [数字] [+]
        Table volumeControlTable = new Table();

        // 1. 减号按钮
        TextButton minusBtn = new TextButton("-", commonButtonStyle);
        // 2. 音量显示标签 (例如 "50%")
        final Label volumeValueLabel = new Label(String.format("%.0f%%", settings.getVolume() * 100), game.getSkin());
        volumeValueLabel.setAlignment(Align.center);
        // 3. 加号按钮
        TextButton plusBtn = new TextButton("+", commonButtonStyle);

        // --- 按钮逻辑 ---
        minusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play(settings.getVolume());
                updateVolume(-0.1f, volumeValueLabel);
            }
        });

        plusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play(settings.getVolume());
                updateVolume(0.1f, volumeValueLabel);
            }
        });

        // 将组件加入内部表格
        // 按钮大小设为小一点的正方形或矩形，比如 50x50
        volumeControlTable.add(minusBtn).width(50).height(50).padRight(10);
        volumeControlTable.add(volumeValueLabel).width(60).align(Align.center); // 数字居中
        volumeControlTable.add(plusBtn).width(50).height(50).padLeft(10);

        // 将内部表格加入主表格
        table.add(volumeControlTable).left().row();

        // -------------------------------------------------------------

        table.add(new Label("", game.getSkin())).height(20).colspan(3).row();

        // 绑定按键
        addKeyBindingRow(table, "Move Up", "move_up");
        addKeyBindingRow(table, "Move Down", "move_down");
        addKeyBindingRow(table, "Move Left", "move_left");
        addKeyBindingRow(table, "Move Right", "move_right");
        addKeyBindingRow(table, "Run / Sprint", "run");

        // 返回按钮
        TextButton backButton = new TextButton("Back to Menu", commonButtonStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play(settings.getVolume());
                settings.save();
                game.goToMenu();
            }
        });
        table.add(backButton).colspan(3).padTop(40).width(300).height(80);
    }

    /**
     * 辅助方法：更新音量逻辑
     */
    private void updateVolume(float changeAmount, Label displayLabel) {
        float currentVol = settings.getVolume();
        float newVol = Math.max(0f, Math.min(1f, currentVol + changeAmount)); // 限制在 0.0 - 1.0 之间

        settings.setVolume(newVol);
        if (game.getBackgroundMusic() != null) {
            game.getBackgroundMusic().setVolume(newVol);
        }

        // 更新显示的百分比文字
        displayLabel.setText(String.format("%.0f%%", newVol * 100));
    }

    private void addKeyBindingRow(Table table, String displayName, final String keyName) {
        // Label 占一列
        table.add(new Label(displayName + ":", game.getSkin())).right().padRight(20).padBottom(10);

        int currentKeyCode = settings.getKey(keyName);
        String keyString = Input.Keys.toString(currentKeyCode);

        final TextButton keyButton = new TextButton(keyString, commonButtonStyle);
        keyButton.getLabel().setFontScale(0.9f);

        keyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (buttonSound != null) buttonSound.play(settings.getVolume());
                keyButton.setText("Press key...");
                Gdx.input.setInputProcessor(new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        settings.setKey(keyName, keycode);
                        keyButton.setText(Input.Keys.toString(keycode));
                        Gdx.input.setInputProcessor(stage);
                        return true;
                    }
                });
            }
        });

        // Button 占一列 (注意这里的 colspan 如果上面是 3 列，这里可能需要调整，
        // 但由于使用了 Table 的自动布局，只要这一行只有两个元素，LibGDX通常会处理，
        // 为了保险起见，可以让 Button 跨两列或者把上面的 Label 跨一列，
        // 这里简单处理：让上面的 Audio Volume Label 占一列，Volume Control Table 占两列，
        // 这里的 Key Binding 也是 Label 占一列，Button 占两列。)
        table.add(keyButton).width(160).height(50).padBottom(10).colspan(2).left().row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
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
        if (buttonSound != null) buttonSound.dispose();
        if (menuBg != null) menuBg.dispose();
        if (batch != null) batch.dispose();
        if (buttonBg != null) buttonBg.dispose();
    }
}