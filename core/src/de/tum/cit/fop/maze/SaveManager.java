package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.progression.SkillTree;

public class SaveManager {
    private static final String SAVE_FILE = "save_game.json";

    public static void saveGame(int levelMapIndex, PlayerStats stats) {
        Json json = new Json();
        SaveData data = new SaveData();

        data.setCurrentLevelMap(levelMapIndex);

        if (stats != null) {
            data.setCurrentHealth((int) stats.getHealth());
            data.setMaxHealth((int) stats.getMaxHealth());
            data.setHasKey(stats.hasKey());

            if (stats.getExpSystem() != null) {
                data.setCurrentExp(stats.getExpSystem().getCurrentExp());
                data.setCharLevel(stats.getExpSystem().getCurrentLevel());
                data.setSkillPoints(stats.getExpSystem().getSkillPoints());
            }


            SkillTree tree = stats.getSkillTree();
            if (tree != null) {
                data.setUnlockedSkillIds(tree.getUnlockedSkillIds());
            }
        }

        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.prettyPrint(data), false);

        System.out.println("Game Saved! Map: Level " + levelMapIndex + ", Player Level: " + data.getCharLevel());
    }

    public static SaveData loadGame() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            return null;
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