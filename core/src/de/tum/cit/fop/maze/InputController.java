package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Handles keyboard input and converts it into simple movement states.
 *
 * InputController reads raw keyboard input every frame and translates it into
 * boolean flags such as up, down, left, right and run. These flags describe the
 * player's intention for the current frame and are used by GameScreen and
 * Player logic.
 *
 * Key bindings are loaded from SettingsManager. If no custom bindings are set,
 * default keys (WASD, Shift) are used.
 */


public class InputController {
    public boolean up, down, left, right;
    public boolean run;
    public float zoomChange = 0;
    private final SettingsManager settingsManager;
    private int keyUp, keyDown, keyLeft, keyRight, keyRun;//from SettingsManager

    /**
     *Creates an InputController using key bindings from the settings manager.
     * @param settingsManager used to load key bindings
     */

    public InputController(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        updateKeyBindings();
    }

    /**
     *Reloads key bindings from the settings manager.
     */

    public void updateKeyBindings() {
        keyUp = settingsManager.getKey("move_up");
        keyDown = settingsManager.getKey("move_down");
        keyLeft = settingsManager.getKey("move_left");
        keyRight = settingsManager.getKey("move_right");
        keyRun = settingsManager.getKey("run");
    }

    /**
     * Updates the input state for the current frame by reading keyboard input.
     * All movement flags are reset and recalculated each frame.
     */
    public void update() {
        up = false;
        down = false;
        left = false;
        right = false;
        run = false;
        zoomChange = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            up = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            down = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            left = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            right = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            run = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            zoomChange = -0.01f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            zoomChange = 0.01f;
        }


    }
}