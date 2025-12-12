package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;

public class SettingsManager {

    private HashMap<String, Integer> keyMap;
    private final String SETTINGS_PATH = "config/settings.json";

    public SettingsManager() {
        load();
    }

    public void load() {
        Json json = new Json();
        FileHandle file = Gdx.files.local(SETTINGS_PATH);

        if(file.exists()) {
            keyMap = json.fromJson(HashMap.class, file.readString());
        } else {
            keyMap = getDefaultKeys();
            save();
        }
    }

    public void save() {
        Json json = new Json();
        FileHandle file = Gdx.files.local(SETTINGS_PATH);
        file.writeString(json.prettyPrint(keyMap), false);
    }

    public int getKey(String action) {
        return keyMap.get(action);
    }

    private HashMap<String, Integer> getDefaultKeys() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("move_up", com.badlogic.gdx.Input.Keys.W);
        map.put("move_down", com.badlogic.gdx.Input.Keys.S);
        map.put("move_left", com.badlogic.gdx.Input.Keys.A);
        map.put("move_right", com.badlogic.gdx.Input.Keys.D);
        map.put("run", com.badlogic.gdx.Input.Keys.SHIFT_LEFT);
        return map;
    }
}
