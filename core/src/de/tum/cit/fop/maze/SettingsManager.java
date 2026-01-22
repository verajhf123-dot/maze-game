package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;


public class SettingsManager {

    private static final String PREF_NAME = "MazeRunnerGame_Settings";


    private final Preferences preferences;

    public SettingsManager() {
        this.preferences = Gdx.app.getPreferences(PREF_NAME);
    }


    public float getVolume() {
        return preferences.getFloat("volume", 0.5f);
    }


    public void setVolume(float volume) {
        preferences.putFloat("volume", volume);
        preferences.flush();
    }


    public int getKey(String action) {
        int defaultKey;

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


        return preferences.getInteger(action, defaultKey);
    }


    public void setKey(String action, int keycode) {
        preferences.putInteger(action, keycode);
        preferences.flush();
    }

    public void save() {
        preferences.flush();
    }
}