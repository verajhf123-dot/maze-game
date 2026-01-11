package de.tum.cit.fop.maze;

import java.util.ArrayList;
import java.util.List;


public class SaveData {
    // ===========================
    // 1. 基础信息
    // ===========================
    private int currentLevel; // 当前地图是第几关 (例如 1, 2, 3...)
    private boolean hasKey;      // 当前是否持有通关钥匙

    private int currentHealth;
    private int maxHealth;
    private int currentExp;
    private int charLevel;       // 玩家的角色等级 (例如 Lv.5)
    private int skillPoints;     // 剩余技能点

    // ===========================
    // 3. 技能树状态
    // ===========================
    // 存储所有已解锁技能的 ID (例如 "doubleJump", "fireball")
    private List<String> unlockedSkillIds = new ArrayList<>();


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