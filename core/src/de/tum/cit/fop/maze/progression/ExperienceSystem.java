package de.tum.cit.fop.maze.progression;

public class ExperienceSystem {
    private int currentExp = 0;
    private int totalExp = 0;

    public ExperienceSystem() {
    }

    public void gainExp(int amount) {
        if (amount <= 0) return;
        currentExp += amount;
        totalExp += amount;
        System.out.println("Gained " + amount + " XP. Current: " + currentExp);
    }

    // ▼▼▼▼▼ 这就是你缺失的方法，必须加上！ ▼▼▼▼▼
    public boolean spendExp(int amount) {
        if (currentExp >= amount) {
            currentExp -= amount;
            System.out.println("Spent " + amount + " XP. Remaining: " + currentExp);
            return true;
        }
        return false;
    }
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    public static int getExpForEnemy(String enemyType) {
        switch (enemyType) {
            case "NineTailedFox": return 75;
            case "QiongQi":       return 100;
            case "ZhuLong":       return 150;
            default:              return 20;
        }
    }

    public int getCurrentExp() { return currentExp; }
    public int getTotalExp() { return totalExp; }

    // 兼容旧代码的方法（如果还有地方调用）
    public int getCurrentLevel() { return 1; } // 永远返回1级
    public int getSkillPoints() { return 0; }  // 永远返回0点
    public int getHealthBonus() { return 0; }
    public int getAttackBonus() { return 0; }
    public int getDefenseBonus() { return 0; }

    public void setCurrentExp(int exp) { this.currentExp = exp; }
}