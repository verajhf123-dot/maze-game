package de.tum.cit.fop.maze.progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single node in the skill tree.
 * Each node defines a skill, its bonuses, and its unlock conditions.
 */
public class SkillNode {

    /**
     * Category of the skill node.
     */
    public enum SkillType {
        ATTACK,
        DEFENSE,
        UTILITY,
        MAGIC
    }

    // Unique identifier of the skill
    private String id;

    // Display name of the skill
    private String name;

    // Description shown to the player
    private String description;

    // Skill category
    private SkillType type;

    // Skill point cost to unlock
    private int cost;

    // Unlock state
    private boolean unlocked = false;

    // Availability state based on progression
    private boolean available = false;

    // Required prerequisite skills
    private List<SkillNode> prerequisites = new ArrayList<>();

    // Attribute bonuses provided by this skill
    private float healthBonus = 0;
    private float attackBonus = 0;
    private float defenseBonus = 0;
    private float speedBonus = 0;
    private float critChanceBonus = 0;
    private float dodgeChanceBonus = 0;
    private float critDamageBonus = 0;

    // Ability unlock flags
    private boolean unlocksDoubleJump = false;
    private boolean unlocksDash = false;
    private boolean unlocksFireResistance = false;
    private boolean unlocksPoisonResistance = false;
    private boolean unlocksPhasing = false;

    // Environmental resistance bonuses
    private float trapResistance = 0;
    private float fogResistance = 0;

    // Item-related bonuses
    private float itemEffectBonus = 0;

    // Extra life bonus
    private int extraLives = 0;

    // Grid position in the skill tree UI
    private int gridX;
    private int gridY;

    // Creates a skill node with basic metadata
    public SkillNode(String id, String name, String description, SkillType type, int cost, int gridX, int gridY) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cost = cost;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    // Adds a prerequisite skill node
    public void addPrerequisite(SkillNode node) {
        prerequisites.add(node);
    }

    // Checks whether the skill can be unlocked
    public boolean canUnlock() {
        if (unlocked) return false;

        for (SkillNode prereq : prerequisites) {
            if (!prereq.isUnlocked()) {
                return false;
            }
        }

        return available;
    }

    // Unlocks the skill if conditions are met
    public boolean unlock() {
        if (canUnlock()) {
            unlocked = true;
            return true;
        }
        return false;
    }

    // Resets skill state (used for restart or respec)
    public void reset() {
        unlocked = false;
        available = false;
    }

    // Sets whether the skill is currently available
    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Basic getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public int getCost() { return cost; }
    public boolean isUnlocked() { return unlocked; }
    public boolean isAvailable() { return available; }
    public List<SkillNode> getPrerequisites() { return prerequisites; }

    // Attribute bonus accessors
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

    // Ability unlock accessors
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

    // Resistance bonus accessors
    public float getTrapResistance() { return trapResistance; }
    public void setTrapResistance(float resistance) { this.trapResistance = resistance; }

    public float getFogResistance() { return fogResistance; }
    public void setFogResistance(float resistance) { this.fogResistance = resistance; }

    // Item bonus accessors
    public float getItemEffectBonus() { return itemEffectBonus; }
    public void setItemEffectBonus(float bonus) { this.itemEffectBonus = bonus; }

    // Extra life accessors
    public int getExtraLives() { return extraLives; }
    public void setExtraLives(int lives) { this.extraLives = lives; }

    // Skill tree grid position
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}