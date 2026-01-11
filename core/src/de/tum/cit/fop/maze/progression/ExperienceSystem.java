package de.tum.cit.fop.maze.progression;

import java.util.ArrayList;
import java.util.List;

public class ExperienceSystem {
    private int currentExp = 0;
    private int currentLevel = 1;
    private int expToNextLevel = 100;
    private int totalExp = 0;
    private int skillPointsGained = 0;

    private int healthBonusPerLevel = 0;
    private int attackBonusPerLevel = 0;
    private int defenseBonusPerLevel = 0;

    private List<ExpListener> listeners = new ArrayList<>();

    public interface ExpListener {
        void onExpGained(int amount, int total);
        void onLevelUp(int newLevel, int skillPoints);
        void onSkillPointsChanged(int points);
    }

    public ExperienceSystem() {
        calculateExpForNextLevel();
    }

    public void gainExp(int amount) {
        if (amount <= 0) return;

        int oldExp = currentExp;
        currentExp += amount;
        totalExp += amount;

        for (ExpListener listener : listeners) {
            listener.onExpGained(amount, currentExp);
        }

        while (currentExp >= expToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        currentLevel++;
        currentExp -= expToNextLevel;
        skillPointsGained++;

        calculateExpForNextLevel();

        for (ExpListener listener : listeners) {
            listener.onLevelUp(currentLevel, skillPointsGained);
        }

        System.out.println("Level up to " + currentLevel + "! Gained 1 skill point");
    }

    public boolean useSkillPoint() {
        if (skillPointsGained > 0) {
            skillPointsGained--;
            for (ExpListener listener : listeners) {
                listener.onSkillPointsChanged(skillPointsGained);
            }
            return true;
        }
        return false;
    }

    private void calculateExpForNextLevel() {
        expToNextLevel = (int) (100 * Math.pow(currentLevel, 1.5));
    }

    public static int getExpForEnemy(String enemyType) {
        switch (enemyType) {
            case "NineTailedFox":
                return 25;
            case "QiongQi":
                return 60;
            case "ZhuLong":
                return 150;
            default:
                return 20;
        }
    }

    public int getHealthBonus() {
        return (currentLevel - 1) * healthBonusPerLevel;
    }

    public int getAttackBonus() {
        return (currentLevel - 1) * attackBonusPerLevel;
    }

    public int getDefenseBonus() {
        return (currentLevel - 1) * defenseBonusPerLevel;
    }

    public void addListener(ExpListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ExpListener listener) {
        listeners.remove(listener);
    }

    public int getCurrentExp() { return currentExp; }
    public int getCurrentLevel() { return currentLevel; }
    public int getExpToNextLevel() { return expToNextLevel; }
    public int getTotalExp() { return totalExp; }
    public int getSkillPoints() { return skillPointsGained; }

    public float getExpPercentage() {
        return (float) currentExp / expToNextLevel;
    }

    public void reset() {
        currentExp = 0;
        currentLevel = 1;
        expToNextLevel = 100;
        totalExp = 0;
        skillPointsGained = 0;
        calculateExpForNextLevel();
    }


    public void setLevel(int level) {
        this.currentLevel = level;
        calculateExpForNextLevel(); // 重新计算升级所需经验
    }

    public void setCurrentExp(int exp) {
        this.currentExp = exp;
        // 简单起见，读取存档时暂且认为 total = current，或者你可以不存 totalExp
        this.totalExp = exp;
    }

    public void setSkillPoints(int points) {
        this.skillPointsGained = points;
    }





}