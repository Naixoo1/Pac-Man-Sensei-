# Gameplay Enhancements: Power Pellets, Tunnel Wrap, and Ghost AI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement power pellets enabling Pac-Man to eat frightened ghosts, screen-wrap tunnel mechanics for the left/right map openings, and junction-aware ghost AI movement to cover all corridors.

**Architecture:** Extend `PacMan.java` with power pellet tile parsing (`'*'`) and collection management; introduce a frightened ghost state machine with timer and sprite swapping; implement horizontal coordinate wrapping for the row 9 tunnel; and replace collision-only ghost direction changes with tile-aligned junction inspection (`canMove` check across valid directions).

**Tech Stack:** Java SE (Swing, AWT), custom lightweight Java assertion test runner (`PacManTest.java`).

**Spec:** `README.md` lines 12-13: "You can add power pellets to allow pacman to eat the ghosts. In addition, there is an opening on left and right, where if pacman goes through, it would appear on the other side of the map. Currently pacman just moves off screen out of the map so a fix would be needed to make pacman appear the other side. For more of a challenge, you can modify the ghosts movement to cover areas unreachable since the ghosts only change directions when they collide against a wall, and not when theres another path available to go through."

## Global Constraints

- Must preserve existing 20 FPS game loop (`Timer(50, this)`) and tile dimensions (`tileSize = 32`, speed = `tileSize / 4 = 8`).
- Must use existing graphical assets already in repository: `powerFood.png` (8x8) and `scaredGhost.png` (16x16).
- Must avoid external libraries; automated tests must run with standard Java `assert` / executable main method.
- Must preserve retro arcade gameplay feel and score display.

## Review Focus

1. **Tunnel Re-entry Loop:** Pac-Man entering the left tunnel at `x < 0` must wrap cleanly to the right side without colliding with walls at Row 8 or Row 10, and must not immediately trigger a reverse wrap.
2. **Double Pellet Collision in same frame:** If Pac-Man eats both regular food and a power pellet in quick succession or within the same frame, food collections and score must update without `ConcurrentModificationException`.
3. **Re-eating Scared Ghost:** An eaten ghost sent back to its starting position (`startX, startY`) must revert to normal mode immediately so it cannot be repeatedly eaten while in the house during the remaining scared timer.
4. **Ghost Dead-End Trapping:** In a dead-end corridor (1-tile cul-de-sac), a ghost with no open forward or side paths must cleanly reverse direction (180-degree turn) rather than getting stuck permanently against the wall.
5. **Ghost House Escape Trap:** Ghosts starting at row 9 inside the ghost house must navigate out to row 7 through the openings at columns 6/12 and row 8 col 9 without getting blocked in an infinite back-and-forth bounce inside the house.

---

### Task 1: Tunnel Wrap-Around Mechanics for Left and Right Openings

**Files:**
- Modify: `PacMan.java`
- Create: `PacManTest.java`

**Interfaces:**
- Consumes: `pacman.x`, `pacman.y`, `pacman.velocityX`, `boardWidth`, `tileSize`
- Produces: `wrapEntity(Block entity)` helper or inline wrap handling in `move()`

- [ ] **Step 1: Write the failing test for tunnel wrap-around**

Create `PacManTest.java` containing:
```java
public class PacManTest {
    public static void testTunnelWrap() {
        PacMan game = new PacMan();
        // Place pacman at left tunnel opening (Row 9, col 0) moving left
        game.pacman.x = -game.pacman.width;
        game.pacman.y = 9 * 32;
        game.pacman.velocityX = -8;
        game.pacman.velocityY = 0;
        
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != game.getBoardWidth() - game.pacman.width) {
            throw new AssertionError("Pacman did not wrap from left to right. x=" + game.pacman.x);
        }

        // Place pacman at right tunnel opening moving right
        game.pacman.x = game.getBoardWidth();
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != 0) {
            throw new AssertionError("Pacman did not wrap from right to left. x=" + game.pacman.x);
        }
        System.out.println("testTunnelWrap passed");
    }

    public static void main(String[] args) {
        testTunnelWrap();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: FAIL with compilation error ("handleTunnelWrap / getBoardWidth not defined") or assertion error.

- [ ] **Step 3: Implement `handleTunnelWrap(Block entity)` and wrap logic in `PacMan.java`**

In `PacMan.java`:
- Provide package-private getters `getBoardWidth()` and `getBoardHeight()`.
- Add `public void handleTunnelWrap(Block entity)`:
  - If `entity.x + entity.width <= 0`: set `entity.x = boardWidth - entity.width`.
  - Else if `entity.x >= boardWidth`: set `entity.x = 0`.
- In `move()`:
  - Call `handleTunnelWrap(pacman)` after position update.
  - For each `ghost`, call `handleTunnelWrap(ghost)` and remove the condition `|| ghost.x <= 0 || ghost.x + ghost.width >= boardWidth` from wall collision check so ghosts can also utilize the tunnel.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: PASS with "testTunnelWrap passed".

- [ ] **Step 5: Commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "feat: add tunnel wrap-around mechanics for map openings"
```

