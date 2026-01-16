package de.tum.cit.fop.maze.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

import java.util.Map;

public class SkillTreeScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SkillTree skillTree;
    private final PlayerStats playerStats;
    private final ExperienceSystem expSystem;
    private final Screen previousScreen;

    // 先声明，在构造函数中初始化
    private Label skillPointsLabel;
    private Label statsLabel;

    private SpriteBatch batch;
    private Texture menuBg;


    public SkillTreeScreen(MazeRunnerGame game, PlayerStats playerStats, Screen previousScreen) {
        this.game = game;
        this.playerStats = playerStats;
        this.skillTree = playerStats.getSkillTree();
        this.expSystem = playerStats.getExpSystem();
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        // 在构造函数中初始化标签
        this.skillPointsLabel = new Label("", game.getSkin());
        this.statsLabel = new Label("", game.getSkin());
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("menu_bg.png"));

        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Title
        Label title = new Label("SKILL TREE", game.getSkin(), "title");
        mainTable.add(title).padBottom(40).row();

        // 当前属性加成
        updateStatsLabel();
        mainTable.add(statsLabel).padBottom(30).row();

        // 技能点信息
        updateSkillPointsLabel();
        mainTable.add(skillPointsLabel).padBottom(30).row();

        // 提示信息
        Label hintLabel = new Label("Press T or ESC to return", game.getSkin());
        mainTable.add(hintLabel).padTop(20);
    }

    private void updateSkillPointsLabel() {
        if (expSystem != null) {
            String text = "XP: " + expSystem.getCurrentExp() +
                    " | Skill Points: " + expSystem.getSkillPoints() +
                    " | Level: " + expSystem.getCurrentLevel();
            skillPointsLabel.setText(text);
        } else {
            skillPointsLabel.setText("XP System not available");
        }
    }

    private void updateStatsLabel() {
        if (skillTree != null && playerStats != null) {
            String stats = String.format(
                    "Current Bonuses: HP +%.0f | Speed +%.0f%% | ATK +%.0f",
                    skillTree.getTotalHealthBonus(),
                    skillTree.getTotalSpeedBonus() * 100,
                    skillTree.getTotalAttackBonus()
            );
            statsLabel.setText(stats);
        } else {
            statsLabel.setText("Skill tree not available");
        }
    }

    private void returnToGame() {
        // Return to current game level
        if (previousScreen != null) {
            game.setScreen(previousScreen);
            this.dispose();
        } else {
            Gdx.app.error("SkillTreeScreen", "No previous screen found to return to!");
            // Fallback to level 1 if no previous screen

        }

    }


    @Override
    public void render(float delta) {
        // 1) 清屏（可以留黑，不影响，因为马上会画背景图）
        ScreenUtils.clear(0, 0, 0, 1);
        // ESC or T key to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
        }

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
        if (stage != null) {
            stage.dispose();
        }
        // 销毁背景图片
        if (menuBg != null) {
            menuBg.dispose();
        }
        // 销毁画笔
        if (batch != null) {
            batch.dispose();
        }


    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}