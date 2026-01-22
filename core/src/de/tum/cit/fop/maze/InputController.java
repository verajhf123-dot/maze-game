package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
    public boolean up, down, left, right;
    public boolean run;
    public float zoomChange = 0;
    private final SettingsManager settingsManager;
    private int keyUp, keyDown, keyLeft, keyRight, keyRun;



    public InputController(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        updateKeyBindings();
    }


    public void updateKeyBindings() {
        keyUp = settingsManager.getKey("move_up");
        keyDown = settingsManager.getKey("move_down");
        keyLeft = settingsManager.getKey("move_left");
        keyRight = settingsManager.getKey("move_right");
        keyRun = settingsManager.getKey("run");
    }


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