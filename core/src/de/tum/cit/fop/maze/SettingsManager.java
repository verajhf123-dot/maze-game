package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;


public class SettingsManager {

    // 存档文件的名字，通常保存在用户的主目录下 (例如 ~/.prefs/)
    private static final String PREF_NAME = "MazeRunnerGame_Settings";

    private final Preferences preferences;

    public SettingsManager() {
        // 获取 LibGDX 的 Preferences 实例
        this.preferences = Gdx.app.getPreferences(PREF_NAME);
    }


    public float getVolume() {
        return preferences.getFloat("volume", 0.5f);
    }


    public void setVolume(float volume) {
        preferences.putFloat("volume", volume);
        preferences.flush(); // 强制写入硬盘
    }


    public int getKey(String action) {
        int defaultKey;

        // 定义默认按键
        switch (action) {
            case "move_up":
                defaultKey = Input.Keys.W;
                break;
            case "move_down":
                defaultKey = Input.Keys.S;
                break;
            case "move_left":
                defaultKey = Input.Keys.A;
                break;
            case "move_right":
                defaultKey = Input.Keys.D;
                break;
            case "run":
                defaultKey = Input.Keys.SHIFT_LEFT;
                break;
            default:
                defaultKey = Input.Keys.UNKNOWN;
                break;
        }

        // 从文件中读取，如果没存过就用 defaultKey
        return preferences.getInteger(action, defaultKey);
    }


    public void setKey(String action, int keycode) {
        preferences.putInteger(action, keycode);
        preferences.flush(); // 强制写入硬盘
    }

    public void save() {
        preferences.flush();
    }
}