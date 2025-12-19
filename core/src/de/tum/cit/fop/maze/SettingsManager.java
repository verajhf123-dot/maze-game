package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

public class SettingsManager {

    private static final String PREF_NAME = "MazeRunnerSettings";
    private final Preferences prefs;

    // action -> keycode
    private final Map<String, Integer> keyBindings = new HashMap<>();

    public SettingsManager() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        loadDefaults();
        loadFromPreferences();
    }

    /** 默认按键绑定 */
    private void loadDefaults() {
        keyBindings.put("move_up", Input.Keys.W);
        keyBindings.put("move_down", Input.Keys.S);
        keyBindings.put("move_left", Input.Keys.A);
        keyBindings.put("move_right", Input.Keys.D);
        keyBindings.put("run", Input.Keys.SHIFT_LEFT);
    }

    /** 从 Preferences 读取（覆盖默认值） */
    private void loadFromPreferences() {
        for (String action : keyBindings.keySet()) {
            if (prefs.contains(action)) {
                keyBindings.put(action, prefs.getInteger(action));
            }
        }
    }

    /** 给 PlayerController 用：返回 keycode */
    public int getKeyCode(String action) {
        Integer key = keyBindings.get(action);
        if (key == null) {
            // 防止 NPE，兜底
            return Input.Keys.UNKNOWN;
        }
        return key;
    }

    /** 给 SettingsScreen 用：返回可读的按键名字 */
    public String getKey(String action) {
        Integer key = keyBindings.get(action);
        if (key == null) {
            return "UNBOUND";
        }
        return Input.Keys.toString(key);
    }

    /** 修改按键绑定（以后设置界面用） */
    public void setKey(String action, int keycode) {
        keyBindings.put(action, keycode);
        prefs.putInteger(action, keycode);
        prefs.flush();
    }
}