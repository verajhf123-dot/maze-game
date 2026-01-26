package de.tum.cit.fop.maze.progression;

import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;
import de.tum.cit.fop.maze.GameScreen;
import de.tum.cit.fop.maze.enemies.Enemy;
import com.badlogic.gdx.math.Vector2;

public class SkillManager {

    // Reference to the player entity
    private Player player;

    // Player statistics and progression data
    private PlayerStats stats;

    // Skill tree handling unlocked skills and upgrades
    private SkillTree skillTree;

    // Reference to the current game screen
    private GameScreen gameScreen;

    // Dash cooldown settings
    private float dashCooldown = 2.0f;
    private float currentDashCooldown = 0f;

    // Dash state control
    private boolean isDashing = false;
    private float dashDuration = 0.3f;
    private float dashTimer = 0f;

    // Double jump control
    private int remainingDoubleJumps = 0;
    private boolean canDoubleJump = false;

    // Skill visual effect timing
    private float skillEffectTimer = 0f;
    private String currentSkillEffect = "";
    private Vector2 skillEffectPosition = new Vector2();

    // Flag for tracking damage events
    private boolean hasDealtDamage = false;

    // Creates a skill manager for the given player
    public SkillManager(Player player, PlayerStats stats) {
        this.player = player;
        this.stats = stats;
        this.skillTree = stats.getSkillTree();
    }

