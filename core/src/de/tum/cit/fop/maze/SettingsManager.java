package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/**Manages user settings such as volume and key bindings.
 *Settings are stored persistently using LibGDX Preferences.
 */


public class SettingsManager {

    private static final String PREF_NAME = "MazeRunnerGame_Settings";


    private final Preferences preferences;

    /**
     * Creates a SettingsManager and loads stored preferences.
     */

    public SettingsManager() {
        this.preferences = Gdx.app.getPreferences(PREF_NAME);
    }



    public float getVolume() {
        return preferences.getFloat("volume", 0.5f);
    }

    /**
     *Sets and saves the volume value.
     *
     */


    public void setVolume(float volume) {
        preferences.putFloat("volume", volume);
        preferences.flush();
    }

    /**
     * Returns the key binding for a given action.
     * If no custom key is stored, a default key is used.
     *
     * @param action action name (e.g. "move_up", "move_down")
     * @return keycode associated with the action
     */


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

    /**
     *Saves all pending preference changes.
     */

    public void save() {
        preferences.flush();
    }
}