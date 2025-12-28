package de.tum.cit.fop.maze.progression;

import java.util.ArrayList;
import java.util.List;

public class SkillNode {
    public enum SkillType {
        ATTACK,
        DEFENSE,
        UTILITY,
        MAGIC
    }

    private String id;
    private String name;
    private String description;
    private SkillType type;
    private int cost;
    private boolean unlocked = false;
    private boolean available = false;

    private List<SkillNode> prerequisites = new ArrayList<>();

    private float healthBonus = 0;
    private float attackBonus = 0;
    private float defenseBonus = 0;
    private float speedBonus = 0;
    private float critChanceBonus = 0;
    private float dodgeChanceBonus = 0;

    private boolean unlocksDoubleJump = false;
    private boolean unlocksDash = false;
    private boolean unlocksFireResistance = false;
    private boolean unlocksPoisonResistance = false;

    private int gridX;
    private int gridY;

    public SkillNode(String id, String name, String description, SkillType type, int cost, int gridX, int gridY) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cost = cost;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void addPrerequisite(SkillNode node) {
        prerequisites.add(node);
    }

    public boolean canUnlock() {
        if (unlocked) return false;

        for (SkillNode prereq : prerequisites) {
            if (!prereq.isUnlocked()) {
                return false;
            }
        }

        return available;
    }

    public boolean unlock() {
        if (canUnlock()) {
            unlocked = true;
            return true;
        }
        return false;
    }

    public void reset() {
        unlocked = false;
        available = false;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public int getCost() { return cost; }
    public boolean isUnlocked() { return unlocked; }
    public boolean isAvailable() { return available; }
    public List<SkillNode> getPrerequisites() { return prerequisites; }

    public float getHealthBonus() { return healthBonus; }
    public void setHealthBonus(float bonus) { this.healthBonus = bonus; }

    public float getAttackBonus() { return attackBonus; }
    public void setAttackBonus(float bonus) { this.attackBonus = bonus; }

    public float getDefenseBonus() { return defenseBonus; }
    public void setDefenseBonus(float bonus) { this.defenseBonus = bonus; }

    public float getSpeedBonus() { return speedBonus; }
    public void setSpeedBonus(float bonus) { this.speedBonus = bonus; }

    public float getCritChanceBonus() { return critChanceBonus; }
    public void setCritChanceBonus(float chance) { this.critChanceBonus = chance; }

    public float getDodgeChanceBonus() { return dodgeChanceBonus; }
    public void setDodgeChanceBonus(float chance) { this.dodgeChanceBonus = chance; }

    public boolean unlocksDoubleJump() { return unlocksDoubleJump; }
    public void setUnlocksDoubleJump(boolean unlocks) { this.unlocksDoubleJump = unlocks; }

    public boolean unlocksDash() { return unlocksDash; }
    public void setUnlocksDash(boolean unlocks) { this.unlocksDash = unlocks; }

    public boolean unlocksFireResistance() { return unlocksFireResistance; }
    public void setUnlocksFireResistance(boolean resistance) { this.unlocksFireResistance = resistance; }

    public boolean unlocksPoisonResistance() { return unlocksPoisonResistance; }
    public void setUnlocksPoisonResistance(boolean resistance) { this.unlocksPoisonResistance = resistance; }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}
