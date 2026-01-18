package de.tum.cit.fop.maze.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.PlayerStats;

public class SkillTreeScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SkillTree skillTree;
    private final PlayerStats playerStats;
    private final ExperienceSystem expSystem;
    private final Screen previousScreen;

    private Label skillPointsLabel;

    // 将这些 Label 提升为类成员，以便在点击后更新它们
    private Label hpLabel;
    private Label speedLabel;
    private Label atkLabel;

    private SpriteBatch batch;
    private Texture menuBg;
    private Texture hpIcon;
    private Texture speedIcon;
    private Texture atkIcon;

    private static final Color MINT_COLOR = new Color(0.96f, 1.0f, 0.98f, 1.0f);

    public SkillTreeScreen(MazeRunnerGame game, PlayerStats playerStats, Screen previousScreen) {
        this.game = game;
        this.playerStats = playerStats;
        this.skillTree = playerStats.getSkillTree();
        this.expSystem = playerStats.getExpSystem();
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        this.skillPointsLabel = new Label("", game.getSkin());
        // statsLabel 不再需要，因为我们直接更新三个具体的 Label
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("sktbg3.png"));

        hpIcon = new Texture(Gdx.files.internal("SkillTree/HP.png"));
        speedIcon = new Texture(Gdx.files.internal("SkillTree/speed.png"));
        atkIcon = new Texture(Gdx.files.internal("SkillTree/attack.png"));

        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center(); // 让整个布局居中
        stage.addActor(mainTable);

        // Title
        Label title = new Label("SKILL TREE", game.getSkin(), "title");
        title.setColor(new Color(0.96f, 0.96f, 0.86f, 1f));
        title.setFontScale(1.2f);
        mainTable.add(title).padBottom(40).row();

        // 技能点信息 (放在上面更显眼)
        updateSkillPointsLabel();
        skillPointsLabel.setFontScale(1.2f);
        mainTable.add(skillPointsLabel).padBottom(40).row();

        Table statsTable = new Table();
        statsTable.defaults().pad(30); // 减小间距，防止超出屏幕

        // 修改：将图标大小从 500 改为 120，确保能显示完全
        float iconSize = 120f;

        // --- HP 容器 ---
        Table hpContainer = new Table();
        Image hpImage = new Image(hpIcon);
        hpImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        // 添加点击事件
        hpImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade("HP");
            }
        });
        hpContainer.add(hpImage).size(iconSize, iconSize).row();

        Label clickHint1 = new Label("Click to Add", game.getSkin());
        clickHint1.setFontScale(0.8f);
        hpContainer.add(clickHint1).padTop(10).row();

        hpLabel = new Label("", game.getSkin());
        hpLabel.setColor(Color.RED);
        hpLabel.setFontScale(1.2f);
        hpContainer.add(hpLabel).padTop(5);
        statsTable.add(hpContainer);

        // --- Speed 容器 ---
        Table speedContainer = new Table();
        Image speedImage = new Image(speedIcon);
        speedImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        // 添加点击事件
        speedImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade("SPEED");
            }
        });
        speedContainer.add(speedImage).size(iconSize, iconSize).row();

        Label clickHint2 = new Label("Click to Add", game.getSkin());
        clickHint2.setFontScale(0.8f);
        speedContainer.add(clickHint2).padTop(10).row();

        speedLabel = new Label("", game.getSkin());
        speedLabel.setColor(MINT_COLOR);
        speedLabel.setFontScale(1.2f);
        speedContainer.add(speedLabel).padTop(5);
        statsTable.add(speedContainer);

        // --- ATK 容器 ---
        Table atkContainer = new Table();
        Image atkImage = new Image(atkIcon);
        atkImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        // 添加点击事件
        atkImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade("ATK");
            }
        });
        atkContainer.add(atkImage).size(iconSize, iconSize).row();

        Label clickHint3 = new Label("Click to Add", game.getSkin());
        clickHint3.setFontScale(0.8f);
        atkContainer.add(clickHint3).padTop(10).row();

        atkLabel = new Label("", game.getSkin());
        atkLabel.setColor(Color.ORANGE);
        atkLabel.setFontScale(1.2f);
        atkContainer.add(atkLabel).padTop(5);
        statsTable.add(atkContainer);

        // 将 statsTable 加入主表
        mainTable.add(statsTable).padBottom(30).row();

        // 初始化 Label 的文字
        updateAllStatsLabels();

        // 提示信息
        Label hintLabel = new Label("Press T or ESC to return", game.getSkin());
        hintLabel.setFontScale(1.0f);
        mainTable.add(hintLabel).padTop(20);
    }

    // 新增：处理升级的核心逻辑
    private void attemptUpgrade(String type) {
        if (expSystem.getSkillPoints() > 0) {
            boolean success = false;

            // 注意：这里需要调用你 SkillTree 类中具体的升级方法
            // 因为我看不到你的 SkillTree 代码，所以我写了通用的逻辑。
            // 如果你的方法名不一样（比如叫 unlockNode），请在这里修改！
            switch (type) {
                case "HP":
                    // 假设你的 SkillTree 有 addHealth 或类似的方法
                    // skillTree.unlockHealthNode();
                    // 如果没有特定方法，这里只是模拟成功：
                    success = true;
                    System.out.println("Upgraded HP!");
                    break;
                case "SPEED":
                    // skillTree.unlockSpeedNode();
                    success = true;
                    System.out.println("Upgraded Speed!");
                    break;
                case "ATK":
                    // skillTree.unlockAttackNode();
                    success = true;
                    System.out.println("Upgraded Attack!");
                    break;
            }

            if (success) {
                expSystem.useSkillPoint(); // 扣除技能点
                updateAllStatsLabels();      // 刷新界面数值
                updateSkillPointsLabel();    // 刷新剩余技能点
            }
        } else {
            System.out.println("Not enough skill points!");
        }
    }

    private void updateAllStatsLabels() {
        if (skillTree != null) {
            hpLabel.setText("HP +" + (int)skillTree.getTotalHealthBonus());
            speedLabel.setText("Speed +" + (int)(skillTree.getTotalSpeedBonus() * 100) + "%");
            atkLabel.setText("ATK +" + (int)skillTree.getTotalAttackBonus());
        }
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

    private void returnToGame() {
        // 确保正确返回到之前的屏幕
        if (previousScreen != null) {
            game.setScreen(previousScreen);
            this.dispose();
        } else {
            Gdx.app.error("SkillTreeScreen", "No previous screen found to return to!");
        }
    }


    @Override
    public void render(float delta) {
        // 1) 清屏
        ScreenUtils.clear(0, 0, 0, 1);

        // ESC or T key to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
        }

        // 2) 画背景
        batch.begin();
        if (menuBg != null) {
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        // 3) 画 UI
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
        if (hpIcon != null) hpIcon.dispose();
        if (speedIcon != null) speedIcon.dispose();
        if (atkIcon != null) atkIcon.dispose();
        if (menuBg != null) menuBg.dispose();
        if (batch != null) batch.dispose();
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