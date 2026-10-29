# Implementation Plan: Pac-Man Gameplay Enhancements

Extend the Pac-Man Swing/AWT game to support power pellets and eating frightened ghosts, left/right tunnel screen wrapping, intersection-aware ghost movement AI, and input buffering for responsive controls.

## Goal Description
The game currently has basic mechanics (eating food, wall collisions, and random ghost movement triggered only on wall collisions). The goals are:
1. **Power Pellets:** Add power pellets in the 4 corners of the maze using `powerFood.png`. Eating one turns ghosts scared (`scaredGhost.png`) for 8 seconds, enabling Pac-Man to eat them for +200 points without losing a life.
2. **Tunnel Wrap:** Fix the horizontal opening on row 9 so Pac-Man (and ghosts) smoothly screen-wrap when exiting the left or right edges instead of running off-screen.
3. **Ghost AI Improvement:** Replace the simplistic collision-only direction change with junction awareness so ghosts can turn at crossroads and T-junctions, covering all corridors of the maze.
4. **Input Buffering:** Provide buffered steering so player turn commands are queued and smoothly executed when the junction is reached.

---

## User Review Required

> [!IMPORTANT]
> **Java Environment Requirement:**
> To compile and execute tests (`javac` / `java`), a Java Development Kit (JDK 11, 17, or 21) is required. If not already on the local PATH, we can install OpenJDK (e.g., via `winget install Microsoft.OpenJDK.17`) during execution.

> [!NOTE]
> **Power Pellet Map Placement:**
> In accordance with standard Pac-Man conventions, power pellets are placed in the four corners of the maze at rows 3 and 19 (columns 1 and 17).

---

## Open Questions
None currently blocking. The mechanics follow classic arcade Pac-Man behavior and directly solve the homework requirements specified in `README.md`.

---

## Proposed Changes

### Game Logic & Rendering

#### [MODIFY] `PacMan.java`
- **Fields & Assets:**
  - Load `powerFoodImage` from `./powerFood.png` and `scaredGhostImage` from `./scaredGhost.png`.
  - Add `HashSet<Block> powerFoods` to track power pellets.
  - Add `int scaredTimer = 0` to track duration of frightened ghost mode.
  - Add `char nextDirection = 'R'` to buffer player input.
  - Add `baseImage` and `isScared` to inner class `Block` so ghosts know their original sprite and frightened state.
- **Map & Initialization:**
  - Update `tileMap` with `'*'` for power pellets at `(3, 1)`, `(3, 17)`, `(19, 1)`, `(19, 17)`.
  - In `loadMap()`, instantiate `powerFoods` blocks.
- **Tunnel Wrapping:**
  - Implement `handleTunnelWrap(Block entity)`: if `entity.x + entity.width <= 0`, wrap to right; if `entity.x >= boardWidth`, wrap to left.
- **Ghost AI & Intersections:**
  - Implement `canMove(Block block, char dir)` to test wall collision in a given direction.
  - When `ghost.x % tileSize == 0 && ghost.y % tileSize == 0`:
    - Identify valid open directions (excluding immediate 180-degree reversal unless dead-ended).
    - If at a junction (perpendicular turns available) or blocked, choose among valid directions.
- **Collision & Scoring:**
  - Separate `checkFoodCollisions()` and `checkGhostCollisions()`.
  - Eating power pellet: +50 points, activate `scaredTimer = 160`, switch ghosts to `scaredGhostImage`.
  - Colliding with scared ghost: +200 points, ghost resets to home spawn and reverts to base image; Pac-Man does not lose a life.
  - Level clears when both `foods.isEmpty()` and `powerFoods.isEmpty()`.
- **Input Buffering:**
  - On key press or release, update `nextDirection`.
  - In `move()`, if `canMove(pacman, nextDirection)`, apply `pacman.updateDirection(nextDirection)`.

---

### Automated Test Suite

#### [NEW] `PacManTest.java`
- Standalone headless test suite with executable `main` method (zero external dependencies).
- **Test Scenarios:**
  1. `testTunnelWrap()`: Pac-Man wrapping left-to-right and right-to-left.
  2. `testPowerPelletsLoaded()`: Verifies 4 power pellets parsed and placed.
  3. `testPowerPelletEatsGhost()`: Verifies power pellet gives 50 points, activates scared mode, and scared ghost is eaten for 200 points with position reset and no life loss.
  4. `testGhostIntersectionTurning()`: Verifies ghost detects and can turn into open perpendicular corridors at junctions.
  5. `testInputBuffering()`: Verifies buffered direction is applied as soon as the corridor opens.
  6. Review Focus tests: Double pellet eating safety, dead-end turnaround, and tunnel collision safety.

---

## Verification Plan

### Automated Tests
Once JDK is installed/configured:
```bash
# Compile game and test suite
javac PacMan.java PacManTest.java

# Run test suite
java PacManTest
```
Expected output:
```text
testTunnelWrap passed
testPowerPelletsLoaded passed
testPowerPelletEatsGhost passed
testGhostIntersectionTurning passed
testInputBuffering passed
All Review Focus edge cases passed!
```

### Manual Verification
1. Launch game: `java App`
2. **Tunnel Test:** Move Pac-Man through the left opening at row 9; verify Pac-Man emerges seamlessly from the right opening (and vice-versa).
3. **Power Pellet Test:** Eat a large pellet in any corner; verify ghosts turn blue/scared, collide with a scared ghost, verify ghost resets to center house, score increases by +200, and Pac-Man retains all lives.
4. **Ghost AI Test:** Observe ghosts at maze intersections; verify they turn into vertical/horizontal corridors and patrol previously unreachable corners instead of bouncing between walls.
5. **Responsiveness Test:** Press turn keys slightly before a junction; verify Pac-Man makes the turn cleanly without getting stuck.
