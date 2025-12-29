package de.tum.cit.fop.maze.progression;

import java.util.HashMap;
import java.util.Map;

public class SkillTree {
    private Map<String, SkillNode> nodes = new HashMap<>();
    private ExperienceSystem expSystem;

    private float totalHealthBonus = 0;
    private float totalAttackBonus = 0;
    private float totalDefenseBonus = 0;
    private float totalSpeedBonus = 0;
    private float totalCritChance = 0;
    private float totalDodgeChance = 0;

    private boolean hasDoubleJump = false;
    private boolean hasDash = false;
    private boolean hasFireResistance = false;
    private boolean hasPoisonResistance = false;

    public SkillTree(ExperienceSystem expSystem) {
        this.expSystem = expSystem;
        initializeSkillTree();
    }

    private void initializeSkillTree() {
        createAttackBranch();
        createDefenseBranch();
        createUtilityBranch();
        createMagicBranch();

        updateSkillAvailability();
    }

    private void createAttackBranch() {
        SkillNode basicAttack = new SkillNode("attack_basic", "Basic Attack",
                "Attack +5", SkillNode.SkillType.ATTACK, 1, 0, 0);
        basicAttack.setAttackBonus(5);
        addNode(basicAttack);

        SkillNode powerAttack = new SkillNode("attack_power", "Power Attack",
                "Attack +10", SkillNode.SkillType.ATTACK, 2, 1, 0);
        powerAttack.setAttackBonus(10);
        powerAttack.addPrerequisite(basicAttack);
        addNode(powerAttack);

        SkillNode criticalStrike = new SkillNode("attack_critical", "Critical Strike",
                "Critical Chance +15%", SkillNode.SkillType.ATTACK, 3, 2, 0);
        criticalStrike.setCritChanceBonus(0.15f);
        criticalStrike.addPrerequisite(powerAttack);
        addNode(criticalStrike);
    }

    private void createDefenseBranch() {
        SkillNode basicHealth = new SkillNode("defense_health", "Health Boost",
                "Max Health +20", SkillNode.SkillType.DEFENSE, 1, 0, 1);
        basicHealth.setHealthBonus(20);
        addNode(basicHealth);

        SkillNode ironDefense = new SkillNode("defense_iron", "Iron Defense",
                "Defense +5", SkillNode.SkillType.DEFENSE, 2, 0, 2);
        ironDefense.setDefenseBonus(5);
        ironDefense.addPrerequisite(basicHealth);
        addNode(ironDefense);

        SkillNode agileDodge = new SkillNode("defense_dodge", "Agile Dodge",
                "Dodge Chance +10%", SkillNode.SkillType.DEFENSE, 3, 0, 3);
        agileDodge.setDodgeChanceBonus(0.1f);
        agileDodge.addPrerequisite(ironDefense);
        addNode(agileDodge);
    }

    private void createUtilityBranch() {
        SkillNode swiftStep = new SkillNode("utility_speed", "Swift Step",
                "Movement Speed +20%", SkillNode.SkillType.UTILITY, 1, 1, 1);
        swiftStep.setSpeedBonus(0.2f);
        addNode(swiftStep);

        SkillNode doubleJump = new SkillNode("utility_doublejump", "Double Jump",
                "Can jump again in mid-air", SkillNode.SkillType.UTILITY, 2, 2, 1);
        doubleJump.setUnlocksDoubleJump(true);
        doubleJump.addPrerequisite(swiftStep);
        addNode(doubleJump);

        SkillNode dash = new SkillNode("utility_dash", "Dash",
                "Short distance dash ability", SkillNode.SkillType.UTILITY, 3, 3, 1);
        dash.setUnlocksDash(true);
        dash.addPrerequisite(doubleJump);
        addNode(dash);
    }

