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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
/**
 * Screen for game settings.
 *
 * Provides UI to adjust audio volume and rebind keys. Settings are stored via SettingsManager
 * (LibGDX Preferences). The UI is built with Scene2D (Stage + Table).
 */

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final SettingsManager settings;
    private Stage stage;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;
    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;
    /**
     * Creates the settings screen.
     *
     * @param game main game instance used for shared resources and screen navigation
     * @param settings settings manager used to read and store preferences
     */

    public SettingsScreen(MazeRunnerGame game, SettingsManager settings) {
        this.game = game;
        this.settings = settings;
        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("SettingsScreen", "Button sound not found!");
        }
    }

    /**
     * Creates a shared TextButton style for this screen (font + colors + background texture).
     * Falls back to the default skin style if custom assets are missing.
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
            Gdx.app.log("SettingsScreen", "Button texture (button2.png) not found!");
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    /**
     * Builds the settings UI and sets the Stage as the active input processor.
     * This method creates volume controls and key binding buttons.
     */

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

        table.add(new Label("SETTINGS", game.getSkin(), "title")).padBottom(40).colspan(3).row();
        table.add(new Label("Audio Volume:", game.getSkin())).right().padRight(20);
        Table volumeControlTable = new Table();
        TextButton minusBtn = new TextButton("-", commonButtonStyle);
        final Label volumeValueLabel = new Label(String.format("%.0f%%", settings.getVolume() * 100), game.getSkin());
        volumeValueLabel.setAlignment(Align.center);
        TextButton plusBtn = new TextButton("+", commonButtonStyle);

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

        volumeControlTable.add(minusBtn).width(50).height(50).padRight(10);
        volumeControlTable.add(volumeValueLabel).width(60).align(Align.center); // 数字居中
        volumeControlTable.add(plusBtn).width(50).height(50).padLeft(10);
        table.add(volumeControlTable).left().row();
        table.add(new Label("", game.getSkin())).height(20).colspan(3).row();

        addKeyBindingRow(table, "Move Up", "move_up");
        addKeyBindingRow(table, "Move Down", "move_down");
        addKeyBindingRow(table, "Move Left", "move_left");
        addKeyBindingRow(table, "Move Right", "move_right");
        addKeyBindingRow(table, "Run / Sprint", "run");

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
     * Updates the stored volume by a small step, clamps it to [0,1],
     * applies it to background music, and refreshes the UI label.
     *
     * @param changeAmount volume delta (e.g. +0.1 or -0.1)
     * @param displayLabel label showing the current volume percentage
     */

    private void updateVolume(float changeAmount, Label displayLabel) {
        float currentVol = settings.getVolume();
        float newVol = Math.max(0f, Math.min(1f, currentVol + changeAmount));

        settings.setVolume(newVol);
        if (game.getBackgroundMusic() != null) {
            game.getBackgroundMusic().setVolume(newVol);
        }

        displayLabel.setText(String.format("%.0f%%", newVol * 100));
    }
    /**
     * Adds a row to the table for rebinding a single action.Change the preferences.
     * When the user clicks the button, the next pressed key is stored in SettingsManager.
     *
     * @param table table to add UI elements to
     * @param displayName label shown in the UI (e.g. "Move Up")
     * @param keyName internal action name used in SettingsManager (e.g. "move_up")
     */

    private void addKeyBindingRow(Table table, String displayName, final String keyName) {
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
        table.add(keyButton).width(160).height(50).padBottom(10).colspan(2).left().row();
    }
    /**
     * Renders the background image and draws the Scene2D stage.
     */

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act(delta);
        stage.draw();
    }
    /**
     * Updates the viewport on window resize.
     */

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    /**
     * Disposes UI and assets used by this screen.
     */

    @Override
    public void dispose() {
        stage.dispose();
        if (buttonSound != null) buttonSound.dispose();
        if (menuBg != null) menuBg.dispose();
        if (batch != null) batch.dispose();
        if (buttonBg != null) buttonBg.dispose();
    }
}