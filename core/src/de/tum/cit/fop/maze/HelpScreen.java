package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

/**
 * Help screen that shows the game controls and basic gameplay instructions.
 * It uses a Stage + Table UI and has a button to go back to the main menu.
 */

public class HelpScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;

    private Texture buttonBg;
    private TextButton.TextButtonStyle buttonStyle;

    private Texture scrollTexture;

    public HelpScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Sound file not found!");
        }
    }

    /**
     * Creates the button style used on this screen.
     * Uses "button2.png" as background, otherwise falls back to skin default.
     */

    private void createButtonStyle() {
        buttonStyle = new TextButton.TextButtonStyle();

        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        buttonStyle.font = baseFont;

        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        try {
            if (buttonBg == null) {
                buttonBg = new Texture(Gdx.files.internal("button2.png"));
            }
            TextureRegionDrawable drawable = new TextureRegionDrawable(buttonBg);

            buttonStyle.up = drawable;
            buttonStyle.down = drawable.tint(Color.LIGHT_GRAY);

        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Button texture (button2.png) not found!");
            buttonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            buttonStyle.fontColor = Color.BLACK;
        }
    }
    /**
     * Builds the help UI once when the screen is shown.
     * Loads background, sets input to stage, and creates labels/buttons.
     */

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));

        createButtonStyle();
        Gdx.input.setInputProcessor(stage);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Table scrollTable = new Table();
        try {
            if (Gdx.files.internal("scroll_1.png").exists()) {
                scrollTexture = new Texture(Gdx.files.internal("scroll_1.png"));
            } else {
                scrollTexture = new Texture(Gdx.files.internal("scroll_1.jpg"));
            }
            scrollTable.setBackground(new TextureRegionDrawable(scrollTexture));
        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Scroll texture not found.");
        }

        Label titleLabel = new Label("HOW TO PLAY", game.getSkin(), "title");
        scrollTable.add(titleLabel).padTop(80).padBottom(20).row();

        String instructions =
                "--- CONTROLS ---\n" +
                        " Move:  W, A, S, D\n" +
                        " Attack: SPACE (360 Omnidirectional Strike)\n" +
                        " Sprint: Hold SHIFT (Requires Dash Skill)\n" +
                        " Skills: Q (Fireball), E (Heal), R (Lightning)\n" +
                        " Menus:  T (Skill Tree), ~ (Dev Console)\n" +
                        " Pause:  ESC\n\n" +

                        "--- HOW TO PLAY ---\n" +
                        "1. HUNT: Defeat mythical beasts to gain XP.\n" +
                        "2. ASCEND: Level up to earn Skill Points.\n" +
                        "3. EVOLVE: Press T to unlock new abilities.\n" +
                        "4. ACHIEVE: Unlock Medals.\n" +
                        "5. GOAL: Find the Spirit Key & Exit.";

        Label infoLabel = new Label(instructions, game.getSkin());
        infoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        infoLabel.setColor(Color.WHITE);

        scrollTable.add(infoLabel).padBottom(30).row();

        TextButton backButton = new TextButton("Back to Menu", buttonStyle);
        backButton.getLabel().setFontScale(1.1f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.goToMenu();
            }
        });
        scrollTable.add(backButton).width(350).height(100).padBottom(50);
        rootTable.add(scrollTable).width(1150).height(1300);
    }
    /**
     * Draws the background and the UI every frame.
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
    /**
     * Updates the viewport when the window size changes.
     */

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    /**
     * Disposes resources created by this screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        if (buttonSound != null) buttonSound.dispose();
        if (batch != null) batch.dispose();
        if (menuBg != null) menuBg.dispose();
        if (buttonBg != null) buttonBg.dispose();
        if (scrollTexture != null) scrollTexture.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}