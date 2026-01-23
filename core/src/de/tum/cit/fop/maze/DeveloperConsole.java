package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-game developer console for testing.
 * It shows a TextField, accepts a command, and executes
 * a predefined action on the player/game.
 */

public class DeveloperConsole {
    private TextField inputField;
    private boolean isVisible = false;
    private Player player;
    private GameScreen gameScreen;
    interface ConsoleCommand {
        void execute();
    }


    private Map<String, ConsoleCommand> commandMap;
    /**
     * Creates the console input field, registers commands, and adds the input to the stage.
     *
     * @param skin UI skin used for the TextField
     * @param stage stage that renders the console input
     * @param player player object affected by commands
     * @param gameScreen game screen used for commands that affect the level (e.g. killall)
     */

    public DeveloperConsole(Skin skin, Stage stage, Player player, GameScreen gameScreen) {
        this.player = player;
        this.gameScreen = gameScreen;

        commandMap = new HashMap<>();
        registerCommands();

        inputField = new TextField("", skin);
        inputField.setMessageText("Enter command (e.g., heal, god)...");
        inputField.setSize(400, 50);
        inputField.setPosition(10, Gdx.graphics.getHeight() - 60);
        inputField.setVisible(false);

        inputField.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') {
                handleCommand(textField.getText());
                textField.setText("");
                toggleConsole();
            }
        });

        stage.addActor(inputField);
    }

    /**
     * Registers all supported commands and connects them to actions.
     * Commands are typed in the TextField .
     */

    private void registerCommands() {
        commandMap.put("heal", () -> {
            player.getStats().heal(1000);
            System.out.println("✅ Command executed: Player healed!");
        });
        commandMap.put("god", () -> {
            player.enableFatalProtection();
            System.out.println(" Command executed: God mode enabled!");
        });
        commandMap.put("key", () -> {
            player.getStats().setHasKey(true);
            System.out.println(" Command executed: Key added!");
        });
        commandMap.put("killall", () -> {
            for (de.tum.cit.fop.maze.enemies.Enemy e : gameScreen.getEnemies()) {
                e.takeDamage(99999);
            }
            System.out.println("Command executed: All enemies killed (XP granted)!");
        });
        commandMap.put("level_up", () -> {
            player.getStats().getExpSystem().gainExp(1000);
            System.out.println(" Command executed: Level Up!");
        });
    }
    /**
     * Parses a text command and executes it if it exists.
     *
     * @param command raw input text from the console
     */
    private void handleCommand(String command) {
        System.out.println("Console input: " + command);
        String key = command.trim().toLowerCase();

        if (commandMap.containsKey(key)) {
            commandMap.get(key).execute();
        } else {
            System.out.println(" Unknown command: " + key);
        }
    }


    /**
     * Toggles console visibility and keyboard focus.
     * When opened, the TextField gets focus so the user can type immediately.
     */

    public void toggleConsole() {
        isVisible = !isVisible;
        inputField.setVisible(isVisible);

        if (isVisible) {
            if (inputField.getStage() != null) {
                inputField.getStage().setKeyboardFocus(inputField);
            }
            inputField.setText("");
        } else {
            if (inputField.getStage() != null) {
                inputField.getStage().setKeyboardFocus(null);
            }
        }
    }
    public boolean isVisible() {
        return isVisible;
    }
}