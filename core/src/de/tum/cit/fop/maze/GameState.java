package de.tum.cit.fop.maze;

/**
 * Represents the different states the game can be in.
 *
 * This enum is used to control the overall flow of the game,
 * such as which screen is shown and how input and updates are handled.
 *
 *
 */
public enum GameState{
    /**
     * The game is currently in the main menu.
     *
     * In this state, the player typically sees options like
     * "Start Game", "Settings", or "Exit". No gameplay logic is running.
     */
    MAIN_MENU,
    /**
     * The game is actively running.
     *
     * In this state, the player can move, enemies are updated,
     * collisions are checked, and the game world is rendered normally.
     */
    RUNNING,
    /**
     * The game is temporarily paused.
     *
     * Gameplay updates are stopped, but the game is not reset.
     * A pause menu may be shown, and player input is usually redirected
     * to UI elements instead of the game world.
     */
    PAUSED,
    /**
     * The player has lost the game.
     *
     * This state is entered when a losing condition is met,
     * such as the player's health reaching zero.
     */
    GAME_OVER,
    /**
     * The player has won the game.
     *
     * This state is entered when a winning condition is met,
     * such as reaching the exit or completing all objectives.
     */
    VICTORY
}

