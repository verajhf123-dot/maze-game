package de.tum.cit.fop.maze.progression;

public class ExperienceSystem {

    // Current available experience points
    private int currentExp = 0;

    // Total accumulated experience points
    private int totalExp = 0;

    // Default constructor
    public ExperienceSystem() {
    }

    // Adds experience points to the system
    public void gainExp(int amount) {
        if (amount <= 0) return;
        currentExp += amount;
        totalExp += amount;
        System.out.println("Gained " + amount + " XP. Current: " + currentExp);
    }

    // Spends experience points if enough are available
    public boolean spendExp(int amount) {
        if (currentExp >= amount) {
            currentExp -= amount;
            System.out.println("Spent " + amount + " XP. Remaining: " + currentExp);
            return true;
        }
        return false;
    }

    // Returns experience reward based on enemy type
    public static int getExpForEnemy(String enemyType) {
        switch (enemyType) {
            case "NineTailedFox": return 75;
            case "QiongQi":       return 100;
            case "ZhuLong":       return 150;
            default:              return 20;
        }
    }

    // Getter for current experience
    public int getCurrentExp() { return currentExp; }

    // Getter for total accumulated experience
    public int getTotalExp() { return totalExp; }

    // Returns current player level (placeholder)
    public int getCurrentLevel() { return 1; }

    // Returns available skill points (placeholder)
    public int getSkillPoints() { return 0; }

    // Returns health bonus from progression (placeholder)
    public int getHealthBonus() { return 0; }

    // Returns attack bonus from progression (placeholder)
    public int getAttackBonus() { return 0; }

    // Returns defense bonus from progression (placeholder)
    public int getDefenseBonus() { return 0; }

    // Sets the current experience value
    public void setCurrentExp(int exp) { this.currentExp = exp; }
}