---

### Task 2: Power Pellets Parsing and Rendering

**Files:**
- Modify: `PacMan.java`
- Modify: `PacManTest.java`

**Interfaces:**
- Consumes: `./powerFood.png`, `tileMap`
- Produces: `HashSet<Block> powerFoods`, `powerFoodImage`, `getPowerFoods()`

- [ ] **Step 1: Write the failing test for power pellet parsing**

Add to `PacManTest.java`:
```java
public static void testPowerPelletsLoaded() {
    PacMan game = new PacMan();
    HashSet<PacMan.Block> powerFoods = game.getPowerFoods();
    if (powerFoods == null || powerFoods.size() != 4) {
        throw new AssertionError("Expected 4 power pellets, found: " + (powerFoods == null ? 0 : powerFoods.size()));
    }
    System.out.println("testPowerPelletsLoaded passed");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: FAIL with compilation error ("getPowerFoods not found").

- [ ] **Step 3: Implement power pellets in `PacMan.java`**

In `PacMan.java`:
- Add field `private Image powerFoodImage;`.
- Add field `HashSet<Block> powerFoods;`.
- Add method `public HashSet<Block> getPowerFoods() { return powerFoods; }`.
- In constructor: load `powerFoodImage = new ImageIcon(getClass().getResource("./powerFood.png")).getImage();`.
- Update `tileMap` at four corners with `'*'`:
  - Row 3: `"X*               *X"`
  - Row 19: `"X*               *X"`
- In `loadMap()`:
  - Initialize `powerFoods = new HashSet<Block>();`.
  - If `tileMapChar == '*'`: create `Block powerFood = new Block(powerFoodImage, x + 8, y + 8, 16, 16); powerFoods.add(powerFood);`.
- In `draw(Graphics g)`:
  - For each `powerFood` in `powerFoods`, draw `g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);`.
- In `move()`:
  - Check `foods.isEmpty() && powerFoods.isEmpty()` before reloading map.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: PASS with "testPowerPelletsLoaded passed".

- [ ] **Step 5: Commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "feat: parse and render power pellets in map corners"
```

---

### Task 3: Frightened Ghost Mode & Ghost Eating Mechanics

**Files:**
- Modify: `PacMan.java`
- Modify: `PacManTest.java`

**Interfaces:**
- Consumes: `./scaredGhost.png`, `pacman`, `ghosts`, `powerFoods`
- Produces: `scaredTimer`, `isScared()`, `ghost.baseImage`, ghost eating logic

- [ ] **Step 1: Write the failing test for eating power pellets and frightened ghosts**

