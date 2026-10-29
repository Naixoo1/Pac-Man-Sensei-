# Task 1: Tunnel Wrap-Around Mechanics for Left and Right Openings

## Context
Working on branch `feature/gameplay-enhancements` in `C:\code\Pac-Man-Sensei-`.
JDK binary location: `C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin`
Compilation command: `& "C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin\javac.exe" PacManTest.java PacMan.java`
Test execution command: `& "C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin\java.exe" PacManTest`

## Requirements
Implement horizontal coordinate wrapping for the row 9 tunnel opening:
1. When Pac-Man exits through the left tunnel opening (`pacman.x + pacman.width <= 0`), wrap to the right side of the board (`boardWidth - pacman.width`).
2. When Pac-Man exits through the right tunnel opening (`pacman.x >= boardWidth`), wrap to the left side of the board (`0`).
3. Add package-private getters in `PacMan.java`: `getBoardWidth()` and `getBoardHeight()`.
4. Provide `public void handleTunnelWrap(Block entity)` in `PacMan.java` and call it in `move()`.
5. For ghosts, also allow wrapping through the tunnel and remove the artificial boundary bounce `|| ghost.x <= 0 || ghost.x + ghost.width >= boardWidth` at row 9.
6. Write test in `PacManTest.java` that asserts left-to-right and right-to-left wrapping without opening a GUI window.
7. Follow strict TDD:
   - Create `PacManTest.java` with failing test `testTunnelWrap()`.
   - Run test and verify failure.
   - Implement minimal changes in `PacMan.java`.
   - Run test and verify it passes.
   - Commit changes: `git commit -m "feat: add tunnel wrap-around mechanics for map openings"`.
8. Write execution report to `.superpowers/sdd/task-1-report.md`.
