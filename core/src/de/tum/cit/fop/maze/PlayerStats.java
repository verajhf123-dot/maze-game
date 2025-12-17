package de.tum.cit.fop.maze;

public class PlayerStats {

    private int maxHealth = 5;
    private int health = 5;
    // for fix the key
    private boolean hasKey = false;
    private int bonusKey = 0;

    public void heal(int value) {
        health = Math.min(maxHealth, health + value);
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if(health < 0) health = 0;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean hasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }
    public int getBonusKey() { return bonusKey; }
    public void setBonusKey(int bonusKey) { this.bonusKey = bonusKey; }
    public void addBonusKey() { if (bonusKey < 2) bonusKey ++; }

    public boolean isDead() {
        return health <= 0;
    }
}
