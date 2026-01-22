package de.tum.cit.fop.maze;

import de.tum.cit.fop.maze.progression.ExperienceSystem;
import de.tum.cit.fop.maze.progression.SkillTree;
import de.tum.cit.fop.maze.progression.SkillManager;

public class PlayerStats {

    public static final String DAMAGE_TYPE_PHYSICAL = "physical";
    public static final String DAMAGE_TYPE_FIRE = "fire";
    public static final String DAMAGE_TYPE_POISON = "poison";

    private int maxHealth = 100;
    private int health = 100;
    private int score = 0;
    private boolean hasExitKey = false;
    private int bonusKeys = 0;

    private ExperienceSystem expSystem;
    private SkillTree skillTree;
    private SkillManager skillManager;

    private boolean canDoubleJump = false;
    private boolean canDash = false;
    private boolean fireResistance = false;
    private boolean poisonResistance = false;
    private float criticalChance = 0.0f;
    private float dodgeChance = 0.0f;

    private int baseMaxHealth = 100;
    private Player player;

    public PlayerStats() {
        expSystem = new ExperienceSystem();
        skillTree = new SkillTree(expSystem);
        skillManager = null;
        updateStatsFromLevel();
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.skillManager = new SkillManager(player, this);
        updateStatsFromLevel();
    }

    public SkillManager getSkillManager() { return skillManager; }
    public SkillTree getSkillTree() { return skillTree; }

    // === 核心修复方法：从技能树读取最新属性 ===
    private void updateStatsFromLevel() {
        // 1. 计算血量加成
        float skillHealthBonus = 0;
        if (skillTree != null) {
            skillHealthBonus = skillTree.getTotalHealthBonus();
        }

        // 2. 更新最大生命值 (基础 100 + 技能加成)
        int newMaxHealth = baseMaxHealth + (int)skillHealthBonus;

        // 只有当上限发生变化时才打印日志（避免刷屏）
        if (newMaxHealth != maxHealth) {
            // System.out.println("HP Limit Updated: " + maxHealth + " -> " + newMaxHealth);
            maxHealth = newMaxHealth;
        } else {
            maxHealth = newMaxHealth;
        }

        // 3. 更新其他能力
        if (skillTree != null) {
            canDoubleJump = skillTree.hasDoubleJump();
            canDash = skillTree.hasDash();
            fireResistance = skillTree.hasFireResistance();
            poisonResistance = skillTree.hasPoisonResistance();
            criticalChance = skillTree.getTotalCritChance();
            dodgeChance = skillTree.getTotalDodgeChance();
        }

        // 4. 确保当前血量不超过新上限
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    private float calculateDefenseBonus() {
        float levelDefense = expSystem.getDefenseBonus();
        float skillDefense = skillTree != null ? skillTree.getTotalDefenseBonus() : 0;
        return levelDefense + skillDefense;
    }

    public void takeDamage(float damage, String damageType) {
        if (skillManager != null && skillManager.hasSpecialAbility("dodge") && Math.random() < dodgeChance) {
            System.out.println("Dodged!");
            return;
        }

        float finalDamage = damage;
        if (damageType.equals(DAMAGE_TYPE_FIRE) && fireResistance) finalDamage *= 0.5f;
        if (damageType.equals(DAMAGE_TYPE_POISON) && poisonResistance) finalDamage *= 0.5f;

        finalDamage -= calculateDefenseBonus() * 0.5f;
        if (finalDamage < 1) finalDamage = 1;

        if (skillTree != null && skillTree.hasShieldActive()) {
            finalDamage = skillTree.applyShield(finalDamage);
        }

        if (skillManager != null) {
            finalDamage = skillManager.applyTrapResistance(finalDamage);
        }

        if (finalDamage > 0) {
            health -= (int) finalDamage;
            if (health < 0) health = 0;
        }
    }

    public void takeDamage(int damage) { takeDamage((float)damage, DAMAGE_TYPE_PHYSICAL); }

    public float getActualAttackDamage() {
        float baseDamage = 10f;
        float skillBonus = skillTree != null ? skillTree.getTotalAttackBonus() : 0;
        float totalDamage = baseDamage + skillBonus;

        if (skillManager != null) {
            totalDamage = skillManager.applySkillBonusesToDamage(totalDamage);
        }
        return totalDamage;
    }

    public void applySkillEffects() { updateStatsFromLevel(); }

    public void gainExpFromKill(String enemyType) {
        int amount = ExperienceSystem.getExpForEnemy(enemyType);
        expSystem.gainExp(amount);
    }

    public boolean unlockSkill(String skillId) {
        if (skillTree == null) return false;
        boolean s = skillTree.unlockSkill(skillId);
        if (s) applySkillEffects();
        return s;
    }

    public void heal(int value) {
        if (skillManager != null) {
            float v = skillManager.applySkillBonusesToHealing(value);
            health += (int)v;
        } else {
            health += value;
        }
        if (health > maxHealth) health = maxHealth;
    }

    public void resetProgression() {
        expSystem.setCurrentExp(0);
        if (skillTree != null) skillTree.reset();
        updateStatsFromLevel();
        health = maxHealth;
    }

    // === 这里是这次修改的关键点 ===
    public void update(float delta) {
        if (skillTree != null) skillTree.update(delta);
        if (skillManager != null) skillManager.update(delta);

        // 🔥 强制每一帧同步属性！这样你在菜单里点了技能，这里立刻就能知道。
        updateStatsFromLevel();
    }

    // Getters
    public int getScore() { return score; }
    public void addScore(int amount) { this.score += amount; }
    public void setScore(int score) { this.score = score; }
    public void collectBonusKey() { this.bonusKeys++; addScore(500); }
    public void collectExitKey() { this.hasExitKey = true; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return health <= 0; }
    public boolean hasKey() { return hasExitKey; }
    public void useKey() { if (hasExitKey) { hasExitKey = false; addScore(1000); } }
    public void setHasKey(boolean hasKey) { this.hasExitKey = hasKey; }
    public int getBonusKey() { return bonusKeys; }
    public void setBonusKey(int bonusKey) { this.bonusKeys = bonusKey; }
    public ExperienceSystem getExpSystem() { return expSystem; }
    public boolean canDoubleJump() { return canDoubleJump; }
    public boolean canDash() { return canDash; }
    public boolean hasFireResistance() { return fireResistance; }
    public boolean hasPoisonResistance() { return poisonResistance; }
    public float getCriticalChance() { return criticalChance; }
    public float getDodgeChance() { return dodgeChance; }
    public int getAvailableSkillPoints() { return expSystem.getSkillPoints(); }
    public void setCurrentHealth(int health) { this.health = health; if (this.health > maxHealth) this.health = maxHealth; }
}