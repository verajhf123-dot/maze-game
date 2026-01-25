package de.tum.cit.fop.maze.progression;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SkillTree manages all player skills, including passive bonuses and Q/E/R abilities.
 * Supports unlocking skills via XP, applying bonuses, handling cooldowns, and special abilities.
 */
public class SkillTree {

    // ------------------- Skill Node -------------------
    public static class SkillNode {
        public String id;           // Unique skill ID
        public String name;         // Display name
        public String description;  // Skill description
        public int cost;            // XP cost to unlock
        public boolean unlocked;    // Whether skill is unlocked
        public float healthBonus;   // Passive HP bonus
        public float speedBonus;    // Passive Speed bonus
        public float attackBonus;   // Passive Attack bonus

        public String skillType;    // Active skill type (fireball, heal, etc. or passive type)
        public float skillValue;    // Skill effect value (damage, heal, crit, dodge etc.)
        public float skillCooldown; // Skill cooldown in seconds
        public String bindKey;      // Key binding (Q/E/R)

        public SkillNode(String id, String name, String description, int cost,
                         float healthBonus, float speedBonus, float attackBonus,
                         String skillType, float skillValue, float skillCooldown, String bindKey) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.unlocked = false;
            this.healthBonus = healthBonus;
            this.speedBonus = speedBonus;
            this.attackBonus = attackBonus;
            this.skillType = skillType;
            this.skillValue = skillValue;
            this.skillCooldown = skillCooldown;
            this.bindKey = bindKey;
        }
    }

    // ------------------- Fields -------------------
    private final ExperienceSystem expSystem;   // Player XP system
    private final Map<String, SkillNode> nodes; // All skills

    // Active skill cooldowns
    private float qCooldownTimer = 0f;
    private float eCooldownTimer = 0f;
    private float rCooldownTimer = 0f;

    // Shield
    private boolean shieldActive = false;
    private float shieldValue = 0f;
    private float shieldTimer = 0f;

    // Total passive bonuses
    private float totalHealthBonus = 0f;
    private float totalSpeedBonus = 0f;
    private float totalAttackBonus = 0f;
    private float totalCritChance = 0f;   // 新增
    private float totalDodgeChance = 0f;  // 新增

    // Special abilities
    private boolean hasDoubleJump = false;
    private boolean hasDash = false;
    private boolean hasFireResistance = false;
    private boolean hasPoisonResistance = false;
    private boolean hasPhasing = false;


    // ------------------- Constructor -------------------
    public SkillTree(ExperienceSystem expSystem) {
        this.expSystem = expSystem;
        this.nodes = new HashMap<>();
        initializeSkills();
    }

    // ------------------- Skill Initialization -------------------
    private void initializeSkills() {
        // Passive bonuses
        nodes.put("health_boost", new SkillNode("health_boost","Health Boost","Max Health +30",100,
                30f,0f,0f,"passive",0f,0f,""));
        nodes.put("speed_boost", new SkillNode("speed_boost","Swift Step","Move Speed +20%",150,
                0f,0.2f,0f,"passive",0f,0f,""));
        nodes.put("attack_boost", new SkillNode("attack_boost","Power Strike","Attack Damage +2",150,
                0f,0f,2f,"passive",0f,0f,""));
        nodes.put("crit_boost", new SkillNode("crit_boost","Sharp Strike","+5% Crit Chance",200,
                0f,0f,0f,"crit",0.05f,0f,""));
        nodes.put("dodge_boost", new SkillNode("dodge_boost","Evasive Maneuver","+5% Dodge Chance",200,
                0f,0f,0f,"dodge",0.05f,0f,""));

        // Active abilities (Q/E/R)
        nodes.put("fireball", new SkillNode("fireball","Fireball","Launch fireball at enemies (Q Key)",100,
                0f,0f,0f,"fireball",30f,4.0f,"Q"));
        nodes.put("heal", new SkillNode("heal","Healing Aura","Restore health (E Key)",300,
                20f,0f,0f,"heal",30f,20f,"E"));
        nodes.put("lightning", new SkillNode("lightning","Chain Lightning","Lightning attacks multiple enemies (R Key)",500,
                0f,0f,2f,"lightning",25f,12f,"R"));
        nodes.put("shield", new SkillNode("shield","Energy Shield","Create shield to absorb damage",180,
                0f,0f,0f,"shield",100f,10f,""));

        // Special movement abilities
        nodes.put("double_jump", new SkillNode("double_jump","Double Jump","Jump twice in mid-air",150,
                0f,0.1f,0f,"ability",0f,0f,""));
        nodes.put("dash", new SkillNode("dash","Dash","Quick dash forward (Shift Key)",150,
                0f,0.15f,0f,"ability",0f,2f,""));

        // Resistances
        nodes.put("fire_resistance", new SkillNode("fire_resistance","Fire Resistance","Reduce fire damage",150,
                0f,0f,0f,"fire_resistance",0f,0f,""));
        nodes.put("poison_resistance", new SkillNode("poison_resistance","Poison Resistance","Reduce poison damage",150,
                0f,0f,0f,"poison_resistance",0f,0f,""));
    }

    // ------------------- Skill Unlocking -------------------
    public boolean unlockSkill(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node == null || node.unlocked) return false;

        if (expSystem.spendExp(node.cost)) {
            node.unlocked = true;
            applyPassiveBonuses(node);
            activateSpecialAbilities(node);
            System.out.println("Skill unlocked: " + node.name);
            return true;
        } else {
            System.out.println("Not enough XP to unlock " + skillId);
            return false;
        }
    }

    /**
     * Force unlock a skill without checking XP (用于加载存档)
     */
    public void forceUnlock(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node != null && !node.unlocked) {
            node.unlocked = true;
            applyPassiveBonuses(node);
            activateSpecialAbilities(node);
            System.out.println("Skill force-unlocked: " + node.name);
        }
    }

    // ------------------- Apply Bonuses -------------------
    private void applyPassiveBonuses(SkillNode node) {
        totalHealthBonus += node.healthBonus;
        totalSpeedBonus += node.speedBonus;
        totalAttackBonus += node.attackBonus;

        // Crit & Dodge
        if ("crit".equals(node.skillType)) totalCritChance += node.skillValue;
        if ("dodge".equals(node.skillType)) totalDodgeChance += node.skillValue;

        // Resistances
        if ("fire_resistance".equals(node.skillType)) hasFireResistance = true;
        if ("poison_resistance".equals(node.skillType)) hasPoisonResistance = true;

        System.out.println("Bonuses applied: HP +" + node.healthBonus +
                ", Speed +" + (int)(node.speedBonus*100) + "%, ATK +" + node.attackBonus);
    }

    private void activateSpecialAbilities(SkillNode node) {
        switch (node.id) {
            case "double_jump": hasDoubleJump = true; break;
            case "dash": hasDash = true; break;
            case "fireball": break;
            case "heal": break;
            case "lightning": break;
        }
    }

    // ------------------- Skill Usage -------------------
    public boolean canUseSkill(String key) {
        SkillNode node = getSkillByKey(key);
        if (node == null || !node.unlocked) return false;
        switch (key) {
            case "Q": return qCooldownTimer <= 0;
            case "E": return eCooldownTimer <= 0;
            case "R": return rCooldownTimer <= 0;
            default: return false;
        }
    }

    public SkillNode useSkill(String key) {
        if (!canUseSkill(key)) return null;
        SkillNode node = getSkillByKey(key);
        if (node == null) return null;

        switch (key) {
            case "Q": qCooldownTimer = node.skillCooldown; break;
            case "E": eCooldownTimer = node.skillCooldown; break;
            case "R": rCooldownTimer = node.skillCooldown; break;
        }
        return node;
    }

    // ------------------- Skill Updates -------------------
    public void update(float delta) {
        if (qCooldownTimer > 0) qCooldownTimer -= delta;
        if (eCooldownTimer > 0) eCooldownTimer -= delta;
        if (rCooldownTimer > 0) rCooldownTimer -= delta;

        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) shieldActive = false;
        }
    }

    // ------------------- Shield -------------------
    public void activateShield(float value, float duration) {
        shieldActive = true;
        shieldValue = value;
        shieldTimer = duration;
    }


    public float applyShield(float damage) {
        if (!shieldActive) return damage;
        float remaining = damage - shieldValue;
        shieldValue -= damage;
        if (shieldValue <= 0) shieldActive = false;
        return Math.max(0, remaining);
    }

    // ------------------- Getters -------------------
    private SkillNode getSkillByKey(String key) {
        for (SkillNode node : nodes.values()) {
            if (key.equals(node.bindKey) && node.unlocked) return node;
        }
        return null;
    }

    public boolean hasDoubleJump() { return hasDoubleJump; }
    public boolean hasDash() { return hasDash; }
    public boolean hasFireResistance() { return hasFireResistance; }
    public boolean hasPoisonResistance() { return hasPoisonResistance; }

    public float getTotalHealthBonus() { return totalHealthBonus; }
    public float getTotalSpeedBonus() { return totalSpeedBonus; }
    public float getTotalAttackBonus() { return totalAttackBonus; }
    public float getTotalCritChance() { return totalCritChance; }
    public float getTotalDodgeChance() { return totalDodgeChance; }

    public SkillNode getQSkill() { return getSkillByKey("Q"); }
    public SkillNode getESkill() { return getSkillByKey("E"); }
    public SkillNode getRSkill() { return getSkillByKey("R"); }

    /** Returns total "defense" bonus from passive skills (approximated) */
    public float getTotalDefenseBonus() {
        // Approximate defense from health bonus and dodge chance
        float defense = totalHealthBonus * 0.1f;   // Each 10 HP = 1 defense
        defense += totalDodgeChance * 50f;        // Scale dodge chance
        return defense;
    }

    public List<String> getUnlockedSkillIds() {
        return nodes.values().stream()
                .filter(node -> node.unlocked)
                .map(node -> node.id)
                .collect(Collectors.toList());
    }

    /** Returns whether shield is currently active */
    public boolean hasShieldActive() {
        return shieldActive;
    }

    /** Resets all skills */
    public void reset() {
        for (SkillNode node : nodes.values()) {
            node.unlocked = false;
        }
        // Reset passive totals
        totalHealthBonus = 0f;
        totalSpeedBonus = 0f;
        totalAttackBonus = 0f;
        totalCritChance = 0f;
        totalDodgeChance = 0f;

        hasDoubleJump = false;
        hasDash = false;
        hasFireResistance = false;
        hasPoisonResistance = false;
        shieldActive = false;
        shieldValue = 0f;
        shieldTimer = 0f;
    }

}