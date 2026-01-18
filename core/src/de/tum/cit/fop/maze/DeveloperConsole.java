package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import java.util.HashMap;
import java.util.Map;

// 定义一个简单的接口，代表一个“指令操作”

public class DeveloperConsole {
    private TextField inputField;
    private boolean isVisible = false;
    private Player player;
    private GameScreen gameScreen;




    interface ConsoleCommand {
        void execute();
    }


    private Map<String, ConsoleCommand> commandMap;

    public DeveloperConsole(Skin skin, Stage stage, Player player, GameScreen gameScreen) {
        this.player = player;
        this.gameScreen = gameScreen;

        // 1. 初始化 HashMap
        commandMap = new HashMap<>();
        registerCommands(); // 注册所有指令

        // 2. 创建输入框 (保持不变)
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

    // 🔥 注册指令：把“名字”和“逻辑”存进 Map 里
    private void registerCommands() {
        // 指令 1: Heal
        commandMap.put("heal", () -> {
            player.getStats().heal(1000);
            System.out.println("✅ Command executed: Player healed!");
        });

        // 指令 2: God Mode
        commandMap.put("god", () -> {
            player.enableFatalProtection();
            System.out.println(" Command executed: God mode enabled!");
        });

        // 指令 3: Key
        commandMap.put("key", () -> {
            player.getStats().setHasKey(true);
            System.out.println(" Command executed: Key added!");
        });

        // 指令 4: Kill All
        commandMap.put("killall", () -> {
            for (de.tum.cit.fop.maze.enemies.Enemy e : gameScreen.getEnemies()) {
                e.takeDamage(99999); // 造成巨大伤害，强制死亡
            }
            System.out.println("Command executed: All enemies killed (XP granted)!");
        });

        commandMap.put("level_up", () -> {
            player.getStats().getExpSystem().gainExp(1000);
            System.out.println(" Command executed: Level Up!");
        });
    }

    private void handleCommand(String command) {
        System.out.println("Console input: " + command);
        String key = command.trim().toLowerCase();

        if (commandMap.containsKey(key)) {
            commandMap.get(key).execute(); // 执行对应的 Lambda 表达式
        } else {
            System.out.println(" Unknown command: " + key);
        }
    }

    public void toggleConsole() {
        isVisible = !isVisible;
        inputField.setVisible(isVisible);

        if (isVisible) {
            if (inputField.getStage() != null) {
                inputField.getStage().setKeyboardFocus(inputField);
            }
            inputField.setText(""); // 每次打开自动清空，方便输入
        } else {
            // 关闭时释放焦点
            if (inputField.getStage() != null) {
                inputField.getStage().setKeyboardFocus(null);
            }
        }
    }
    public boolean isVisible() {
        return isVisible;
    }
}