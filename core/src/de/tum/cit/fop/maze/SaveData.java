package de.tum.cit.fop.maze;

/**
 * SaveData 类用于序列化保存游戏进度。
 * (已重命名以避免与 GameState 枚举冲突)
 */
public class SaveData {
    private int currentLevel;
    private float playerHealth;
    private boolean hasKey;

    public SaveData() {}



    public SaveData(int level, float health, boolean hasKey) {
        this.currentLevel = level;
        this.playerHealth = health;
        this.hasKey = hasKey;
    }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public float getPlayerHealth() { return playerHealth; }
    public void setPlayerHealth(float playerHealth) { this.playerHealth = playerHealth; }

    public boolean isHasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }
}