Add to `PacManTest.java`:
```java
public static void testPowerPelletEatsGhost() {
    PacMan game = new PacMan();
    int initialScore = game.score;
    int initialLives = game.lives;

    // Simulate eating power food
    PacMan.Block pf = game.getPowerFoods().iterator().next();
    game.pacman.x = pf.x;
    game.pacman.y = pf.y;
    game.checkFoodCollisions();

    if (game.getScaredTimer() <= 0) {
        throw new AssertionError("Scared timer not activated upon eating power pellet");
    }
    if (game.score != initialScore + 50) {
        throw new AssertionError("Score did not increase by 50. Current score=" + game.score);
    }

    // Now test eating a ghost while scared
    PacMan.Block ghost = game.ghosts.iterator().next();
    game.pacman.x = ghost.x;
    game.pacman.y = ghost.y;
    game.checkGhostCollisions();

    if (game.lives != initialLives) {
        throw new AssertionError("Lives decreased when eating scared ghost! lives=" + game.lives);
    }
    if (game.score != initialScore + 50 + 200) {
        throw new AssertionError("Score did not award 200 for eating scared ghost. score=" + game.score);
    }
    if (ghost.x != ghost.startX || ghost.y != ghost.startY) {
        throw new AssertionError("Eaten ghost did not reset to start position");
    }
    System.out.println("testPowerPelletEatsGhost passed");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: FAIL with compilation error ("getScaredTimer / checkFoodCollisions not defined").

- [ ] **Step 3: Implement frightened ghost state and collision handling in `PacMan.java`**

In `PacMan.java`:
- Add field `Image scaredGhostImage;` loaded from `./scaredGhost.png`.
- Add field `Image baseImage;` and `boolean isScared = false;` to `Block`.
- Add field `int scaredTimer = 0;` (package-private with getter `getScaredTimer()`).
- In `loadMap()`: when creating each ghost, assign `ghost.baseImage = ghost.image`.
- Extract food collision check into `public void checkFoodCollisions()`:
  - Check normal foods (`foods`): `score += 10`.
  - Check `powerFoods`: on collision, remove, `score += 50`, `scaredTimer = 160` (8 seconds at 20fps).
  - For each `ghost` in `ghosts`: set `ghost.isScared = true; ghost.image = scaredGhostImage;`.
- Extract ghost collision check into `public void checkGhostCollisions()`:
  - For each `ghost`:
    - If `collision(ghost, pacman)`:
      - If `ghost.isScared`:
        - `score += 200`.
        - `ghost.reset();`
        - `ghost.image = ghost.baseImage;`
        - `ghost.isScared = false;`
      - Else:
        - `lives -= 1;`
        - If `lives == 0` -> `gameOver = true`.
        - Else -> `resetPositions()`.
- In `move()`:
  - If `scaredTimer > 0`:
    - `scaredTimer--;`
    - If `scaredTimer == 0`:
      - For all ghosts: `ghost.image = ghost.baseImage; ghost.isScared = false;`.
- In `resetPositions()`:
  - `scaredTimer = 0;`
  - For all ghosts: `ghost.image = ghost.baseImage; ghost.isScared = false;`.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: PASS with "testPowerPelletEatsGhost passed".

- [ ] **Step 5: Commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "feat: implement frightened ghost state and ghost eating mechanics"
```

---

### Task 4: Intersection-Aware Ghost Movement AI

**Files:**
- Modify: `PacMan.java`
- Modify: `PacManTest.java`

**Interfaces:**
- Consumes: `walls`, `ghost.x`, `ghost.y`, `tileSize`
- Produces: `canMove(Block block, char dir)`, `getOppositeDirection(char dir)`, `updateGhostAI(Block ghost)`

- [ ] **Step 1: Write the failing test for ghost junction turn capability**

Add to `PacManTest.java`:
```java
public static void testGhostIntersectionTurning() {
    PacMan game = new PacMan();
    PacMan.Block ghost = game.ghosts.iterator().next();
    
    // Position ghost at intersection (Row 5, Col 1) where multiple directions are open
    ghost.x = 1 * 32;
    ghost.y = 5 * 32;
    ghost.direction = 'U';
    ghost.updateVelocity();

    // Check available directions at this junction
    java.util.List<Character> openDirs = game.getValidDirections(ghost, true);
    if (!openDirs.contains('R')) {
        throw new AssertionError("Ghost cannot detect open right turn at intersection (Row 5, Col 1)");
    }
    System.out.println("testGhostIntersectionTurning passed");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: FAIL with compilation error ("getValidDirections not found").

- [ ] **Step 3: Implement intersection navigation in `PacMan.java`**

In `PacMan.java`:
- Add `public char getOppositeDirection(char dir)`:
  - `'U'` <-> `'D'`, `'L'` <-> `'R'`.
- Add `public boolean canMove(Block block, char dir)`:
  - Calculate hypothetical step `(nextX, nextY)` using `tileSize / 4` velocity.
  - Return false if `nextX, nextY, block.width, block.height` collides with any wall; true otherwise.
- Add `public java.util.List<Character> getValidDirections(Block ghost, boolean excludeReverse)`:
  - Test all 4 directions with `canMove(ghost, d)`.
  - If `excludeReverse`, filter out `getOppositeDirection(ghost.direction)` unless no other choice exists.
- In ghost movement section of `move()`:
  - If `ghost.x % tileSize == 0 && ghost.y % tileSize == 0`:
    - Fetch non-reverse valid directions.
    - If perpendicular turns are available or forward path is blocked:
      - Pick randomly among valid non-reverse directions (or reverse if dead end).
      - Set `ghost.updateDirection(newDir)`.
  - Maintain the existing ghost house exit guide when `ghost.y == tileSize * 9` to ensure ghosts smoothly leave spawn.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: PASS with "testGhostIntersectionTurning passed".

- [ ] **Step 5: Commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "feat: add intersection-aware ghost AI movement"
```

