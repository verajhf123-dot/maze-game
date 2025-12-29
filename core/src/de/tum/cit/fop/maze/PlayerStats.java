package de.tum.cit.fop.maze;

import de.tum.cit.fop.maze.progression.ExperienceSystem;
import de.tum.cit.fop.maze.progression.SkillTree;

public class PlayerStats {

    private int maxHealth = 5;
    private int health = 5;
    private boolean hasKey = false;
    private int bonusKey = 0;

    private ExperienceSystem expSystem;
    private SkillTree skillTree;

    private boolean canDoubleJump = false;
    private boolean canDash = false;
    private boolean fireResistance = false;
    private boolean poisonResistance = false;
    private float criticalChance = 0.0f;
    private float dodgeChance = 0.0f;

    private int baseMaxHealth = 5;
    private float baseSpeedMultiplier = 1.0f;
    private float baseAttackMultiplier = 1.0f;
    private float baseDefenseMultiplier = 1.0f;

    public PlayerStats() {
        expSystem = new ExperienceSystem();

        skillTree = new SkillTree(expSystem);

        expSystem.addListener(new ExperienceSystem.ExpListener() {
            @Override
            public void onExpGained(int amount, int total) {
                System.out.println("Gained exp: " + amount + ", total: " + total);
            }

            @Override
            public void onLevelUp(int newLevel, int skillPoints) {
                System.out.println("Level up to " + newLevel + "! Skill points: " + skillPoints);
                updateStatsFromLevel();
                heal(maxHealth);
            }

            @Override
            public void onSkillPointsChanged(int points) {
                System.out.println("Skill points: " + points);
                skillTree.updateSkillAvailability();
            }
        });
    }

    private void updateStatsFromLevel() {
        int levelHealthBonus = expSystem.getHealthBonus();
        int levelAttackBonus = expSystem.getAttackBonus();
        int levelDefenseBonus = expSystem.getDefenseBonus();

        float skillHealthBonus = skillTree.getTotalHealthBonus();
        float skillAttackBonus = skillTree.getTotalAttackBonus();
        float skillDefenseBonus = skillTree.getTotalDefenseBonus();
        float skillSpeedBonus = skillTree.getTotalSpeedBonus();

        maxHealth = baseMaxHealth + levelHealthBonus + (int)skillHealthBonus;

        canDoubleJump = skillTree.hasDoubleJump();
        canDash = skillTree.hasDash();
        fireResistance = skillTree.hasFireResistance();
        poisonResistance = skillTree.hasPoisonResistance();

        criticalChance = skillTree.getTotalCritChance();
        dodgeChance = skillTree.getTotalDodgeChance();

        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public void gainExpFromKill(String enemyType) {
        int exp = ExperienceSystem.getExpForEnemy(enemyType);
        expSystem.gainExp(exp);
    }

    public boolean unlockSkill(String skillId) {
        boolean success = skillTree.unlockSkill(skillId);
        if (success) {
            updateStatsFromLevel();
        }
        return success;
    }

    public boolean isCriticalHit() {
        return Math.random() < criticalChance;
    }

    public boolean dodgeAttack() {
        return Math.random() < dodgeChance;
    }

    public void takeDamage(float damage, String damageType) {
        if (dodgeAttack()) {
            System.out.println("Dodged attack!");
            return;
        }

        float finalDamage = damage;

        if (damageType.equals("fire") && fireResistance) {
            finalDamage *= 0.5f;
        }
        if (damageType.equals("poison") && poisonResistance) {
            finalDamage *= 0.5f;
        }

        float defenseBonus = expSystem.getDefenseBonus() + skillTree.getTotalDefenseBonus();
        finalDamage -= defenseBonus * 0.5f;

        if (finalDamage < 1) finalDamage = 1;

        health -= (int) finalDamage;
        if (health < 0) health = 0;

        System.out.println("Took " + (int)finalDamage + " damage, health remaining: " + health);
    }

    public void takeDamage(int damage) {
        takeDamage((float)damage, "physical");
    }

    public void heal(int value) {
        health += value;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public void resetProgression() {
        expSystem.reset();
        skillTree.reset();
        updateStatsFromLevel();
        health = maxHealth;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public boolean isDead() {
        return health <= 0;
    }


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

    public int getBonusKey() { return bonusKey; }
    public void setBonusKey(int bonusKey) { this.bonusKey = bonusKey; }

    public ExperienceSystem getExpSystem() { return expSystem; }
    public SkillTree getSkillTree() { return skillTree; }

    public boolean canDoubleJump() { return canDoubleJump; }
    public boolean canDash() { return canDash; }
    public boolean hasFireResistance() { return fireResistance; }
    public boolean hasPoisonResistance() { return poisonResistance; }

    public float getCriticalChance() { return criticalChance; }
    public float getDodgeChance() { return dodgeChance; }
}
