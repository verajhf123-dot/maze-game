package de.tum.cit.fop.maze.progression;

/**
 * Handles experience points (XP) for the player.
 * Tracks current XP, total XP, and provides utility for gaining/spending XP.
 */
public class ExperienceSystem {

    /** Current XP available to spend */
    private int currentExp = 0;

    /** Total XP gained across the game */
    private int totalExp = 0;

    /** Constructor initializes the system */
    public ExperienceSystem() {
        System.out.println("ExperienceSystem initialized. Current XP: 0, Total XP: 0");
    }

    /**
     * Adds XP to the player.
     * @param amount XP amount to add
     */
    public void gainExp(int amount) {
        if (amount <= 0) return;

        currentExp += amount;
        totalExp += amount;

        System.out.println("Gained " + amount + " XP. Current XP: " + currentExp
                + ", Total XP: " + totalExp);
    }

    /**
     * Spends XP if enough is available.
     * @param amount XP to spend
     * @return true if spending succeeded, false otherwise
     */
    public boolean spendExp(int amount) {
        if (currentExp >= amount) {
            currentExp -= amount;
            System.out.println("Spent " + amount + " XP. Remaining XP: " + currentExp);
            return true;
        } else {
            System.out.println("Not enough XP to spend " + amount + ". Current: " + currentExp);
            return false;
        }
    }

    /**
     * Returns the amount of XP awarded for defeating a specific enemy type.
     * @param enemyType The enemy's class/type name
     * @return XP reward
     */
    public static int getExpForEnemy(String enemyType) {
        switch (enemyType) {
            case "NineTailedFox": return 75;
            case "QiongQi":       return 100;
            case "ZhuLong":       return 150;
            default:              return 20;
        }
    }

    // --- Getters and setters ---

    public int getCurrentExp() { return currentExp; }
    public int getTotalExp() { return totalExp; }

    public int getCurrentLevel() { return 1; }   // Placeholder for level calculation
    public int getSkillPoints() { return 0; }
    public int getHealthBonus() { return 0; }
    public int getAttackBonus() { return 0; }
    public int getDefenseBonus() { return 0; }

    public void setCurrentExp(int exp) {
        currentExp = Math.max(0, exp);
        System.out.println("Current XP manually set to " + currentExp);
    }
}