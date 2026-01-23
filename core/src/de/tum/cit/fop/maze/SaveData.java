package de.tum.cit.fop.maze;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple data container for saving and loading game progress.
 * This class only stores data and does not contain any game logic.
 */

public class SaveData {

    private int currentLevel;
    private boolean hasKey;

    private int currentHealth;
    private int maxHealth;
    private int currentExp;
    private int charLevel;
    private int skillPoints;


    private List<String> unlockedSkillIds = new ArrayList<>();

    /**
     * Creates an empty SaveData object.
     * Values are filled later when saving the game.
     */
    public SaveData() {}


    public int getCurrentLevelMap() { return currentLevel; }
    public void setCurrentLevelMap(int currentLevelMap) { this.currentLevel = currentLevelMap; }

    public boolean isHasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getCurrentExp() { return currentExp; }
    public void setCurrentExp(int currentExp) { this.currentExp = currentExp; }

    public int getCharLevel() { return charLevel; }
    public void setCharLevel(int charLevel) { this.charLevel = charLevel; }

    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }

    public List<String> getUnlockedSkillIds() { return unlockedSkillIds; }
    public void setUnlockedSkillIds(List<String> unlockedSkillIds) { this.unlockedSkillIds = unlockedSkillIds; }
}