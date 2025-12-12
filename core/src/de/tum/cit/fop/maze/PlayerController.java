package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;

public class PlayerController {

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
