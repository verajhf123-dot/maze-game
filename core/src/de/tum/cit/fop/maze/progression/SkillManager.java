package de.tum.cit.fop.maze.progression;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.GameScreen;

/**
 * SkillManager handles active skill usage, dash, double jump, cooldowns, and skill effects.
 * Fully adapted to the current SkillTree implementation.
 */
public class SkillManager {

    private final Player player;
    private final PlayerStats stats;
    private final SkillTree skillTree;
    private GameScreen gameScreen;

    // --- Dash ---
    private float dashCooldown = 2.0f;
    private float currentDashCooldown = 0f;
    private boolean isDashing = false;
    private float dashDuration = 0.3f;
    private float dashTimer = 0f;

    // --- Double Jump ---
    private int remainingDoubleJumps = 0;

    // --- Skill effect tracking ---
    private float skillEffectTimer = 0f;
    private String currentSkillEffect = "";
    private final Vector2 skillEffectPosition = new Vector2();
    private boolean hasDealtDamage = false;

    public SkillManager(Player player, PlayerStats stats) {
        this.player = player;
        this.stats = stats;
        this.skillTree = stats.getSkillTree();
    }

    public void setGameScreen(GameScreen screen) { this.gameScreen = screen; }

    public void update(float delta) {
        // Dash cooldown
        if (currentDashCooldown > 0f) currentDashCooldown -= delta;

        // Dash duration
        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) isDashing = false;
        }

        // SkillTree cooldown updates
        if (skillTree != null) skillTree.update(delta);

        // Skill effect timer
        if (skillEffectTimer > 0f) skillEffectTimer -= delta;
    }

    // ----------------- Active Skill Usage -----------------
    public boolean useSkill(String key) {
        if (skillTree == null) return false;
        SkillTree.SkillNode node = skillTree.useSkill(key);
        if (node == null) return false;

        boolean success = false;

        switch (node.skillType) {
            case "fireball": success = true; break;
            case "heal": success = castHeal(node.skillValue); break;
            case "lightning": success = castLightning(node.skillValue); break;
            case "shield": success = castShield(node.skillValue, node.skillCooldown); break;
            default: break;
        }

        if (success) {
            currentSkillEffect = node.skillType;
            skillEffectTimer = 0.5f;
            hasDealtDamage = false;
            if (player != null) skillEffectPosition.set(player.getPosition());
        }

        return success;
    }

    private boolean castHeal(float healAmount) {
        if (player == null || stats == null) return false;
        stats.heal((int) healAmount);
        return true;
    }

    private boolean castLightning(float damage) {
        // lightning effect handled elsewhere
        return true;
    }

    private boolean castShield(float value, float duration) {
        skillTree.activateShield(value, duration);
        return true;
    }

    // ----------------- Dash / Double Jump -----------------
    public boolean canDash() {
        return skillTree.hasDash() && currentDashCooldown <= 0f;
    }

    public void performDash(float dirX, float dirY) {
        if (!canDash()) return;
        isDashing = true;
        dashTimer = dashDuration;
        currentDashCooldown = dashCooldown;
        currentSkillEffect = "dash";
        skillEffectTimer = 0.5f;
    }

    public boolean canDoubleJump() {
        return skillTree.hasDoubleJump() && remainingDoubleJumps > 0;
    }

    public void performDoubleJump() {
        if (!canDoubleJump()) return;
        remainingDoubleJumps--;
        currentSkillEffect = "jump";
        skillEffectTimer = 0.3f;
    }

    public void resetDoubleJumps() {
        if (skillTree.hasDoubleJump()) remainingDoubleJumps = 1;
    }

    // ----------------- Misc -----------------
    public float getQCooldown() { return skillTree.getQSkill() != null ? skillTree.getQSkill().skillCooldown : 0f; }
    public float getECooldown() { return skillTree.getESkill() != null ? skillTree.getESkill().skillCooldown : 0f; }
    public float getRCooldown() { return skillTree.getRSkill() != null ? skillTree.getRSkill().skillCooldown : 0f; }

    public boolean hasQSkill() { return skillTree.getQSkill() != null; }
    public boolean hasESkill() { return skillTree.getESkill() != null; }
    public boolean hasRSkill() { return skillTree.getRSkill() != null; }

    public boolean canUseQSkill() { return skillTree.canUseSkill("Q"); }
    public boolean canUseESkill() { return skillTree.canUseSkill("E"); }
    public boolean canUseRSkill() { return skillTree.canUseSkill("R"); }

    public boolean hasSpecialAbility(String ability) {
        switch (ability) {
            case "doubleJump": return skillTree.hasDoubleJump();
            case "dash": return skillTree.hasDash();
            case "fireResistance": return skillTree.hasFireResistance();
            case "poisonResistance": return skillTree.hasPoisonResistance();
            case "phasing": return false; // not implemented
            default: return false;
        }
    }

    // ----------------- Getters / Setters -----------------
    public SkillTree getSkillTree() { return skillTree; }
    public String getCurrentSkillEffect() { return skillEffectTimer > 0 ? currentSkillEffect : ""; }
    public Vector2 getSkillEffectPosition() { return skillEffectPosition; }
    public float getSkillEffectTimer() { return skillEffectTimer; }
    public boolean isDashing() { return isDashing; }
    public float getDashCooldown() { return currentDashCooldown; }
    public boolean hasDealtDamage() { return hasDealtDamage; }
    public void setHasDealtDamage(boolean val) { hasDealtDamage = val; }

    public float calculateFireballDamage(float baseDamage) {
        if (player == null) return 0f;
        float bonusMultiplier = 1 + skillTree.getTotalAttackBonus() * 0.1f;
        return baseDamage * bonusMultiplier;
    }

    public float getTotalDefenseBonus() {
        if (skillTree == null) return 0f;

        // 用 totalHealthBonus 和 totalDodgeChance 近似防御
        float defense = skillTree.getTotalHealthBonus() * 0.1f;
        defense += skillTree.getTotalDodgeChance() * 50f; // 可调
        return defense;
    }

    /** Apply trap resistance to incoming damage (example: 20% reduction if relevant) */
    public float applyTrapResistance(float damage) {
        // For now, let's just reduce 20% if player has dash (example)
        if (skillTree.hasDash()) {
            damage *= 0.8f;
        }
        return damage;
    }

    /** Apply skill bonuses to outgoing damage */
    public float applySkillBonusesToDamage(float damage) {
        if (skillTree == null) return damage;
        return damage + skillTree.getTotalAttackBonus();
    }

    /** Apply skill bonuses to healing */
    public float applySkillBonusesToHealing(float healAmount) {
        if (skillTree == null) return healAmount;
        // Example: each point of health bonus increases healing by 10%
        return healAmount * (1f + skillTree.getTotalHealthBonus() * 0.01f);
    }
}