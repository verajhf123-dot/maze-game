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
 * SkillTreeScreen displays the player's skill tree UI,
 * allows upgrading HP, Speed, and Attack stats,
 * and integrates with Q/E/R skill system.
 */
public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final SkillTree skillTree;
    private final PlayerStats playerStats;
    private final ExperienceSystem expSystem;
    private final Screen previousScreen;

    private Label skillPointsLabel;
    private Label hpLabel;
    private Label speedLabel;
    private Label atkLabel;

    private SpriteBatch batch;
    private Texture menuBg;
    private Texture hpIcon;
    private Texture speedIcon;
    private Texture atkIcon;

    private static final Color MINT_COLOR = new Color(0.96f, 1.0f, 0.98f, 1.0f);

    // ------------------- Constructor -------------------
    public SkillTreeScreen(MazeRunnerGame game, PlayerStats playerStats, Screen previousScreen) {
        this.game = game;
        this.playerStats = playerStats;
        this.skillTree = playerStats.getSkillTree();
        this.expSystem = playerStats.getExpSystem();
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        skillPointsLabel = new Label("", game.getSkin());
    }

    // ------------------- Show / UI Setup -------------------
    @Override
    public void show() {
        batch = new SpriteBatch();

        // Load textures for menu background and stat icons
        menuBg = new Texture(Gdx.files.internal("sktbg3.png"));
        hpIcon = new Texture(Gdx.files.internal("SkillTree/HP.png"));
        speedIcon = new Texture(Gdx.files.internal("SkillTree/speed.png"));
        atkIcon = new Texture(Gdx.files.internal("SkillTree/attack.png"));

        Gdx.input.setInputProcessor(stage);

        // Main table to organize UI
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        stage.addActor(mainTable);

        // Title label
        Label title = new Label("SKILL TREE", game.getSkin(), "title");
        title.setColor(new Color(0.96f, 0.96f, 0.86f, 1f));
        title.setFontScale(1.2f);
        mainTable.add(title).padBottom(40).row();

        // XP / skill points label
        updateSkillPointsLabel();
        skillPointsLabel.setFontScale(1.2f);
        mainTable.add(skillPointsLabel).padBottom(40).row();

        // Stats table: HP, Speed, Attack
        Table statsTable = new Table();
        statsTable.defaults().pad(30);
        float iconSize = 120f;

        // HP stat
        hpLabel = createStatColumn(statsTable, hpIcon, "HP", Color.RED, iconSize);

        // Speed stat
        speedLabel = createStatColumn(statsTable, speedIcon, "SPEED", MINT_COLOR, iconSize);

        // Attack stat
        atkLabel = createStatColumn(statsTable, atkIcon, "ATK", Color.ORANGE, iconSize);

        mainTable.add(statsTable).padBottom(30).row();

        updateAllStatsLabels();

        // Hint label
        Label hintLabel = new Label("Press T or ESC to return", game.getSkin());
        hintLabel.setFontScale(1.0f);
        mainTable.add(hintLabel).padTop(20);
    }

    /**
     * Helper method to create a stat column (icon + click hint + value label)
     */
    private Label createStatColumn(Table parentTable, Texture iconTexture, String type, Color valueColor, float iconSize) {
        Table container = new Table();
        Image icon = new Image(iconTexture);
        icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        icon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(type);
            }
        });
        container.add(icon).size(iconSize, iconSize).row();

        Label clickHint = new Label("Click to Add", game.getSkin());
        clickHint.setFontScale(0.8f);
        container.add(clickHint).padTop(10).row();

        Label valueLabel = new Label("", game.getSkin());
        valueLabel.setColor(valueColor);
        valueLabel.setFontScale(1.2f);
        container.add(valueLabel).padTop(5);

        parentTable.add(container);
        return valueLabel;
    }

    // ------------------- Skill Upgrade Logic -------------------
    private void attemptUpgrade(String type) {
        String skillId;
        switch (type) {
            case "HP": skillId = "health_boost"; break;
            case "SPEED": skillId = "speed_boost"; break;
            case "ATK": skillId = "attack_boost"; break;
            default:
                System.out.println("Unknown upgrade type: " + type);
                return;
        }

        boolean success = skillTree.unlockSkill(skillId);

        if (success) {
            System.out.println("Successfully upgraded: " + type);
            updateAllStatsLabels();
            updateSkillPointsLabel();
        } else {
            System.out.println("Cannot upgrade " + type + " (Not enough XP or already unlocked)");
        }
    }

    // Update the stat labels with current totals from skill tree
    private void updateAllStatsLabels() {
        if (skillTree != null) {
            hpLabel.setText("HP +" + (int) skillTree.getTotalHealthBonus());
            speedLabel.setText("Speed +" + (int) (skillTree.getTotalSpeedBonus() * 100) + "%");
            atkLabel.setText("ATK +" + (int) skillTree.getTotalAttackBonus());
        }
    }

    // Update XP / skill points label
    private void updateSkillPointsLabel() {
        if (expSystem != null) {
            skillPointsLabel.setText("Current XP: " + expSystem.getCurrentExp());
        } else {
            skillPointsLabel.setText("XP System not available");
        }
    }

    // ------------------- Return / Navigation -------------------
    private void returnToGame() {
        if (previousScreen != null) {
            game.setScreen(previousScreen);
            dispose();
        } else {
            game.goToGame(1, playerStats);
        }
    }

    // ------------------- Screen Methods -------------------
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            returnToGame();
            return;
        }

        batch.begin();
        if (menuBg != null) {
            batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

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