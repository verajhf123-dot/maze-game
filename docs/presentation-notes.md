
“LibGDX is a Java-based game development framework that provides rendering, 
input, audio, and animation utilities.LibGDX provides an Animation class
that manages frame-based animations.“The Animation object selects the current 
frame based on the elapsed time since the animation started.
During rendering, we query the animation with the current
state time to get the correct frame.

“We separate pixel-based size for rendering from tile-based size
for game logic.”

“Each GameScreen keeps a reference to the main game object, so it can access 
shared resources and switch between screens.”

GameScreen uses an orthographic camera for 2D rendering, 
a bitmap font for drawing text,
and a shape renderer for simple shapes and visual effects.

These fields support navigation and interaction logic, 
including pathfinding for enemies, collision checking, 
and detecting when the player reaches the exit.

“Enemies are stored in an array and represent 
all hostile entities currently active in the level.”

The walkable grid is a boolean map that indicates 
which tiles can be walked on and which cannot
GameScreen keeps track of the player and all enemies, 
and uses a simplified walkable grid to support
enemy movement and pathfinding.

gameTime tracks how long the current level has been running.

GameScreen tracks level information, handles player 
input through a controller, and manages core interactive objects 
such as the exit, keys, and doors.

“exitPosition stores the world coordinates of the exit.”

This timer tracks how long the player has been invulnerable after spawning

GameScreen stores entry and exit positions, uses a 
guidance arrow to help navigation, and applies a short
invulnerability period when the player spawns.

“SettingsManager stores and provides access to game configuration,
such as sound volume or gameplay preferences

previousStats stores player attributes from the previous level, 
allowing progress to carry over between levels.

initCommon is our helper method that initializes shared state for the screen,
so we don’t duplicate setup code across constructors.


if (this.previousStats != null) {
this.previousStats.setBonusKey(0);
this.previousStats.setHasKey(false);
}
initCommon();
}
We carry over persistent player progression across levels,
but we reset temporary per-level states like key ownership 
to ensure each level starts clean

initCommon initializes shared screen components: settings, 
camera and font for rendering, input controller, enemy container, 
UI stage, and the initial game state.

“We create a separate UI Stage using a ScreenViewport, 
rendered with the game’s shared SpriteBatch.”
In initCommon, we set up shared screen components: 
settings, the orthographic camera (including position and zoom), a font for HUD text, 
the input controller, and an enemy container. We also initialize a separate UI stage 
and set the initial game state to RUNNING, then build the pause menu UI.

We use a shared TextButtonStyle so all buttons look consistent
We also implement a fallback to the default Skin style in case 
the custom texture fails to load


Pause Menu: We build the pause UI using LibGDX Scene2D. 
A Table fills the screen and is hidden by default. 
It contains a title label and three buttons: Resume, Music ON/OFF, 
and Exit to Menu. Each button uses a shared TextButtonStyle for
consistent visuals. We attach listeners so Resume toggles the game state, 
Music toggles background playback, and Exit stops in-game music
and returns to the menu.

togglePause switches the game between RUNNING and PAUSED states.

“togglePause switches the game between running and paused states.
When paused, it shows the pause menu, redirects input to the UI stage, 
and switches music.
When resuming, it hides the menu, restores gameplay input, 
and resumes the game music.

Delta time is the time passed since the last frame．
“Delta time is provided by LibGDX. It represents 
the time passed since the last frame and is passed 
automatically to the render method.”
The render method is called every frame by LibGDX.
It first handles input like pause or console toggles.
When the game is running, it updates the player, enemies,
and skills using delta time.
After that, it renders the game world and finally draws the 
HUD and pause menu.

- render(delta) framework:
    1) Handle toggles (console + pause) and input routing
    2) Update gameplay only if RUNNING and console hidden
    3) Update input/skills/timers/entities, then run collision & interaction checks
    4) Render world (tiles, walls, entities, effects)
    5) Render HUD and UI stage (pause overlay + menu)


- Console toggle: GRAVE key toggles the developer console (uses isKeyJustPressed to trigger once).



In render(delta), we first handle input and update the game 
logic when the state is RUNNING. Then we clear the screen and render the world 
(floor, walls, entities, effects). Finally, we render the HUD and UI stage,
including the pause overlay and pause menu.”

- Player actions: input is updated each frame and mapped to actions
- (Q/E/R skills, SPACE attack with sound).
- Camera control: player can adjust zoom; 
- zoom is clamped to maintain a usable view.



Each active projectile is updated every frame using delta
If a projectile hits an enemy, we remove it immediately to avoid repeated hits.

