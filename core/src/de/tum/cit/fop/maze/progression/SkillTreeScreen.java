package de.tum.cit.fop.maze.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
        } else {
            // Fallback to level 1 if no previous screen
            game.goToGame(1, playerStats);
        }
        this.dispose();
    }

    @Override
    public void render(float delta) {
        // ESC or T key to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
        }

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

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}