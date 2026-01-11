package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import java.util.Map;
import java.util.Map.Entry; // 必须导入 Entry 接口
import de.tum.cit.fop.maze.AchievementManager;


public class ResultScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final boolean isVictory;
    private final int currentLevel;
    private final int score;
    private Music resultMusic;
    private Sound buttonSound;
    private PlayerStats savedStats;
    private AchievementManager achievementManager;


    public ResultScreen(MazeRunnerGame game, boolean isVictory, int currentLevel, int score,PlayerStats stats,AchievementManager achievementManager) {
        this.game = game;
        this.isVictory = isVictory;
        this.currentLevel = currentLevel;
        this.score = score;
        this.savedStats = stats;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.achievementManager = achievementManager;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage); // 必须开启输入处理

        try {
            if (isVictory) {
                resultMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/orchestral-win-331233.mp3"));
            } else {
                resultMusic = Gdx.audio.newMusic(Gdx.files.internal("Sound/game-over-417465.mp3"));
            }
            resultMusic.setLooping(false);
            resultMusic.play();

            buttonSound = Gdx.audio.newSound(Gdx.files.internal("Sound/button.mp3"));

        } catch (Exception e) {
            System.out.println("Error loading result music: " + e.getMessage());
        }



        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 1. 标题
        String titleText = isVictory ? "VICTORY!" : "GAME OVER";
        Label titleLabel = new Label(titleText, game.getSkin(), "title");
        titleLabel.setColor(isVictory ? Color.GOLD : Color.RED);
        table.add(titleLabel).padBottom(20).row();

        // 2. 分数
        Label scoreLabel = new Label("Total Journey Scores: " + score, game.getSkin());
        table.add(scoreLabel).padBottom(40).row();


        if (achievementManager != null) {
            Label statsLabel = new Label("--- Cultivation Summary ---", game.getSkin());
            statsLabel.setColor(Color.CYAN);
            table.add(statsLabel).padBottom(5).row();

            // 显示杀敌数
            Label killLabel = new Label("Enemies Defeated: " + achievementManager.getTotalKills(), game.getSkin());
            table.add(killLabel).padBottom(5).row();

            // 显示总获得经验
            Label expLabel = new Label("Spirit Energy Gained: " + achievementManager.getTotalExpGained(), game.getSkin());
            table.add(expLabel).padBottom(10).row();

            // 显示已解锁的成就 ID (可选)
            String achievements = "Achievements: ";
            for (Map.Entry<String, Boolean> entry : achievementManager.getUnlockedStatus().entrySet()) {
                if (entry.getValue()) achievements += entry.getKey() + "  ";
            }
            Label achieveList = new Label(achievements, game.getSkin());
            achieveList.setFontScale(0.8f);
            achieveList.setColor(Color.YELLOW);
            table.add(achieveList).padBottom(30).row();
        }














        // 3.
        if (isVictory) {
            TextButton nextBtn = new TextButton("Next Level", game.getSkin());
            nextBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                   game.goToGame(currentLevel+1,savedStats);

                }
            });
            table.add(nextBtn).width(300).padBottom(20).row();
        } else {
            TextButton retryBtn = new TextButton("Retry Level", game.getSkin());
            retryBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                    game.resetGlobalScore();
                    game.goToGame(1,null);// 重新加载当前关卡
                }
            });
            table.add(retryBtn).width(300).padBottom(20).row();
        }

        // 4. 返回菜单
        TextButton menuBtn = new TextButton("Back to Menu", game.getSkin());
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playClickAndStopMusic();
                game.goToMenu();
            }
        });
        table.add(menuBtn).width(300).row();
    }


    private void playClickAndStopMusic() {
        if (buttonSound != null) buttonSound.play();
        if (resultMusic != null) resultMusic.stop();
    }

    @Override
    public void render(float delta) {
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
    @Override public void dispose() { stage.dispose(); }
}