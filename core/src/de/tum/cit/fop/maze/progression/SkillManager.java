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

    // === 🔥 1. 新增变量：防止每帧重复造成伤害 ===
    private boolean hasDealtDamage = false;

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
            // System.err.println("Cannot use skill with key: " + key); // 注释掉，防止没学技能时一直报错
            return false;
        }

        // Execute skill effect based on type
        boolean success = false;
        switch (skill.skillType) {
            case "fireball":
                // 我们在 GameScreen 里处理生成火球，这里只返回 true 表示技能释放成功（进入冷却）
                // 具体的发射逻辑移交给了 GameScreen
                success = true;
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
            skillEffectTimer = 0.5f; // 特效持续 0.5 秒

            // === 🔥 2. 重置伤害标记：新技能还没造成过伤害 ===
            hasDealtDamage = false;

            if (player != null) {
                // 记录技能释放时的位置（火球从这里发射）
                skillEffectPosition.set(player.getPosition());
            }
        }

        return success;
    }

    /**
     * Q Skill - Fireball
     */
    public float calculateFireballDamage(float baseDamage) { // 改名并将返回值改为 float
        if (player == null) return 0f;
        // 计算最终伤害（包含天赋加成）
        return baseDamage * (1 + skillTree.getTotalAttackBonus() * 0.1f);
    }

    /**
     * E Skill - Healing
     */
    private boolean castHeal(float healing) {
        if (player == null || stats == null) return false;

        System.out.println("💚 Casting Healing! Base healing: " + healing);

        stats.heal((int)healing);

        return true;
    }

    /**
     * R Skill - Lightning Chain
     */
    private boolean castLightning(float damage) {
        if (player == null) return false;
        System.out.println("⚡ Casting Lightning Chain!");

        // === 🔥 3. 移除直接伤害逻辑 ===
        // 同样，闪电的 AOE 伤害现在由 GameScreen 的循环来判断。
        // 这样可以确保视觉特效和伤害发生的位置是一致的。

        return true;
    }

    /**
     * Shield Skill
     */
    private boolean castShield(float shieldValue, float duration) {
        System.out.println("🛡️ Activating Energy Shield! Shield: " + shieldValue + ", Duration: " + duration + "s");
        skillTree.activateShield(shieldValue, duration);
        return true;
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
        // System.out.println("💨 Dashing with speed: " + dashSpeed);

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
        modifiedDamage *= (1 + skillTree.getTotalAttackBonus() * 0.1f);
        if (Math.random() < 0.1f) {
            modifiedDamage *= 1.5f;
            System.out.println("CRITICAL HIT! Damage: " + modifiedDamage);
        }
        return modifiedDamage;
    }

    public float applySkillBonusesToHealing(float baseHealing) {
        return baseHealing;
    }

    public float applyTrapResistance(float trapDamage) {
        float modifiedDamage = trapDamage;
        if (skillTree.hasShieldActive()) {
            modifiedDamage = skillTree.applyShield(trapDamage);
        }
        return Math.max(modifiedDamage, 1);
    }

    public float applyFogResistance(float fogReduction) {
        float actualReduction = fogReduction;
        return Math.max(actualReduction, 0.1f);
    }

    public boolean hasSpecialAbility(String ability) {
        switch (ability) {
            case "doubleJump": return skillTree.hasDoubleJump();
            case "dash": return skillTree.hasDash();
            case "fireResistance": return skillTree.hasFireResistance();
            case "poisonResistance": return skillTree.hasPoisonResistance();
            case "phasing": return skillTree.hasPhasing();
            default: return false;
        }
    }

    // ===== GETTER METHODS =====

    // === 🔥 4. 新增 Getter/Setter 供 GameScreen 调用 ===
    public boolean hasDealtDamage() { return hasDealtDamage; }
    public void setHasDealtDamage(boolean val) { this.hasDealtDamage = val; }
    // =======================================================

    public SkillTree getSkillTree() { return skillTree; }

    public String getCurrentSkillEffect() {
        return skillEffectTimer > 0 ? currentSkillEffect : "";
    }

    public Vector2 getSkillEffectPosition() { return skillEffectPosition; }

    public float getSkillEffectTimer() { return skillEffectTimer; }

    public boolean isDashing() { return isDashing; }

    public float getDashCooldown() { return currentDashCooldown; }

    // UI Helpers
    public float getQCooldown() { return skillTree != null ? skillTree.getQCooldown() : 0; }
    public float getECooldown() { return skillTree != null ? skillTree.getECooldown() : 0; }
    public float getRCooldown() { return skillTree != null ? skillTree.getRCooldown() : 0; }

    public float getQCooldownPercent() { return skillTree != null ? skillTree.getQCooldownPercent() : 0; }
    public float getECooldownPercent() { return skillTree != null ? skillTree.getECooldownPercent() : 0; }
    public float getRCooldownPercent() { return skillTree != null ? skillTree.getRCooldownPercent() : 0; }

    public boolean hasQSkill() { return skillTree != null && skillTree.hasQSkill(); }
    public boolean hasESkill() { return skillTree != null && skillTree.hasESkill(); }
    public boolean hasRSkill() { return skillTree != null && skillTree.hasRSkill(); }

    public String getSkillSummary() {
        if (skillTree == null) return "Skill system not initialized";
        return "Skills Active"; // 简化返回，原逻辑没问题
    }

    public boolean canUseQSkill() {
        // 调用 SkillTree 的判断逻辑
        return skillTree != null && skillTree.canUseSkill("Q");
    }

    public boolean canUseESkill() {
        return skillTree != null && skillTree.canUseSkill("E");
    }

    public boolean canUseRSkill() {
        return skillTree != null && skillTree.canUseSkill("R");
    }
}