---

### Task 5: Smooth Pac-Man Input Buffering (Pre-turning)

**Files:**
- Modify: `PacMan.java`
- Modify: `PacManTest.java`

**Interfaces:**
- Consumes: `KeyEvent`, `pacman`
- Produces: `nextDirection` buffered turning in `move()`

- [ ] **Step 1: Write the failing test for Pac-Man input buffering**

Add to `PacManTest.java`:
```java
public static void testInputBuffering() {
    PacMan game = new PacMan();
    game.pacman.x = 9 * 32;
    game.pacman.y = 15 * 32; // Start position
    game.pacman.direction = 'L';
    game.pacman.updateVelocity();
    
    // Set nextDirection to 'R' (opposite or pending turn)
    game.setNextDirection('R');
    game.applyBufferedDirection();
    
    if (game.pacman.direction != 'R') {
        throw new AssertionError("Buffered direction 'R' was not applied when open. direction=" + game.pacman.direction);
    }
    System.out.println("testInputBuffering passed");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: FAIL with compilation error ("setNextDirection / applyBufferedDirection not found").

- [ ] **Step 3: Implement buffered input in `PacMan.java`**

In `PacMan.java`:
- Add field `char nextDirection = 'R';`.
- Add `public void setNextDirection(char dir) { this.nextDirection = dir; }`.
- Add `public void applyBufferedDirection()`:
  - If `canMove(pacman, nextDirection)`:
    - `pacman.updateDirection(nextDirection);`
    - Update `pacman.image` to matching `pacmanUpImage`, `pacmanDownImage`, etc.
- In `keyReleased(KeyEvent e)` and `keyPressed(KeyEvent e)`:
  - Record desired direction into `nextDirection`.
- In `move()`:
  - Call `applyBufferedDirection()` before advancing Pac-Man position.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: PASS with "testInputBuffering passed".

- [ ] **Step 5: Commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "feat: add responsive input buffering for Pac-Man direction changes"
```

---

### Task 6: Comprehensive Verification and Edge-Case Hardening

**Files:**
- Modify: `PacManTest.java`
- Modify: `PacMan.java` (if any edge-case fixes needed)

**Interfaces:**
- Consumes: All modules from Tasks 1-5
- Produces: Complete test suite exercising all 5 Review Focus items

- [ ] **Step 1: Write tests covering all 5 Review Focus edge cases**

Add to `PacManTest.java`:
- `testReviewFocusTunnelNoWallCollision()`: Verifies that during horizontal tunnel wrap at Row 9, `pacman` coordinates never trigger wall collision with Row 8 or Row 10 walls.
- `testReviewFocusDoubleEat()`: Verifies eating normal food and power pellet in same frame does not throw `ConcurrentModificationException`.
- `testReviewFocusEatenGhostImmunity()`: Verifies that after a scared ghost is eaten and reset to start, it is immune to being eaten again while in house.
- `testReviewFocusGhostDeadEnd()`: Verifies ghost placed in a dead end reverses direction 180 degrees without freezing.
- `testReviewFocusGhostHouseExit()`: Verifies ghosts in the house can reach row 7 through upward passage.

- [ ] **Step 2: Run comprehensive test suite**

Run: `javac PacManTest.java PacMan.java && java PacManTest`
Expected: All 5 tests PASS.

- [ ] **Step 3: Verify clean build and commit**

```bash
git add PacMan.java PacManTest.java
git commit -m "test: add edge-case verification for Review Focus scenarios"
```
