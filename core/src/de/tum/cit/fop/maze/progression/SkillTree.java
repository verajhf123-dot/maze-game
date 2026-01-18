package de.tum.cit.fop.maze.progression;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Skill Tree with Q/E/R skills system.
 */
public class SkillTree {
    // Make SkillNode public and fields public
    public static class SkillNode {
        public String id;
        public String name;
        public String description;
        public int cost;
        public boolean unlocked;
        public float healthBonus;
        public float speedBonus;
        public float attackBonus;

        // Skill-related fields
        public String skillType;  // "fireball", "heal", "lightning", "shield", "slow", "teleport"
        public float skillValue;  // Skill value (damage/healing amount)
        public float skillCooldown; // Cooldown time in seconds
        public String bindKey;    // Bind to key "Q", "E", "R"

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

    // Q/E/R skill cooldown timers
    private float qCooldownTimer = 0f;
    private float eCooldownTimer = 0f;
    private float rCooldownTimer = 0f;

    // Skill states
    private boolean shieldActive = false;
    private float shieldTimer = 0f;
    private float shieldValue = 0f;

    // Total bonuses
    private float totalHealthBonus = 0;
    private float totalSpeedBonus = 0;
    private float totalAttackBonus = 0;

    // Special abilities
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
        // 1. Health Boost (基础技能)
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

        // 2. Speed Boost (基础技能)
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

        // 3. Attack Boost (基础技能)
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

        // 4. Q Skill - Fireball
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

        // 5. E Skill - Healing Aura
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

        // 6. R Skill - Chain Lightning
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

        // 7. Shield Skill
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
                ""  // Not bound to specific key
        ));

        // 8. Double Jump Ability
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
                ""  // Passive ability
        ));

        // 9. Dash Ability
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
                ""  // Bound to Shift
        ));
    }

    /**
     * Unlock a skill
     */
    public boolean unlockSkill(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node == null) {
            System.err.println("Skill not found: " + skillId);
            return false;
        }

        if (node.unlocked) {
            System.err.println("Skill already unlocked: " + node.name);
            return false;
        }

        // Check if player has enough XP
        if (expSystem.getCurrentExp() < node.cost) {
            System.err.println("Not enough XP! Need: " + node.cost + ", Have: " + expSystem.getCurrentExp());
            return false;
        }

        // Check skill points
        if (expSystem.getSkillPoints() <= 0) {
            System.err.println("No skill points available");
            return false;
        }

        // Use skill point
        if (!expSystem.useSkillPoint()) {
            System.err.println("Failed to use skill point");
            return false;
        }

        // Unlock the skill
        node.unlocked = true;

        // Apply skill bonuses
        applySkillBonuses(node);

        // Activate special abilities
        activateSpecialAbilities(node);

        System.out.println("✓ Skill unlocked: " + node.name +
                (node.bindKey.isEmpty() ? "" : " (" + node.bindKey + " Key)"));
        return true;
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
        if (node == null) return false;
        if (node.unlocked) return false;
        return expSystem.getSkillPoints() >= 1 && expSystem.getCurrentExp() >= node.cost;
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

        // Set cooldown
        switch (key) {
            case "Q":
                qCooldownTimer = node.skillCooldown;
                System.out.println("🔥 Fireball launched! Cooldown: " + node.skillCooldown + "s");
                break;
            case "E":
                eCooldownTimer = node.skillCooldown;
                System.out.println("💚 Healing applied! Cooldown: " + node.skillCooldown + "s");
                break;
            case "R":
                rCooldownTimer = node.skillCooldown;
                System.out.println("⚡ Lightning cast! Cooldown: " + node.skillCooldown + "s");
                break;
        }

        return node;
    }

    /**
     * Update cooldowns and effects
     */
    public void update(float delta) {
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
        System.out.println("🛡️ Shield activated: " + value + " HP, " + duration + "s");
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

    // ===== GETTER METHODS =====
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

    // Skill cooldown getters
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

    // Get specific skill nodes
    public SkillNode getQSkill() { return getSkillByKey("Q"); }
    public SkillNode getESkill() { return getSkillByKey("E"); }
    public SkillNode getRSkill() { return getSkillByKey("R"); }

    // Check skill availability
    public boolean hasQSkill() { return getQSkill() != null; }
    public boolean hasESkill() { return getESkill() != null; }
    public boolean hasRSkill() { return getRSkill() != null; }

    // Total bonuses
    public float getTotalHealthBonus() { return totalHealthBonus; }
    public float getTotalAttackBonus() { return totalAttackBonus; }
    public float getTotalSpeedBonus() { return totalSpeedBonus; }
    public float getTotalDefenseBonus() { return 0; } // Not used
    public float getTotalCritChance() { return 0; } // Not used
    public float getTotalDodgeChance() { return 0; } // Not used
    public float getTotalCritDamageBonus() { return 0; } // Not used
    public float getTotalTrapResistance() { return 0; } // Not used
    public float getTotalFogResistance() { return 0; } // Not used
    public float getTotalItemEffectBonus() { return 0; } // Not used
    public int getTotalExtraLives() { return 0; } // Not used

    // Special abilities
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

    // ==========================================
    // 🔥 新增：用于存档和读档的方法
    // ==========================================

    /**
     * 获取所有已解锁技能的 ID 列表 (用于存档)
     */
    public java.util.List<String> getUnlockedSkillIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        // 遍历所有技能节点，如果已解锁，就记下它的 ID
        // getAllNodes() 是你已经有的方法
        for (SkillNode node : getAllNodes().values()) {
            if (node.unlocked) {
                ids.add(node.id);
            }
        }
        return ids;
    }

    /**
     * 强制解锁技能 (用于读档，不扣点数，不检查前置条件)
     * 这个方法会在加载游戏时被调用，用于恢复玩家之前学的技能
     */
    public void forceUnlock(String skillId) {
        // 从 Map 中找到这个技能
        SkillNode node = getAllNodes().get(skillId);
        if (node != null) {
            node.unlocked = true;

            // 🔥 重要：解锁后不仅要把 unlocked 设为 true，还要把属性加成加上去！
            applySkillBonuses(node);
            activateSpecialAbilities(node);

            System.out.println("Restored skill from save: " + node.name);
        }
    }




}