We also remove projectiles when they become inactive or leave a reasonable range, 
which prevents memory buildup.
We iterate backwards so removing elements by index doesn’t break the loop

After updating entities, we run a pipeline of collision and
interaction checks, apply special effects like fog, and finally 
update the camera to follow the player

- Checks & interactions: after updates, we run collision and interaction 
- checks (traps, attacks, skills, pickups) to ensure consistent state.
- Camera logic: fog effects reduce visibility via smooth zoom interpolation;
- the camera then follows the player and updates.
- When the player enters fog, visibility is reduced by smoothly 
- adjusting the camera zoom using linear interpolation
- Finally, the camera follows the player and updates its internal matrices.

“Rendering is done in layers: we clear the screen, draw the world using the game 
camera, then draw HUD and UI using the UI camera, and finally draw the pause 
overlay and the UI stage



Then we render all world entities in a fixed order: walls and obstacles first,
then items and keys, then interactive objects like doors, then enemies and the player,
and finally visual effects like projectiles.”


Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
shapeRenderer.setProjectionMatrix(camera.combined);
shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
shapeRenderer.end();
Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
For the healing skill effect, we render a 
temporary overlay near the player with fading alpha 
and additive blending to create a glow effect.

“HUD is drawn using the UI camera so it stays 
fixed on the screen regardless of world camera movement.

When paused, we render a semi-transparent
dark overlay to visually indicate the paused state

Finally, we update and draw the UI stage,
which includes the pause menu and other UI actors

- Rendering pipeline: clear screen → render world 
- with game camera (floor, walls, entities, effects).
- HUD/UI: draw HUD with UI camera so it stays screen-fixed;
- if PAUSED draw a semi-transparent overlay.
- UI stage: uiStage.act(delta) updates UI logic; 
- uiStage.draw() renders menus (pause menu, etc.).


We use the game’s shared SpriteBatch to render textures efficiently

We compute a level bonus based on remaining health and completion time.

    game.globalScore += levelBonus;
We add the level bonus to a global score stored in the main game object

- winGame(): one-time win handler; computes bonus (health + speed),
- updates globalScore, stops map music, then switches to ResultScreen 
- with stats + achievements.
- When the player wins, winGame() runs exactly once using a guard flag. 
- It computes a level bonus based on remaining health and completion time,
- updates the global score, stops the level music, and switches to the ResultScreen
- while passing the relevant stats and achievements.


findNearestEnemy scans all alive enemies, compares distances to the player, 
and returns the closest one—useful for auto-targeting skills.”

Pressing T opens the Skill Tree screen. We switch to SkillTreeScreen and 
set the game state to PAUSED so gameplay stops while the player
upgrades skills.

- Skill input: T opens the SkillTreeScreen and pauses gameplay; 
- Q/E/R trigger skills with just-pressed semantics.
- Dash: holding Shift performs a dash if cooldown allows it; 
- direction comes from movement input (controller).


Each frame we update all alive enemies, passing delta 
and wall data for movement and collision.

Enemies become aggressive only when the player enters 
their detection range. Then they compute a path towards the player;
otherwise they clear their path

When an enemy dies, we grant experience based on its type,
optionally track achievements, and remove it from the active enemy 
list using backward iteration.
- Enemy AI: enemies update each frame; within detection range they 
- pathfind towards the player and attack within attack range (respecting spawn invulnerability).
- Rewards: on death, enemy is removed (backward iteration) and 
- grants XP by type; achievements optionally track kills.


“We first handle the door interaction: if the player’s hitbox overlaps the
door and the player has a key, we open the door and trigger winGame(). 
Otherwise we prevent passing through the door.


If an alive enemy overlaps the player’s hitbox, the enemy attacks, 
we play a sound, and we trigger a damage visual effect.


If the player dies, we stop the level music, save the score, 
reset the global score, and switch to the ResultScreen with a loss flag.
- checkCollisions(): handles key interactions with the door; 
- with key → open door + winGame(), without key → revert movement to block passage.
- Damage rules: enemy-player overlap triggers enemy.
- attack + sound + damage VFX, but is skipped during spawn invulnerability.
- End conditions: if player health <= 0 → stop music, save score, 
- reset global score, switch to ResultScreen (loss).






































///////Player:
“Movement inputs are interpreted 
by the GameScreen and passed to the 
Player as boolean flags.
The Player does not read raw input directly, 
but updates its state based on the intended actions for the current frame.”

“This method only draws the player.
When the player was damaged recently, I change the batch 
color to red for a short time, then reset it back.”
takeDamage 那个

“Before applying damage, I check several conditions
like health, invulnerability and cooldown.
Only when damage is actually applied,
I update hurt state, cooldown and trigger visual effects.”

