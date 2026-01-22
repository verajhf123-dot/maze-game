package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AchievementManager handles the game's milestone system.
 * It tracks player statistics and unlocks achievements from an external JSON file.
 */
public class AchievementManager {
    private int totalKills = 0;
    private int totalExpGained = 0;

    private Map<String, AchievementData> achievementMap;
    private Map<String, Boolean> unlockedStatus;
    public static class AchievementData {
        public String id;
        public String name;
        public String description;
        public int requirement;
    }

    public AchievementManager() {
        achievementMap = new HashMap<>();
        unlockedStatus = new HashMap<>();
        loadAchievements();
    }

    /**
     * Loads achievement definitions from an external JSON file[cite: 75, 88].
     */
    private void loadAchievements() {
        Json json = new Json();
        try {
            ArrayList<AchievementData> list = json.fromJson(ArrayList.class, AchievementData.class, Gdx.files.internal("achievements.json"));
            for (AchievementData data : list) {
                achievementMap.put(data.id, data);
                unlockedStatus.put(data.id, false);
            }
            System.out.println("AchievementManager: Successfully loaded milestones.");
        } catch (Exception e) {
            System.out.println("Error loading achievements: " + e.getMessage());
        }
    }

    /**
     * Tracks a kill and checks for milestone completion.
     */
    public void trackKill() {
        totalKills++;
        checkMilestones();
    }

    /**
     * Tracks gained experience and checks for milestones[cite: 84].
     */
    public void trackExp(int exp) {
        totalExpGained += exp;
        checkMilestones();
    }

    private void checkMilestones() {
        if (totalKills >= 1) unlock("FIRST_BLOOD");
        if (achievementMap.containsKey("MONSTER_SLAYER") &&
                totalKills >= achievementMap.get("MONSTER_SLAYER").requirement) {
            unlock("MONSTER_SLAYER");
        }
        if (achievementMap.containsKey("EXP_MASTER") &&
                totalExpGained >= achievementMap.get("EXP_MASTER").requirement) {
            unlock("EXP_MASTER");
        }
    }

    private void unlock(String id) {
        if (unlockedStatus.containsKey(id) && !unlockedStatus.get(id)) {
            unlockedStatus.put(id, true);
            AchievementData data = achievementMap.get(id);
            System.out.println("CULTIVATION BREAKTHROUGH: " + data.name + " (" + data.description + ")");
        }
    }
    public int getTotalKills() { return totalKills; }
    public int getTotalExpGained() { return totalExpGained; }
    public Map<String, Boolean> getUnlockedStatus() { return unlockedStatus; }
}