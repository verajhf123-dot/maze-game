package de.tum.cit.fop.maze;

public class PlayerStats {

    private int maxHealth = 5;
    private int health = 5;

    public void heal(int value) {
        health = Math.min(maxHealth, health + value);
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if(health < 0) health = 0;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public boolean isDead() {
        return health <= 0;
    }
    private boolean hasKey = false;

    public boolean hasKey() {
        return hasKey;
    }

    public void obtainKey() {
        this.hasKey = true;
    }
    public void useKey(){
        if(hasKey){
            hasKey = false;
        }
    }
}
