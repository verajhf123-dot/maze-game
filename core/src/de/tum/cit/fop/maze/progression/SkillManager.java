package de.tum.cit.fop.maze.progression;

import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.GameScreen;
import de.tum.cit.fop.maze.enemies.Enemy;
import com.badlogic.gdx.math.Vector2;

public class SkillManager {

    private Player player;
    private PlayerStats stats;
    private SkillTree skillTree;
    private GameScreen gameScreen;

    // Dash cooldown
    private float dashCooldown = 2.0f;
    private float currentDashCooldown = 0f;
    private boolean isDashing = false;
    private float dashDuration = 0.3f;
    private float dashTimer = 0f;

    // Double jump
    private int remainingDoubleJumps = 0;
    private boolean canDoubleJump = false;

    // Skill effect timer
    private float skillEffectTimer = 0f;
    private String currentSkillEffect = "";
    private Vector2 skillEffectPosition = new Vector2();

    public SkillManager(Player player, PlayerStats stats) {
        this.player = player;
        this.stats = stats;
        this.skillTree = stats.getSkillTree();
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void update(float delta) {
        // Update dash cooldown
        if (currentDashCooldown > 0) {
            currentDashCooldown -= delta;
        }

        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0) {
                isDashing = false;
            }
        }

        // Update skill tree cooldowns
        if (skillTree != null) {
            skillTree.update(delta);
        }

