package de.tum.cit.fop.maze.progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single skill node in the player's skill tree.
 * Handles unlocking logic, prerequisites, and skill bonuses.
 */
public class SkillNode {

    // ------------------- Skill Type Enum -------------------
    public enum SkillType {
        ATTACK,
        DEFENSE,
        UTILITY,
        MAGIC
    }

    // ------------------- Core Properties -------------------
    private final String id;                 // Unique skill ID
    private final String name;               // Display name
    private final String description;        // Skill description
    private final SkillType type;            // Category of skill
    private final int cost;                  // Skill point cost
    private boolean unlocked = false;        // Whether the skill has been unlocked
    private boolean available = false;       // Whether the skill is available to unlock

    private final List<SkillNode> prerequisites = new ArrayList<>(); // Prerequisite skills

    // ------------------- Bonuses -------------------
    private float healthBonus = 0;
    private float attackBonus = 0;
    private float defenseBonus = 0;
    private float speedBonus = 0;
    private float critChanceBonus = 0;
    private float dodgeChanceBonus = 0;
    private float critDamageBonus = 0;

    // ------------------- Special Unlocks -------------------
    private boolean unlocksDoubleJump = false;
    private boolean unlocksDash = false;
    private boolean unlocksFireResistance = false;
    private boolean unlocksPoisonResistance = false;
    private boolean unlocksPhasing = false;

    // ------------------- Miscellaneous Bonuses -------------------
    private float trapResistance = 0;
    private float fogResistance = 0;
    private float itemEffectBonus = 0;
    private int extraLives = 0;

    // ------------------- UI / Grid Position -------------------
    private final int gridX;
    private final int gridY;

    // ------------------- Constructor -------------------

    /**
     * Initialize a new skill node with its basic info.
     *
     * @param id Unique skill identifier
     * @param name Display name
     * @param description Skill description
     * @param type Skill type/category
     * @param cost Skill point cost
     * @param gridX X coordinate in skill tree grid
     * @param gridY Y coordinate in skill tree grid
     */
    public SkillNode(String id, String name, String description, SkillType type, int cost, int gridX, int gridY) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cost = cost;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    // ------------------- Prerequisite Management -------------------

    /**
     * Add a prerequisite skill that must be unlocked before this skill.
     *
     * @param node SkillNode that is a prerequisite
     */
    public void addPrerequisite(SkillNode node) {
        prerequisites.add(node);
    }

    /**
     * Check whether this skill can be unlocked.
     * Requires all prerequisites to be unlocked and the skill to be available.
     *
     * @return true if unlockable, false otherwise
     */
    public boolean canUnlock() {
        if (unlocked) return false;

        for (SkillNode prereq : prerequisites) {
            if (!prereq.isUnlocked()) {
                return false;
            }
        }

        return available;
    }

    /**
     * Attempt to unlock this skill.
     *
     * @return true if unlock successful, false otherwise
     */
    public boolean unlock() {
        if (canUnlock()) {
            unlocked = true;
            return true;
        }
        return false;
    }

    /**
     * Reset skill to locked and unavailable state.
     */
    public void reset() {
        unlocked = false;
        available = false;
    }

    /**
     * Set the availability status of the skill.
     *
     * @param available true if player can attempt to unlock
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    // ------------------- Getters -------------------

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

    public float getCritDamageBonus() { return critDamageBonus; }
    public void setCritDamageBonus(float bonus) { this.critDamageBonus = bonus; }

    // ------------------- Special Abilities -------------------

    public boolean unlocksDoubleJump() { return unlocksDoubleJump; }
    public void setUnlocksDoubleJump(boolean unlocks) { this.unlocksDoubleJump = unlocks; }

    public boolean unlocksDash() { return unlocksDash; }
    public void setUnlocksDash(boolean unlocks) { this.unlocksDash = unlocks; }

    public boolean unlocksFireResistance() { return unlocksFireResistance; }
    public void setUnlocksFireResistance(boolean resistance) { this.unlocksFireResistance = resistance; }

    public boolean unlocksPoisonResistance() { return unlocksPoisonResistance; }
    public void setUnlocksPoisonResistance(boolean resistance) { this.unlocksPoisonResistance = resistance; }

    public boolean unlocksPhasing() { return unlocksPhasing; }
    public void setUnlocksPhasing(boolean phasing) { this.unlocksPhasing = phasing; }

    // ------------------- Miscellaneous Bonuses -------------------

    public float getTrapResistance() { return trapResistance; }
    public void setTrapResistance(float resistance) { this.trapResistance = resistance; }

    public float getFogResistance() { return fogResistance; }
    public void setFogResistance(float resistance) { this.fogResistance = resistance; }

    public float getItemEffectBonus() { return itemEffectBonus; }
    public void setItemEffectBonus(float bonus) { this.itemEffectBonus = bonus; }

    public int getExtraLives() { return extraLives; }
    public void setExtraLives(int lives) { this.extraLives = lives; }

    // ------------------- Grid Position -------------------

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}