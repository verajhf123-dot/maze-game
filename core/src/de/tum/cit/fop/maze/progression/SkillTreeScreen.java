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

/**
 * Screen for displaying and interacting with the skill tree UI.
 * Allows the player to upgrade stats using experience points.
 */
public class SkillTreeScreen implements Screen {

    // Core game and data references
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SkillTree skillTree;
    private final PlayerStats playerStats;
    private final ExperienceSystem expSystem;
    private final Screen previousScreen;

    // UI labels for displaying values
    private Label skillPointsLabel;
    private Label hpLabel;
    private Label speedLabel;
    private Label atkLabel;

    // Rendering resources
    private SpriteBatch batch;
    private Texture menuBg;
    private Texture hpIcon;
    private Texture speedIcon;
    private Texture atkIcon;

    // Custom UI color
    private static final Color MINT_COLOR = new Color(0.96f, 1.0f, 0.98f, 1.0f);

    /**
     * Creates the skill tree screen.
     */
    public SkillTreeScreen(MazeRunnerGame game, PlayerStats playerStats, Screen previousScreen) {
        this.game = game;
        this.playerStats = playerStats;
        this.skillTree = playerStats.getSkillTree();
        this.expSystem = playerStats.getExpSystem();
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        this.skillPointsLabel = new Label("", game.getSkin());
    }

    @Override
    public void show() {
        // Initialize rendering resources
        batch = new SpriteBatch();
        menuBg = new Texture(Gdx.files.internal("sktbg3.png"));

        // Load stat icons
        hpIcon = new Texture(Gdx.files.internal("SkillTree/HP.png"));
        speedIcon = new Texture(Gdx.files.internal("SkillTree/speed.png"));
        atkIcon = new Texture(Gdx.files.internal("SkillTree/attack.png"));

        // Set stage as input processor
        Gdx.input.setInputProcessor(stage);

        // Main layout table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        stage.addActor(mainTable);

        // Title label
        Label title = new Label("SKILL TREE", game.getSkin(), "title");
        title.setColor(new Color(0.96f, 0.96f, 0.86f, 1f));
        title.setFontScale(1.2f);
        mainTable.add(title).padBottom(40).row();

        // XP display
        updateSkillPointsLabel();
        skillPointsLabel.setFontScale(1.2f);
        mainTable.add(skillPointsLabel).padBottom(40).row();

        // Table for stat upgrade icons
        Table statsTable = new Table();
        statsTable.defaults().pad(30);

        float iconSize = 120f;

        // HP upgrade icon and click handler
        Table hpContainer = new Table();
        Image hpImage = new Image(hpIcon);
        hpImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
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

        // Speed upgrade icon and click handler
        Table speedContainer = new Table();
        Image speedImage = new Image(speedIcon);
        speedImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
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

        // Attack upgrade icon and click handler
        Table atkContainer = new Table();
        Image atkImage = new Image(atkIcon);
        atkImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
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

        mainTable.add(statsTable).padBottom(30).row();

        // Initialize displayed stat values
        updateAllStatsLabels();

        // Hint label for returning to game
        Label hintLabel = new Label("Press T or ESC to return", game.getSkin());
        hintLabel.setFontScale(1.0f);
        mainTable.add(hintLabel).padTop(20);
    }

    /**
     * Attempts to unlock a stat upgrade using the skill tree.
     */
    private void attemptUpgrade(String type) {
        String skillId = "";

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

        boolean success = skillTree.unlockSkill(skillId);

        if (success) {
            updateAllStatsLabels();
            updateSkillPointsLabel();
        }
    }

    /**
     * Updates all displayed stat labels.
     */
    private void updateAllStatsLabels() {
        if (skillTree != null) {
            hpLabel.setText("HP +" + (int)skillTree.getTotalHealthBonus());
            speedLabel.setText("Speed +" + (int)(skillTree.getTotalSpeedBonus() * 100) + "%");
            atkLabel.setText("ATK +" + (int)skillTree.getTotalAttackBonus());
        }
    }

    /**
     * Updates the XP display label.
     */
    private void updateSkillPointsLabel() {
        if (expSystem != null) {
            skillPointsLabel.setText("Current XP: " + expSystem.getCurrentExp());
        }
    }

    /**
     * Returns to the previous screen or the game screen.
     */
    private void returnToGame() {
        if (previousScreen != null) {
            game.setScreen(previousScreen);
            this.dispose();
        } else {
            game.goToGame(1, playerStats);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Handle return input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
            return;
        }

        // Draw background
        batch.begin();
        if (menuBg != null) {
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        // Update and render UI
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