        // Update skill effect
        if (skillEffectTimer > 0) {
            skillEffectTimer -= delta;
        }
    }

    // ===== Q/E/R SKILL METHODS =====

    public boolean useSkill(String key) {
        if (skillTree == null) {
            System.err.println("SkillTree not initialized");
            return false;
        }

        SkillTree.SkillNode skill = skillTree.useSkill(key);
        if (skill == null) {
            System.err.println("Cannot use skill with key: " + key);
            return false;
        }

        // Execute skill effect based on type
        boolean success = false;
        switch (skill.skillType) {
            case "fireball":
                success = castFireball(skill.skillValue);
                break;

            case "heal":
                success = castHeal(skill.skillValue);
                break;

            case "lightning":
                success = castLightning(skill.skillValue);
                break;

            case "shield":
                success = castShield(skill.skillValue, 5.0f);
                break;

            default:
                System.out.println("Unknown skill type: " + skill.skillType);
                break;
        }

        if (success) {
            // Record skill effect for visual feedback
            currentSkillEffect = skill.skillType;
            skillEffectTimer = 0.8f;
            if (player != null) {
                skillEffectPosition.set(player.getPosition());
            }
        }

        return success;
    }

    /**
     * Q Skill - Fireball
     */
    private boolean castFireball(float damage) {
        if (player == null) return false;

        System.out.println(" Casting Fireball! Damage: " + damage);

        // In a full implementation, this would create a projectile
        // For now, we'll apply damage to nearest enemy
        if (gameScreen != null) {
            // Get nearest enemy
            Enemy nearestEnemy = findNearestEnemy(200f);
            if (nearestEnemy != null) {
                float actualDamage = damage * (1 + skillTree.getTotalAttackBonus() * 0.1f);
                nearestEnemy.takeDamage(actualDamage);
                System.out.println("Fireball hit " + nearestEnemy.getClass().getSimpleName() +
                        " for " + actualDamage + " damage!");
                return true;
            } else {
                System.out.println("No enemy in range for fireball");
            }
        }

        return false;
    }

    /**
     * E Skill - Healing
     */
    private boolean castHeal(float healing) {
        if (player == null || stats == null) return false;

        System.out.println("💚 Casting Healing! Base healing: " + healing);

        // Calculate actual healing with bonuses
        float actualHealing = healing * (1 + skillTree.getTotalHealthBonus() * 0.02f);
        stats.heal((int)actualHealing);

        System.out.println("Player healed for " + (int)actualHealing + " HP");
        return true;
    }

    /**
     * R Skill - Lightning Chain
     */
    private boolean castLightning(float damage) {
        if (player == null) return false;

        System.out.println("⚡ Casting Lightning Chain! Base damage: " + damage);

        boolean hitAnyEnemy = false;
        if (gameScreen != null) {
            // In a full implementation, this would chain between multiple enemies
            // For now, damage all enemies in range
            for (Enemy enemy : gameScreen.getEnemies()) {
                if (enemy.isAlive()) {
                    float distance = player.getPosition().dst(enemy.getPosition());
                    if (distance <= 150f) { // 150 pixel range
                        float actualDamage = damage * (1 + skillTree.getTotalAttackBonus() * 0.15f);
                        enemy.takeDamage(actualDamage);
                        System.out.println("Lightning hit " + enemy.getClass().getSimpleName() +
                                " for " + actualDamage + " damage!");
                        hitAnyEnemy = true;
                    }
                }
            }

            if (!hitAnyEnemy) {
                System.out.println("No enemies in lightning range");
            }
        }

        return hitAnyEnemy;
    }

    /**
     * Shield Skill
     */
    private boolean castShield(float shieldValue, float duration) {
        System.out.println("🛡️ Activating Energy Shield! Shield: " + shieldValue + ", Duration: " + duration + "s");
        skillTree.activateShield(shieldValue, duration);
        return true;
    }

    /**
     * Find nearest enemy within range
     */
    private Enemy findNearestEnemy(float range) {
        if (gameScreen == null || player == null) return null;

        Enemy nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Enemy enemy : gameScreen.getEnemies()) {
            if (enemy.isAlive()) {
                float distance = player.getPosition().dst(enemy.getPosition());
                if (distance <= range && distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = enemy;
                }
            }
        }

        return nearest;
    }

    // ===== SPECIAL ABILITIES =====

    public boolean canDash() {
        return skillTree.hasDash() && currentDashCooldown <= 0;
    }

    public void performDash(float directionX, float directionY) {
        if (!canDash()) return;

        isDashing = true;
        dashTimer = dashDuration;
        currentDashCooldown = dashCooldown;

        float dashSpeed = 500f * (1 + skillTree.getTotalSpeedBonus());
        System.out.println("💨 Dashing with speed: " + dashSpeed);

        // Apply dash effect
        currentSkillEffect = "dash";
        skillEffectTimer = 0.5f;
    }

    public boolean canDoubleJump() {
        return skillTree.hasDoubleJump() && remainingDoubleJumps > 0;
    }

    public void performDoubleJump() {
        if (!canDoubleJump()) return;

        remainingDoubleJumps--;
        System.out.println("🦘 Double jump! Remaining: " + remainingDoubleJumps);

        // Apply double jump effect
        currentSkillEffect = "jump";
        skillEffectTimer = 0.3f;
    }

    public void resetDoubleJumps() {
        if (skillTree.hasDoubleJump()) {
            remainingDoubleJumps = 1;
        }
    }

    // ===== SKILL BONUS APPLICATIONS =====

    public float applySkillBonusesToDamage(float baseDamage) {
        float modifiedDamage = baseDamage;

        // Attack bonus from skill tree
        modifiedDamage *= (1 + skillTree.getTotalAttackBonus() * 0.1f);

        // Critical hit chance (simplified)
        if (Math.random() < 0.1f) { // 10% base crit chance
            modifiedDamage *= 1.5f;
            System.out.println("CRITICAL HIT! Damage: " + modifiedDamage);
        }

        return modifiedDamage;
    }

    public float applySkillBonusesToHealing(float baseHealing) {
        float modifiedHealing = baseHealing;

        // Healing bonus from health skills
        modifiedHealing *= (1 + skillTree.getTotalHealthBonus() * 0.02f);

        return modifiedHealing;
    }

    public float applyTrapResistance(float trapDamage) {
        float modifiedDamage = trapDamage;

        // Apply shield if active
        if (skillTree.hasShieldActive()) {
            modifiedDamage = skillTree.applyShield(trapDamage);
        }

        return Math.max(modifiedDamage, 1);
    }

    public float applyFogResistance(float fogReduction) {
        float actualReduction = fogReduction;
        // Currently no fog resistance in skill tree
        return Math.max(actualReduction, 0.1f);
    }

    public boolean hasSpecialAbility(String ability) {
        switch (ability) {
            case "doubleJump":
                return skillTree.hasDoubleJump();
            case "dash":
                return skillTree.hasDash();
            case "fireResistance":
                return skillTree.hasFireResistance();
            case "poisonResistance":
                return skillTree.hasPoisonResistance();
            case "phasing":
                return skillTree.hasPhasing();
            default:
                return false;
        }
    }

    // ===== GETTER METHODS =====

    public SkillTree getSkillTree() { return skillTree; }

    public String getCurrentSkillEffect() {
        return skillEffectTimer > 0 ? currentSkillEffect : "";
    }

    public Vector2 getSkillEffectPosition() { return skillEffectPosition; }

    public float getSkillEffectTimer() { return skillEffectTimer; }

    public boolean isDashing() { return isDashing; }

    public float getDashCooldown() { return currentDashCooldown; }

    public float getDashCooldownPercent() {
        return Math.min(1.0f, currentDashCooldown / dashCooldown);
    }

    // Get skill cooldown info for UI
    public float getQCooldown() {
        return skillTree != null ? skillTree.getQCooldown() : 0;
    }

    public float getECooldown() {
        return skillTree != null ? skillTree.getECooldown() : 0;
    }

    public float getRCooldown() {
        return skillTree != null ? skillTree.getRCooldown() : 0;
    }

    public float getQCooldownPercent() {
        return skillTree != null ? skillTree.getQCooldownPercent() : 0;
    }

    public float getECooldownPercent() {
        return skillTree != null ? skillTree.getECooldownPercent() : 0;
    }

    public float getRCooldownPercent() {
        return skillTree != null ? skillTree.getRCooldownPercent() : 0;
    }

    public boolean canUseQSkill() {
        return skillTree != null && skillTree.canUseSkill("Q");
    }

    public boolean canUseESkill() {
        return skillTree != null && skillTree.canUseSkill("E");
    }

    public boolean hasQSkill() {
        return skillTree != null && skillTree.hasQSkill();
    }

    public boolean hasESkill() {
        return skillTree != null && skillTree.hasESkill();
    }

    public boolean hasRSkill() {
        return skillTree != null && skillTree.hasRSkill();
    }

    /**
     * Get skill summary for UI
     */
    public String getSkillSummary() {
        if (skillTree == null) return "Skill system not initialized";

        StringBuilder sb = new StringBuilder();
        sb.append("=== ACTIVE SKILLS ===\n");

        if (hasQSkill()) {
            SkillTree.SkillNode q = skillTree.getQSkill();
            float cd = getQCooldown();
            sb.append("Q: ").append(q.name);
            sb.append(cd > 0 ? String.format(" (%.1fs)", cd) : " [READY]").append("\n");
        }

        if (hasESkill()) {
            SkillTree.SkillNode e = skillTree.getESkill();
            float cd = getECooldown();
            sb.append("E: ").append(e.name);
            sb.append(cd > 0 ? String.format(" (%.1fs)", cd) : " [READY]").append("\n");
        }

        if (hasRSkill()) {
            SkillTree.SkillNode r = skillTree.getRSkill();
            float cd = getRCooldown();
            sb.append("R: ").append(r.name);
            sb.append(cd > 0 ? String.format(" (%.1fs)", cd) : " [READY]").append("\n");
        }

        if (skillTree.hasDash()) {
            float dashCd = getDashCooldown();
            sb.append("Dash (Shift)");
            sb.append(dashCd > 0 ? String.format(" (%.1fs)", dashCd) : " [READY]").append("\n");
        }

        if (skillTree.hasDoubleJump()) {
            sb.append("Double Jump: Available\n");
        }

        if (skillTree.hasShieldActive()) {
            sb.append(String.format("Shield: %.0f HP (%.1fs)\n",
                    skillTree.getShieldValue(), skillTree.getShieldTimer()));
        }

        return sb.toString();
    }
}
