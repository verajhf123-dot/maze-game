package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveManager {
    private static final String SAVE_FILE = "save_game.json";

    public static void saveGame(int level, float health, boolean hasKey) {
        Json json = new Json();
        SaveData data = new SaveData(level, health, hasKey);



        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.toJson(data), false);

        System.out.println("Game Saved: Level " + level);
    }


    public static SaveData loadGame() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            return null; // 没有存档
        }

        Json json = new Json();
        try {
            return json.fromJson(SaveData.class, file.readString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE).exists();
    }
}