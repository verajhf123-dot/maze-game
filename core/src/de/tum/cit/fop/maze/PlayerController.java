package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;

/**
 * Handles player input based on the current key bindings from SettingsManager.
 * This class only checks whether a specific action key is currently pressed.
 */

public class PlayerController {
/**
 * Creates a controller that reads key bindings from the settings.
 */
    private SettingsManager settings;

    public PlayerController(SettingsManager sm) {
        this.settings = sm;
    }


    public boolean up() {
        return Gdx.input.isKeyPressed(settings.getKey("move_up"));
    }


    public boolean down() {
        return Gdx.input.isKeyPressed(settings.getKey("move_down"));
    }

    public boolean left() {
        return Gdx.input.isKeyPressed(settings.getKey("move_left"));
    }

    public boolean right() {
        return Gdx.input.isKeyPressed(settings.getKey("move_right"));
    }

    public boolean run() {
        return Gdx.input.isKeyPressed(settings.getKey("run"));
    }
}