    private void createMagicBranch() {
        SkillNode fireResist = new SkillNode("magic_fire", "Fire Resistance",
                "Reduces fire damage taken", SkillNode.SkillType.MAGIC, 2, 2, 2);
        fireResist.setUnlocksFireResistance(true);
        addNode(fireResist);

        SkillNode poisonResist = new SkillNode("magic_poison", "Poison Resistance",
                "Reduces poison damage taken", SkillNode.SkillType.MAGIC, 2, 3, 2);
        poisonResist.setUnlocksPoisonResistance(true);
        addNode(poisonResist);
    }

    public boolean unlockSkill(String skillId) {
        SkillNode node = nodes.get(skillId);
        if (node == null) {
            System.err.println("Skill not found: " + skillId);
            return false;
        }

        if (node.isUnlocked()) {
            System.err.println("Skill already unlocked: " + skillId);
            return false;
        }

        if (!node.canUnlock()) {
            System.err.println("Cannot unlock skill: " + skillId + " (prerequisites not met or not enough skill points)");
            return false;
        }

        if (!expSystem.useSkillPoint()) {
            System.err.println("Not enough skill points");
            return false;
        }

        if (node.unlock()) {
            applySkillEffects(node);
            updateSkillAvailability();
            System.out.println("Successfully unlocked skill: " + node.getName());
            return true;
        }

        return false;
    }

    private void applySkillEffects(SkillNode skill) {
        totalHealthBonus += skill.getHealthBonus();
        totalAttackBonus += skill.getAttackBonus();
        totalDefenseBonus += skill.getDefenseBonus();
        totalSpeedBonus += skill.getSpeedBonus();
        totalCritChance += skill.getCritChanceBonus();
        totalDodgeChance += skill.getDodgeChanceBonus();

        if (skill.unlocksDoubleJump()) hasDoubleJump = true;
        if (skill.unlocksDash()) hasDash = true;
        if (skill.unlocksFireResistance()) hasFireResistance = true;
        if (skill.unlocksPoisonResistance()) hasPoisonResistance = true;
    }

    public void updateSkillAvailability() {
        for (SkillNode node : nodes.values()) {
            if (!node.isUnlocked()) {
                boolean available = true;

                for (SkillNode prereq : node.getPrerequisites()) {
                    if (!prereq.isUnlocked()) {
                        available = false;
                        break;
                    }
                }

                if (available && expSystem.getSkillPoints() < node.getCost()) {
                    available = false;
                }

                node.setAvailable(available);
            }
        }
    }

    public void reset() {
        for (SkillNode node : nodes.values()) {
            if (node.isUnlocked()) {
                node.reset();
            }
        }

        totalHealthBonus = 0;
        totalAttackBonus = 0;
        totalDefenseBonus = 0;
        totalSpeedBonus = 0;
        totalCritChance = 0;
        totalDodgeChance = 0;

        hasDoubleJump = false;
        hasDash = false;
        hasFireResistance = false;
        hasPoisonResistance = false;

        updateSkillAvailability();
        System.out.println("Skill tree reset");
    }

    public SkillNode getNode(String id) {
        return nodes.get(id);
    }

    public Map<String, SkillNode> getAllNodes() {
        return nodes;
    }

    public Map<String, SkillNode> getUnlockedNodes() {
        Map<String, SkillNode> unlocked = new HashMap<>();
        for (Map.Entry<String, SkillNode> entry : nodes.entrySet()) {
            if (entry.getValue().isUnlocked()) {
                unlocked.put(entry.getKey(), entry.getValue());
            }
        }
        return unlocked;
    }

    private void addNode(SkillNode node) {
        nodes.put(node.getId(), node);
    }

    public float getTotalHealthBonus() { return totalHealthBonus; }
    public float getTotalAttackBonus() { return totalAttackBonus; }
    public float getTotalDefenseBonus() { return totalDefenseBonus; }
    public float getTotalSpeedBonus() { return totalSpeedBonus; }
    public float getTotalCritChance() { return totalCritChance; }
    public float getTotalDodgeChance() { return totalDodgeChance; }

    public boolean hasDoubleJump() { return hasDoubleJump; }
    public boolean hasDash() { return hasDash; }
    public boolean hasFireResistance() { return hasFireResistance; }
    public boolean hasPoisonResistance() { return hasPoisonResistance; }
}
