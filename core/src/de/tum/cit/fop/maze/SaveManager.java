package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.progression.SkillTree;

/**
 * Handles saving and loading game progress using a local JSON file.
 * This class converts PlayerStats into SaveData and writes/reads it from disk.
 */

public class SaveManager {
    private static final String SAVE_FILE = "save_game.json";
    /**
     * Saves the current game state to a local JSON file.
     *
     * @param levelMapIndex current level index
     * @param stats current player stats
     */
    public static void saveGame(int levelMapIndex, PlayerStats stats) {
        Json json = new Json();
        SaveData data = new SaveData();

        data.setCurrentLevelMap(levelMapIndex);
//save player stats if available.
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
    /**
     * Loads the saved game data from the local JSON file.
     *
     * @return SaveData object if loading succeeds, or null if no save exists or an error occurs
     */
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

    /**
     * Checks whether a save file already exists.
     *
     * @return true if a save file is found, false otherwise
     */

    public static boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE).exists();
    }
}