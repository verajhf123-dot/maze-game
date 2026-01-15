package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;



public class HelpScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Sound buttonSound;
    private SpriteBatch batch;
    private Texture menuBg;


    public HelpScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        try {
            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));
        } catch (Exception e) {
            Gdx.app.log("HelpScreen", "Sound file not found!");
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));

        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("HOW TO PLAY", game.getSkin(), "title")).padBottom(40).row();

        String instructions =
                "--- CONTROLS ---\n" +
                        " Move:  W, A, S, D\n" +
                        " Attack: SPACE (360° Omnidirectional Strike)\n" + // 突出全方位攻击
                        " Sprint: Hold SHIFT (Requires Dash Skill)\n" +
                        " Skills: Q (Fireball), E (Heal), R (Lightning)\n" +
                        " Menus:  T (Skill Tree), ~ (Dev Console)\n" +   // 加入控制台说明
                        " Pause:  ESC\n\n" +

                        "--- HOW TO PLAY ---\n" +
                        "1. HUNT: Defeat mythical beasts to gain XP.\n" +
                        "2. ASCEND: Level up to earn Skill Points.\n" +
                        "3. EVOLVE: Press T to unlock new abilities.\n" +
                        "4. ACHIEVE: Reach secret milestones to unlock Medals.\n" + // 突出成就系统
                        "5. GOAL: Find the Spirit Key & reach the Exit Portal.";
        Label infoLabel = new Label(instructions, game.getSkin());
        infoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        table.add(infoLabel).padBottom(40).row();


        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (buttonSound != null) buttonSound.play();
                game.goToMenu();
            }
        });
        table.add(backButton).width(300).height(60);
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


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (buttonSound != null) {
            buttonSound.dispose();
        }
    }

    //这几个方法暂时用不到，留空即可
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}