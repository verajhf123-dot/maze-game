package de.tum.cit.fop.maze.progression;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Skill Tree with Q/E/R skills system.
 */
public class SkillTree {
    public static class SkillNode {
        public String id;
        public String name;
        public String description;
        public int cost;
        public boolean unlocked;
        public float healthBonus;
        public float speedBonus;
        public float attackBonus;

        public String skillType;
        public float skillValue;
        public float skillCooldown;
        public String bindKey;

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

    private ExperienceSystem expSystem;
    private Map<String, SkillNode> nodes;

    private float qCooldownTimer = 0f;
    private float eCooldownTimer = 0f;
    private float rCooldownTimer = 0f;

    private boolean shieldActive = false;
    private float shieldTimer = 0f;
    private float shieldValue = 0f;

    private float totalHealthBonus = 0;
    private float totalSpeedBonus = 0;
    private float totalAttackBonus = 0;

    private boolean hasDoubleJump = false;
    private boolean hasDash = false;
    private boolean hasFireResistance = false;
    private boolean hasPoisonResistance = false;
    private boolean hasPhasing = false;

    public SkillTree(ExperienceSystem expSystem) {
        this.expSystem = expSystem;
        this.nodes = new HashMap<>();
        initializeSkills();
    }

    private void initializeSkills() {
        nodes.put("health_boost", new SkillNode(
                "health_boost",
                "Health Boost",
                "Max Health +30",
                100,
                30f,
                0f,
                0f,
                "", 0f, 0f, ""
        ));

        nodes.put("speed_boost", new SkillNode(
                "speed_boost",
                "Swift Step",
                "Move Speed +20%",
                150,
                0f,
                0.2f,
                0f,
                "", 0f, 0f, ""
        ));

        nodes.put("attack_boost", new SkillNode(
                "attack_boost",
                "Power Strike",
                "Attack Damage +2",
                150,
                0f,
                0f,
                2f,
                "", 0f, 0f, ""
        ));

        nodes.put("fireball", new SkillNode(
                "fireball",
                "Fireball",
                "Launch fireball at enemies (Q Key)",
                100,
                0f,
                0f,
                0f,
                "fireball",
                30f,
                4.0f,
                "Q"
        ));

        nodes.put("heal", new SkillNode(
                "heal",
                "Healing Aura",
                "Restore health (E Key)",
                300,
                20f,
                0f,
                0f,
                "heal",
                30f,
                20.0f,
                "E"
        ));

        nodes.put("lightning", new SkillNode(
                "lightning",
                "Chain Lightning",
                "Lightning attacks multiple enemies (R Key)",
                500,
                0f,
                0f,
                2f,
                "lightning",
                25f,
                12.0f,
                "R"
        ));

        nodes.put("shield", new SkillNode(
                "shield",
                "Energy Shield",
                "Create shield to absorb damage",
                180,
                0f,
                0f,
                0f,
                "shield",
                100f,
                10.0f,
                ""
        ));

        nodes.put("double_jump", new SkillNode(
                "double_jump",
                "Double Jump",
                "Jump twice in mid-air",
                150,
                0f,
                0.1f,
                0f,
                "ability",
                0f,
                0f,
                ""
        ));

        nodes.put("dash", new SkillNode(
                "dash",
                "Dash",
                "Quick dash forward (Shift Key)",
                150,
                0f,
                0.15f,
                0f,
                "ability",
                0f,
                2.0f,
                ""
        ));
    }

    /**
     * Unlock a skill
     */
    public boolean unlockSkill(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node == null) return false;
        if (node.unlocked) return false;
        if (expSystem.spendExp(node.cost)) {
            node.unlocked = true;
            applySkillBonuses(node);
            activateSpecialAbilities(node);
            System.out.println("Skill unlocked: " + node.name);
            return true;
        } else {
            System.out.println("Not enough XP!");
            return false;
        }
    }

    private void applySkillBonuses(SkillNode node) {
        totalHealthBonus += node.healthBonus;
        totalSpeedBonus += node.speedBonus;
        totalAttackBonus += node.attackBonus;

        System.out.println("Bonuses applied: HP +" + node.healthBonus +
                ", Speed +" + (node.speedBonus * 100) + "%, " +
                "ATK +" + node.attackBonus);
    }

    private void activateSpecialAbilities(SkillNode node) {
        switch (node.id) {
            case "double_jump":
                hasDoubleJump = true;
                System.out.println("Double Jump ability activated!");
                break;
            case "dash":
                hasDash = true;
                System.out.println("Dash ability activated! (Press Shift)");
                break;
            case "fireball":
                System.out.println("Fireball skill ready! (Press Q)");
                break;
            case "heal":
                System.out.println("Healing skill ready! (Press E)");
                break;
            case "lightning":
                System.out.println("Lightning skill ready! (Press R)");
                break;
        }
    }

