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

    private Table skillTable;
    private Label skillPointsLabel;
    private Label statsLabel;

    public SkillTreeScreen(MazeRunnerGame game, PlayerStats playerStats) {
        this.game = game;
        this.playerStats = playerStats;
        this.skillTree = playerStats.getSkillTree();
        this.expSystem = playerStats.getExpSystem();
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Title
        Label title = new Label("SKILL TREE", game.getSkin(), "title");
        mainTable.add(title).padBottom(30).colspan(2).row();

        // Skill points display
        skillPointsLabel = new Label("", game.getSkin());
        updateSkillPointsLabel();
        mainTable.add(skillPointsLabel).padBottom(20).colspan(2).row();

        // Stats display
        statsLabel = new Label("", game.getSkin());
        updateStatsLabel();
        mainTable.add(statsLabel).padBottom(30).colspan(2).row();

        // Create skill buttons
        skillTable = new Table();
        skillTable.defaults().width(200).height(120).pad(10);

        refreshSkillTable();

        ScrollPane scrollPane = new ScrollPane(skillTable, game.getSkin());
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFadeScrollBars(false);

        mainTable.add(scrollPane).colspan(2).width(800).height(400).padBottom(20).row();

        // Back button
        TextButton backButton = new TextButton("Back to Game", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                returnToGame();
            }
        });

        // Reset button (for testing)
        TextButton resetButton = new TextButton("Reset Skills (Test)", game.getSkin());
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skillTree.reset();
                playerStats.applySkillEffects();
                refreshSkillTable();
                System.out.println("All skills reset for testing");
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(backButton).padRight(20);
        buttonTable.add(resetButton);

        mainTable.add(buttonTable).padTop(20).colspan(2);
    }

    private void refreshSkillTable() {
        skillTable.clear();

        if (skillTree == null) {
            skillTable.add(new Label("Skill tree not initialized", game.getSkin()));
            return;
        }

        Map<String, SkillTree.SkillNode> allNodes = skillTree.getAllNodes();

        // Create a grid layout (2 rows, multiple columns)
        int itemsPerRow = 4;
        int currentItem = 0;

        Table currentRow = new Table();
        skillTable.add(currentRow).row();

        for (SkillTree.SkillNode node : allNodes.values()) {
            if (currentItem > 0 && currentItem % itemsPerRow == 0) {
                currentRow = new Table();
                skillTable.add(currentRow).padTop(10).row();
            }

            currentRow.add(createSkillButton(node)).pad(5);
            currentItem++;
        }

        updateSkillPointsLabel();
        updateStatsLabel();
    }

    private Button createSkillButton(SkillTree.SkillNode node) {
        TextButton button;

        // Build button text with skill information
        StringBuilder buttonText = new StringBuilder();
        buttonText.append(node.name).append("\n");

        // Add key binding if available
        if (node.bindKey != null && !node.bindKey.isEmpty()) {
            buttonText.append("[").append(node.bindKey).append("]\n");
        }

        buttonText.append("Cost: ").append(node.cost).append(" XP\n");

        // Add skill effects
        if (node.healthBonus > 0) {
            buttonText.append("HP +").append((int)node.healthBonus).append("\n");
        }
        if (node.attackBonus > 0) {
            buttonText.append("ATK +").append((int)node.attackBonus).append("\n");
        }
        if (node.speedBonus > 0) {
            buttonText.append("SPD +").append((int)(node.speedBonus * 100)).append("%\n");
        }

        // Add skill specific info
        if (node.skillValue > 0) {
            if ("heal".equals(node.skillType)) {
                buttonText.append("Heal: ").append((int)node.skillValue).append("\n");
            } else if ("fireball".equals(node.skillType)) {
                buttonText.append("Damage: ").append((int)node.skillValue).append("\n");
            } else if ("lightning".equals(node.skillType)) {
                buttonText.append("Chain: ").append((int)node.skillValue).append("\n");
            } else if ("shield".equals(node.skillType)) {
                buttonText.append("Shield: ").append((int)node.skillValue).append("\n");
            }
        }

        if (node.skillCooldown > 0 && node.skillType != null && !node.skillType.isEmpty()) {
            buttonText.append("CD: ").append(node.skillCooldown).append("s");
        }

        if (node.unlocked) {
            button = new TextButton(buttonText.toString() + "\n[UNLOCKED]", game.getSkin());
            button.setColor(Color.GREEN);
            button.setDisabled(true);

            // Add tooltip for unlocked skills
            button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    showTooltip(node, true);
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor toActor) {
                    hideTooltip();
                }
            });
        } else if (skillTree.canUnlockSkill(node.id)) {
            button = new TextButton(buttonText.toString(), game.getSkin());
            button.setColor(Color.YELLOW);

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (skillTree.unlockSkill(node.id)) {
                        // Update player stats
                        if (playerStats != null) {
                            playerStats.applySkillEffects();
                        }
                        refreshSkillTable();
                        System.out.println("Unlocked skill: " + node.name);

                        // Show key binding info
                        if (node.bindKey != null && !node.bindKey.isEmpty()) {
                            System.out.println("Press " + node.bindKey + " to use this skill in game!");
                        }
                    }
                }
            });

            // Add tooltip for unlockable skills
            button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    showTooltip(node, false);
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor toActor) {
                    hideTooltip();
                }
            });
        } else {
            button = new TextButton(buttonText.toString(), game.getSkin());
            button.setColor(Color.GRAY);
            button.setDisabled(true);

            // Add tooltip for locked skills
            button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    showTooltip(node, false);
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor toActor) {
                    hideTooltip();
                }
            });
        }

        return button;
    }

    private void showTooltip(SkillTree.SkillNode node, boolean isUnlocked) {
        // You can implement a proper tooltip system here
        // For now, just print to console
        System.out.println("\n=== SKILL INFO ===");
        System.out.println("Name: " + node.name);
        System.out.println("Description: " + node.description);

        if (node.bindKey != null && !node.bindKey.isEmpty()) {
            System.out.println("Key: " + node.bindKey);
        }

        System.out.println("Cost: " + node.cost + " XP");
        System.out.println("Status: " + (isUnlocked ? "UNLOCKED" : "LOCKED"));

        if (node.healthBonus > 0) System.out.println("Health Bonus: +" + node.healthBonus);
        if (node.attackBonus > 0) System.out.println("Attack Bonus: +" + node.attackBonus);
        if (node.speedBonus > 0) System.out.println("Speed Bonus: +" + (node.speedBonus * 100) + "%");

        if (node.skillValue > 0) {
            if ("heal".equals(node.skillType)) {
                System.out.println("Healing Amount: " + node.skillValue);
            } else if ("fireball".equals(node.skillType)) {
                System.out.println("Fireball Damage: " + node.skillValue);
            } else if ("lightning".equals(node.skillType)) {
                System.out.println("Lightning Damage: " + node.skillValue);
            } else if ("shield".equals(node.skillType)) {
                System.out.println("Shield Value: " + node.skillValue);
            }
        }

        if (node.skillCooldown > 0) {
            System.out.println("Cooldown: " + node.skillCooldown + " seconds");
        }

        if (!isUnlocked) {
            System.out.println("\nRequirements:");
            System.out.println("- " + expSystem.getSkillPoints() + " skill point(s) available");
            System.out.println("- " + expSystem.getCurrentExp() + "/" + node.cost + " XP");

            if (expSystem.getCurrentExp() < node.cost) {
                System.out.println("Need " + (node.cost - expSystem.getCurrentExp()) + " more XP");
            }
        }
    }

    private void hideTooltip() {
        // Clear console or hide tooltip
        System.out.println("\n");
    }

    private void updateSkillPointsLabel() {
        if (expSystem != null) {
            String text = "XP: " + expSystem.getCurrentExp() +
                    " | Skill Points: " + expSystem.getSkillPoints() +
                    " | Level: " + expSystem.getCurrentLevel();
            skillPointsLabel.setText(text);
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
        }
    }

    private void returnToGame() {
        // Return to current game level
        // You might want to track the current level
        game.goToGame(1); // Default to level 1 for now
    }

    @Override
    public void render(float delta) {
        // ESC key to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}