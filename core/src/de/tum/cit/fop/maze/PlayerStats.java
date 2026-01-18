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

    private void updateStatsFromLevel() {
        // ▼▼▼ 修改：不再获取 expSystem.getHealthBonus()，只看技能树加成 ▼▼▼
        float skillHealthBonus = 0;
        if (skillTree != null) {
            skillHealthBonus = skillTree.getTotalHealthBonus();
        }

        maxHealth = baseMaxHealth + (int)skillHealthBonus;
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        if (skillTree != null) {
            canDoubleJump = skillTree.hasDoubleJump();
            canDash = skillTree.hasDash();
            fireResistance = skillTree.hasFireResistance();
            poisonResistance = skillTree.hasPoisonResistance();
        }

        criticalChance = skillTree != null ? skillTree.getTotalCritChance() : 0.0f;
        dodgeChance = skillTree != null ? skillTree.getTotalDodgeChance() : 0.0f;

        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    private float calculateDefenseBonus() {
        float levelDefense = expSystem.getDefenseBonus();
        float skillDefense = skillTree != null ? skillTree.getTotalDefenseBonus() : 0;
        return levelDefense + skillDefense;
    }

    // === 核心逻辑：受伤处理 (护盾/闪避/抗性) ===
    public void takeDamage(float damage, String damageType) {
        // 1. 闪避
        if (skillManager != null && skillManager.hasSpecialAbility("dodge") && Math.random() < dodgeChance) {
            System.out.println("Dodged!");
            return;
        }

        float finalDamage = damage;
        // 2. 抗性
        if (damageType.equals(DAMAGE_TYPE_FIRE) && fireResistance) finalDamage *= 0.5f;
        if (damageType.equals(DAMAGE_TYPE_POISON) && poisonResistance) finalDamage *= 0.5f;

        // 3. 防御
        finalDamage -= calculateDefenseBonus() * 0.5f;
        if (finalDamage < 1) finalDamage = 1;

        // 4. 🔥 护盾 (Shield) 🔥
        if (skillTree != null && skillTree.hasShieldActive()) {
            finalDamage = skillTree.applyShield(finalDamage);
        }

        // 5. 其他 Buff
        if (skillManager != null) {
            finalDamage = skillManager.applyTrapResistance(finalDamage);
        }

        // 6. 最终结算
        if (finalDamage > 0) {
            health -= (int) finalDamage;
            if (health < 0) health = 0;
            System.out.println("Damage taken: " + (int)finalDamage + ". Current HP: " + health);
        } else {
            System.out.println("Damage blocked by Shield!");
        }
    }

    public void takeDamage(int damage) { takeDamage((float)damage, DAMAGE_TYPE_PHYSICAL); }

    public float getActualAttackDamage() {
        float baseDamage = 10f;
        // float levelBonus = expSystem.getAttackBonus(); // <--- 删掉这行
        float skillBonus = skillTree != null ? skillTree.getTotalAttackBonus() : 0;

        float totalDamage = baseDamage + skillBonus; // <--- 这里不加 levelBonus

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

    // === 核心逻辑：更新时钟 ===
    public void update(float delta) {
        if (skillTree != null) skillTree.update(delta);
        if (skillManager != null) skillManager.update(delta);
    }

    // Getters/Setters 保持不变
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