    /**
     * Check if a skill can be unlocked
     */
    public boolean canUnlockSkill(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node == null || node.unlocked) return false;
        return expSystem.getCurrentExp() >= node.cost;
    }

    /**
     * Check if a skill is ready to use
     */
    public boolean canUseSkill(String key) {
        SkillNode node = getSkillByKey(key);
        if (node == null || !node.unlocked) {
            return false;
        }

        switch (key) {
            case "Q": return qCooldownTimer <= 0;
            case "E": return eCooldownTimer <= 0;
            case "R": return rCooldownTimer <= 0;
            default: return false;
        }
    }

    /**
     * Use a skill
     */
    public SkillNode useSkill(String key) {
        if (!canUseSkill(key)) {
            return null;
        }

        SkillNode node = getSkillByKey(key);
        if (node == null) {
            return null;
        }

        switch (key) {
            case "Q":
                qCooldownTimer = node.skillCooldown;
                System.out.println("Fireball launched! Cooldown: " + node.skillCooldown + "s");
                break;
            case "E":
                eCooldownTimer = node.skillCooldown;
                System.out.println("Healing applied! Cooldown: " + node.skillCooldown + "s");
                break;
            case "R":
                rCooldownTimer = node.skillCooldown;
                System.out.println("Lightning cast! Cooldown: " + node.skillCooldown + "s");
                break;
        }

        return node;
    }

    public void updateSkillUnlocks() {
        if (expSystem == null) return;

        int totalXP = expSystem.getTotalExp();

        if (totalXP >= 120) {
            unlockPassive("heal");
        }

        if (totalXP >= 150) {
            unlockPassive("fireball");
        }

        if (totalXP >= 200) {
            unlockPassive("lightning");
        }
    }

    private void unlockPassive(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node != null && !node.unlocked) {
            node.unlocked = true;
            activateSpecialAbilities(node);
            System.out.println("MILESTONE REACHED: Auto-unlocked " + node.name + "!");
        }
    }

    public void update(float delta) {
        updateSkillUnlocks();

        if (qCooldownTimer > 0) qCooldownTimer -= delta;
        if (eCooldownTimer > 0) eCooldownTimer -= delta;
        if (rCooldownTimer > 0) rCooldownTimer -= delta;

        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) {
                shieldActive = false;
                System.out.println("Shield expired");
            }
        }
    }

    /**
     * Get skill by key binding
     */
    private SkillNode getSkillByKey(String key) {
        for (SkillNode node : nodes.values()) {
            if (node.bindKey != null && node.bindKey.equals(key) && node.unlocked) {
                return node;
            }
        }
        return null;
    }

    /**
     * Activate shield effect
     */
    public void activateShield(float value, float duration) {
        shieldActive = true;
        shieldValue = value;
        shieldTimer = duration;
        System.out.println("Shield activated: " + value + " HP, " + duration + "s");
    }

    /**
     * Calculate damage reduction from shield
     */
    public float applyShield(float damage) {
        if (!shieldActive) return damage;

        float remainingDamage = damage - shieldValue;
        shieldValue -= damage;

        if (shieldValue <= 0) {
            shieldActive = false;
            System.out.println("Shield broken!");
            return Math.max(0, remainingDamage);
        }
        return 0;
    }

    /**
     * Reset all skills
     */
    public void reset() {
        for (SkillNode node : nodes.values()) {
            node.unlocked = false;
        }
        totalHealthBonus = 0;
        totalSpeedBonus = 0;
        totalAttackBonus = 0;

        hasDoubleJump = false;
        hasDash = false;
        hasFireResistance = false;
        hasPoisonResistance = false;
        hasPhasing = false;

        qCooldownTimer = 0;
        eCooldownTimer = 0;
        rCooldownTimer = 0;
        shieldActive = false;

        System.out.println("Skill tree reset");
    }


    public SkillNode getNode(String id) { return nodes.get(id); }
    public Map<String, SkillNode> getAllNodes() { return nodes; }
    public Map<String, SkillNode> getUnlockedNodes() {
        Map<String, SkillNode> unlocked = new HashMap<>();
        for (Map.Entry<String, SkillNode> entry : nodes.entrySet()) {
            if (entry.getValue().unlocked) {
                unlocked.put(entry.getKey(), entry.getValue());
            }
        }
        return unlocked;
    }

    public float getQCooldown() { return qCooldownTimer; }
    public float getECooldown() { return eCooldownTimer; }
    public float getRCooldown() { return rCooldownTimer; }

    public float getQCooldownPercent() {
        SkillNode node = getSkillByKey("Q");
        if (node == null) return 0;
        return Math.min(1.0f, qCooldownTimer / node.skillCooldown);
    }

    public float getECooldownPercent() {
        SkillNode node = getSkillByKey("E");
        if (node == null) return 0;
        return Math.min(1.0f, eCooldownTimer / node.skillCooldown);
    }

    public float getRCooldownPercent() {
        SkillNode node = getSkillByKey("R");
        if (node == null) return 0;
        return Math.min(1.0f, rCooldownTimer / node.skillCooldown);
    }

    public SkillNode getQSkill() { return getSkillByKey("Q"); }
    public SkillNode getESkill() { return getSkillByKey("E"); }
    public SkillNode getRSkill() { return getSkillByKey("R"); }

    public boolean hasQSkill() { return getQSkill() != null; }
    public boolean hasESkill() { return getESkill() != null; }
    public boolean hasRSkill() { return getRSkill() != null; }

    public float getTotalHealthBonus() { return totalHealthBonus; }
    public float getTotalAttackBonus() { return totalAttackBonus; }
    public float getTotalSpeedBonus() { return totalSpeedBonus; }
    public float getTotalDefenseBonus() { return 0; }
    public float getTotalCritChance() { return 0; }
    public float getTotalDodgeChance() { return 0; }
    public float getTotalCritDamageBonus() { return 0; }
    public float getTotalTrapResistance() { return 0; }
    public float getTotalFogResistance() { return 0; }
    public float getTotalItemEffectBonus() { return 0; }
    public int getTotalExtraLives() { return 0; }

    public boolean hasDoubleJump() { return hasDoubleJump; }
    public boolean hasDash() { return hasDash; }
    public boolean hasFireResistance() { return hasFireResistance; }
    public boolean hasPoisonResistance() { return hasPoisonResistance; }
    public boolean hasPhasing() { return hasPhasing; }

    public boolean hasShieldActive() { return shieldActive; }
    public float getShieldValue() { return shieldValue; }
    public float getShieldTimer() { return shieldTimer; }

    /**
     * Get skill status summary
     */
    public String getSkillSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SKILL TREE SUMMARY ===\n");

        int unlockedCount = 0;
        for (SkillNode node : nodes.values()) {
            sb.append(node.name);
            if (!node.bindKey.isEmpty()) {
                sb.append(" [").append(node.bindKey).append("]");
            }
            sb.append(": ");
            if (node.unlocked) {
                sb.append("[UNLOCKED]");
                unlockedCount++;
            } else {
                sb.append("[LOCKED] - Cost: ").append(node.cost).append(" XP");
            }
            sb.append("\n");
        }

        sb.append("\n=== ACTIVE SKILLS ===\n");
        if (hasQSkill()) sb.append("Q: Fireball\n");
        if (hasESkill()) sb.append("E: Healing Aura\n");
        if (hasRSkill()) sb.append("R: Chain Lightning\n");
        if (hasDoubleJump()) sb.append("Ability: Double Jump\n");
        if (hasDash()) sb.append("Ability: Dash (Shift)\n");

        sb.append("\n=== TOTAL BONUSES ===\n");
        sb.append("Health: +").append(totalHealthBonus).append(" HP\n");
        sb.append("Speed: +").append((int)(totalSpeedBonus * 100)).append("%\n");
        sb.append("Attack: +").append(totalAttackBonus).append(" DMG\n");
        sb.append("\nSkills Unlocked: ").append(unlockedCount).append("/").append(nodes.size()).append("\n");

        return sb.toString();
    }

    public void updateSkillAvailability() {
        for (SkillNode node : nodes.values()) {
            boolean canUnlock = canUnlockSkill(node.id);
        }
    }

    /**
     * Obtain the list of IDs for all unlocked skills (for saving progress).
     */
    public java.util.List<String> getUnlockedSkillIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (SkillNode node : getAllNodes().values()) {
            if (node.unlocked) {
                ids.add(node.id);
            }
        }
        return ids;
    }


    public void forceUnlock(String skillId) {
        SkillNode node = getAllNodes().get(skillId);
        if (node != null) {
            node.unlocked = true;

            applySkillBonuses(node);
            activateSpecialAbilities(node);

            System.out.println("Restored skill from save: " + node.name);
        }
    }
}