    // Sets the active game screen reference
    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    // Updates cooldowns, timers, and skill states
    public void update(float delta) {
        if (currentDashCooldown > 0) {
            currentDashCooldown -= delta;
        }

        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0) {
                isDashing = false;
            }
        }

        if (skillTree != null) {
            skillTree.update(delta);
        }

        if (skillEffectTimer > 0) {
            skillEffectTimer -= delta;
        }
    }

    // Attempts to activate a skill by key input
    public boolean useSkill(String key) {
        if (skillTree == null) {
            System.err.println("SkillTree not initialized");
            return false;
        }

        SkillTree.SkillNode skill = skillTree.useSkill(key);
        if (skill == null) {
            return false;
        }

        boolean success = false;
        switch (skill.skillType) {
            case "fireball":
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

        // Triggers visual feedback if skill activation succeeded
        if (success) {
            currentSkillEffect = skill.skillType;
            skillEffectTimer = 0.5f;
            hasDealtDamage = false;

            if (player != null) {
                skillEffectPosition.set(player.getPosition());
            }
        }

        return success;
    }

    // Calculates fireball damage with skill bonuses applied
    public float calculateFireballDamage(float baseDamage) {
        if (player == null) return 0f;
        return baseDamage * (1 + skillTree.getTotalAttackBonus() * 0.1f);
    }

    // Casts healing skill on the player
    private boolean castHeal(float healing) {
        if (player == null || stats == null) return false;

        System.out.println("Casting Healing! Base healing: " + healing);
        stats.heal((int) healing);
        return true;
    }

    // Executes lightning chain skill logic
    private boolean castLightning(float damage) {
        if (player == null) return false;
        System.out.println("Casting Lightning Chain!");
        return true;
    }

    // Activates temporary shield effect
    private boolean castShield(float shieldValue, float duration) {
        System.out.println("Activating Energy Shield! Shield: " + shieldValue + ", Duration: " + duration + "s");
        skillTree.activateShield(shieldValue, duration);
        return true;
    }

    // Checks if dash skill can be used
    public boolean canDash() {
        return skillTree.hasDash() && currentDashCooldown <= 0;
    }

    // Performs dash movement and sets cooldown
    public void performDash(float directionX, float directionY) {
        if (!canDash()) return;

        isDashing = true;
        dashTimer = dashDuration;
        currentDashCooldown = dashCooldown;

        float dashSpeed = 500f * (1 + skillTree.getTotalSpeedBonus());
        currentSkillEffect = "dash";
        skillEffectTimer = 0.5f;
    }

    // Checks if double jump is available
    public boolean canDoubleJump() {
        return skillTree.hasDoubleJump() && remainingDoubleJumps > 0;
    }

    // Performs a double jump action
    public void performDoubleJump() {
        if (!canDoubleJump()) return;

        remainingDoubleJumps--;
        currentSkillEffect = "jump";
        skillEffectTimer = 0.3f;
    }

    // Resets double jump count after landing
    public void resetDoubleJumps() {
        if (skillTree.hasDoubleJump()) {
            remainingDoubleJumps = 1;
        }
    }

    // Applies skill-based damage modifiers
    public float applySkillBonusesToDamage(float baseDamage) {
        float modifiedDamage = baseDamage;
        modifiedDamage *= (1 + skillTree.getTotalAttackBonus() * 0.1f);

        if (Math.random() < 0.1f) {
            modifiedDamage *= 1.5f;
            System.out.println("CRITICAL HIT! Damage: " + modifiedDamage);
        }
        return modifiedDamage;
    }

    // Applies healing modifiers (currently unused)
    public float applySkillBonusesToHealing(float baseHealing) {
        return baseHealing;
    }

    // Applies shield resistance to trap damage
    public float applyTrapResistance(float trapDamage) {
        float modifiedDamage = trapDamage;
        if (skillTree.hasShieldActive()) {
            modifiedDamage = skillTree.applyShield(trapDamage);
        }
        return Math.max(modifiedDamage, 1);
    }

    // Applies resistance to fog debuffs
    public float applyFogResistance(float fogReduction) {
        float actualReduction = fogReduction;
        return Math.max(actualReduction, 0.1f);
    }

    // Checks whether a specific ability is unlocked
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

    // Damage tracking flag
    public boolean hasDealtDamage() { return hasDealtDamage; }
    public void setHasDealtDamage(boolean val) { this.hasDealtDamage = val; }

    // Returns the skill tree reference
    public SkillTree getSkillTree() { return skillTree; }

    // Returns current active skill effect
    public String getCurrentSkillEffect() {
        return skillEffectTimer > 0 ? currentSkillEffect : "";
    }

    // Returns skill effect position
    public Vector2 getSkillEffectPosition() { return skillEffectPosition; }

    // Returns remaining effect time
    public float getSkillEffectTimer() { return skillEffectTimer; }

    // Returns dash state
    public boolean isDashing() { return isDashing; }

    // Returns current dash cooldown
    public float getDashCooldown() { return currentDashCooldown; }

    // Cooldown accessors for Q/E/R skills
    public float getQCooldown() { return skillTree != null ? skillTree.getQCooldown() : 0; }
    public float getECooldown() { return skillTree != null ? skillTree.getECooldown() : 0; }
    public float getRCooldown() { return skillTree != null ? skillTree.getRCooldown() : 0; }

    public float getQCooldownPercent() { return skillTree != null ? skillTree.getQCooldownPercent() : 0; }
    public float getECooldownPercent() { return skillTree != null ? skillTree.getECooldownPercent() : 0; }
    public float getRCooldownPercent() { return skillTree != null ? skillTree.getRCooldownPercent() : 0; }

    // Checks whether skills are unlocked
    public boolean hasQSkill() { return skillTree != null && skillTree.hasQSkill(); }
    public boolean hasESkill() { return skillTree != null && skillTree.hasESkill(); }
    public boolean hasRSkill() { return skillTree != null && skillTree.hasRSkill(); }

    // Returns brief skill system summary
    public String getSkillSummary() {
        if (skillTree == null) return "Skill system not initialized";
        return "Skills Active";
    }

    // Checks whether skills can currently be used
    public boolean canUseQSkill() {
        return skillTree != null && skillTree.canUseSkill("Q");
    }

    public boolean canUseESkill() {
        return skillTree != null && skillTree.canUseSkill("E");
    }

    public boolean canUseRSkill() {
        return skillTree != null && skillTree.canUseSkill("R");
    }
}