//////////SettingsManager:
SettingsManager is responsible for storing and loading user settings,
such as volume and key bindings, using LibGDX Preferences.

SettingsManager handles persistent user settings like volume and key bindings.
It uses LibGDX Preferences so settings 
are saved locally and restored when the game restarts.
GameScreen reads input based on key bindings from SettingsManager,
so changing settings does not affect game logic classes like Player.

///////InputController:
InputController reads keyboard input every frame and converts it into 
simple movement flags that can be used by the game logic.
InputController reads raw keyboard input every frame and converts it
into simple boolean flags like up, down, run.
These flags describe the player's intention for the current f
rame and are later used by GameScreen and Player logic.
Gdx.input reads the real keyboard.
SettingsManager stores user preferences (like which key is mapped to an action).
InputController reads Gdx.input each frame
and outputs simple movement flags for the game logic.

////////CollidableEntity:
“Different game objects have different behaviors, 
but collision handling requires the same basic information.
Using an interface allows different entities to be 
treated uniformly during collision checks.”

///////SettingScreen
SettingsScreen is a Scene2D UI screen that lets the user
adjust volume and rebind keys. It saves changes via 
SettingsManager (Preferences) and updates the game’s music
volume immediately
SettingsScreen allows the player to change game preferences such as volume and key bindings.
It uses SettingsManager to store these preferences
persistently, while the actual input is still handled by 
InputController during gameplay

//////Exit:
Exit represents the goal of the level and
only stores whether the player has reached it or not.

/////Door:
Door represents a locked gate that blocks the player until a key is used.
It handles its own collision check, open state, and rendering when closed.

//////MenusScreen:
MenuScreen displays the main menu and lets the player start or load a game,
select levels, open settings, or exit the game.

//////PlayerController:
“PlayerController reads the current key bindings from SettingsManager and 
provides simple boolean checks for movement and running.”

////playerstats:
PlayerStats keeps track of the player’s health, score, keys, and progression,
and it recalculates bonuses from skills and levels.”

////ResultScreen:
“ResultScreen shows the end-of-level result (win/lose), 
displays the score and achievements,
and provides buttons to continue or return to the main menu.”

///SaveData:
SaveData is a plain data object that stores all information needed
to save and restore the player’s game progress.”

////SaveManager:
“SaveManager is responsible for saving and loading the game state by writing 
PlayerStats into a JSON file and restoring it as SaveData.”

/////HelpScreen:
HelpScreen shows the controls and gameplay instructions on a 
scroll-style UI and lets the player return to the main menu.


//////HighScoreManager:
“HighScoreManager stores player scores locally,
sorts them, and keeps only the top five results



//////HighScoreScreen:
HighScoreScreen reads the saved top scores and
displays them in a simple UI with a back-to-menu button.



////////MapLoader:
MapLoader reads the level properties file and 
turns tile codes into actual game objects like walls,
enemies, traps, items, and entry/exit positions.



////////StoryScreen:



////////Wall:
“Wall represents a solid 
tile in the map with fixed position
and collision bounds that block player movement.”


///////DeveloperConsole:
“DeveloperConsole is a small debug tool that lets us type commands in-game 
to test features like healing, god mode, spawning a key, 
or killing all enemies.”




////////AchievementManager:
AchievementManager tracks the player’s progress 
(kills and gained EXP) and unlocks achievements
automatically when milestone requirements are met.





////////Decoration:
Decoration is a simple visual object used 
to add atmosphere to the map without affecting gameplay.”









Question:
“The fireball is rendered as an animation by splitting a texture into
multiple regions and playing them sequentially over time.
“We first load the fireball texture, then split it into several TextureRegions.
These regions are passed into a LibGDX Animation object, which selects the correct 
frame based on the elapsed time.

ｗｈｙ　player.getHitbox().overlaps(enemy.getBounds())
ｎｏｔ　player.getHitbox().overlaps(enemy.getBounds())


ｖｅｃｔｏｒ?
why InputController still need SettingsManager as parameter?
design choice:For flexibility and user customization.
InputController reads raw keyboard input using Gdx.input.
At the same time, it can load key bindings from SettingsManager
to respect user preferences, such as which key is mapped to which action.
Reading the keyboard directly works, but separating input reading 
from user preferences keeps the design cleaner and easier to extend.


show() is called once when the screen becomes active.
render() is called every frame to draw and update the screen.

Q3: 为什么入口/陷阱用 pixelX/pixelY，出口却用 new Vector2(x, y)？
A: 入口和陷阱是按像素坐标生成的对象；
出口这里存的是格子坐标（如果后面用于 tile 判断）。