package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // 需要导入
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils; // 建议使用 ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;
import java.util.Map;
import java.util.Map.Entry;
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

    // 按钮样式资源
    private Texture buttonBg;
    private TextButton.TextButtonStyle commonButtonStyle;

    // 新增：背景资源
    private SpriteBatch batch;
    private Texture menuBg;

    public ResultScreen(MazeRunnerGame game, boolean isVictory, int currentLevel, int score, PlayerStats stats, AchievementManager achievementManager) {
        this.game = game;
        this.isVictory = isVictory;
        this.currentLevel = currentLevel;
        this.score = score;
        this.savedStats = stats;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.achievementManager = achievementManager;
    }

    /**
     * 创建统一按钮样式的方法
     */
    private void createButtonStyle() {
        commonButtonStyle = new TextButton.TextButtonStyle();

        BitmapFont baseFont = game.getSkin().getFont("font");
        if (baseFont == null) baseFont = new BitmapFont();
        commonButtonStyle.font = baseFont;

        // 设置字体颜色
        commonButtonStyle.fontColor = Color.WHITE;
        commonButtonStyle.downFontColor = Color.LIGHT_GRAY;

        try {
            // 加载按钮背景图
            if (buttonBg == null) {
                buttonBg = new Texture(Gdx.files.internal("button2.png"));
            }
            TextureRegionDrawable drawable = new TextureRegionDrawable(buttonBg);

            commonButtonStyle.up = drawable;
            commonButtonStyle.down = drawable.tint(Color.LIGHT_GRAY);

        } catch (Exception e) {
            Gdx.app.log("ResultScreen", "Button texture (button2.png) not found! Reverting to default.");
            commonButtonStyle = game.getSkin().get(TextButton.TextButtonStyle.class);
            commonButtonStyle.fontColor = Color.BLACK;
        }
    }

    @Override
    public void show() {
        // 新增：加载背景资源
        batch = new SpriteBatch();
        try {
            menuBg = new Texture(Gdx.files.internal("menu_bg.png"));
        } catch (Exception e) {
            Gdx.app.log("ResultScreen", "Background texture not found!");
        }

        // 初始化按钮样式
        createButtonStyle();

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

        // 3. 按钮部分 (已修改样式)
        if (isVictory) {
            // 使用 commonButtonStyle
            TextButton nextBtn = new TextButton("Next Level", commonButtonStyle);
            nextBtn.getLabel().setFontScale(1.1f); // 字体放大
            nextBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                    game.goToGame(currentLevel+1,savedStats);

                }
            });
            // 调整尺寸以适应图片
            table.add(nextBtn).width(350).height(100).padBottom(20).row();
        } else {
            // 使用 commonButtonStyle
            TextButton retryBtn = new TextButton("Retry Level", commonButtonStyle);
            retryBtn.getLabel().setFontScale(1.1f); // 字体放大
            retryBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playClickAndStopMusic();
                    game.resetGlobalScore();
                    game.goToGame(1,null);// 重新加载当前关卡
                }
            });
            // 调整尺寸以适应图片
            table.add(retryBtn).width(350).height(100).padBottom(20).row();
        }

        // 4. 返回菜单 (已修改样式)
        TextButton menuBtn = new TextButton("Back to Menu", commonButtonStyle);
        menuBtn.getLabel().setFontScale(1.1f); // 字体放大
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playClickAndStopMusic();
                game.goToMenu();
            }
        });
        // 调整尺寸以适应图片
        table.add(menuBtn).width(350).height(100).row();
    }


    private void playClickAndStopMusic() {
        if (buttonSound != null) buttonSound.play();
        if (resultMusic != null) resultMusic.stop();
    }

    @Override
    public void render(float delta) {
        // 1. 清屏
        ScreenUtils.clear(0, 0, 0, 1);

        // 2. 绘制背景 (新增)
        if (batch != null && menuBg != null) {
            batch.begin();
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        }

        // 3. 绘制 UI
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
    @Override public void dispose() {
        stage.dispose();
        // 记得释放按钮资源
        if (buttonBg != null) buttonBg.dispose();
        // 新增：释放背景资源
        if (menuBg != null) menuBg.dispose();
        if (batch != null) batch.dispose();
    }
}