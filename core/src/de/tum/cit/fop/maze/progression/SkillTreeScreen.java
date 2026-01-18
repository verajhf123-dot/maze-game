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

    private void attemptUpgrade(String type) {
        String skillId = "";

        // 1. 将按钮类型映射为 SkillTree.java 中定义的 ID
        switch (type) {
            case "HP":
                skillId = "health_boost";
                break;
            case "SPEED":
                skillId = "speed_boost";
                break;
            case "ATK":
                skillId = "attack_boost";
                break;
            default:
                System.out.println("Unknown upgrade type: " + type);
                return;
        }

        // 2. 尝试解锁
        // skillTree.unlockSkill() 内部会自动检查：
        //    a. 你的 XP 是否足够 (不够返回 false)
        //    b. 自动扣除 XP
        //    c. 是否已经解锁过 (防止重复购买)
        boolean success = skillTree.unlockSkill(skillId);

        // 3. 根据结果刷新界面
        if (success) {
            System.out.println("Successfully upgraded: " + type);

            // 刷新属性面板
            updateAllStatsLabels();

            // 刷新右上角的 XP 显示 (之前叫 SkillPointsLabel)
            updateSkillPointsLabel();
        } else {
            // 如果失败（XP不够或者已经解锁过了），可以在这里加提示音或者弹窗
            System.out.println("Cannot upgrade " + type + " (Not enough XP or already unlocked)");
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
            // ▼▼▼ 修改：只显示当前 XP ▼▼▼
            String text = "Current XP: " + expSystem.getCurrentExp();
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
            skillPointsLabel.setText(text);
        } else {
            skillPointsLabel.setText("XP System not available");
        }
    }

    private void returnToGame() {
        if (previousScreen != null) {
            // 如果 previousScreen 是 GameScreen，尝试恢复它的状态
            if (previousScreen instanceof de.tum.cit.fop.maze.GameScreen) {
                // 需要在 GameScreen 里加一个 public 方法叫 resumeFromSkillTree() 或者直接访问
                // 这里我们用一种简单粗暴的方法，依靠 GameScreen 的 show() 逻辑
                // 但最好是在这里把 GameScreen 的状态改回 RUNNING

                // 假设你没法直接访问 GameScreen 的私有变量，
                // 那么在 GameScreen.java 的 show() 方法里，你需要确保状态被重置。
            }

            game.setScreen(previousScreen);
            this.dispose();
        } else {
            game.goToGame(1, playerStats);
        }
    }


    @Override
    public void render(float delta) {
        // 1) 清屏
        ScreenUtils.clear(0, 0, 0, 1);

        // ESC or T key to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
            return;
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