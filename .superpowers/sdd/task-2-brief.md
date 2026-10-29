# Task 2: Power Pellets Parsing and Rendering

## Context
Working on branch `feature/gameplay-enhancements` in `C:\code\Pac-Man-Sensei-`.
JDK binary location: `C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin`
Compilation command: `& "C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin\javac.exe" PacManTest.java PacMan.java`
Test execution command: `& "C:\Users\vloxm\AppData\Local\Programs\jdk17\jdk-17.0.20.1+1\bin\java.exe" PacManTest`

## Requirements
Implement power pellets model and rendering:
1. In `PacMan.java`:
   - Add field `private Image powerFoodImage;`.
   - Add field `HashSet<Block> powerFoods;`.
   - Add method `public HashSet<Block> getPowerFoods() { return powerFoods; }`.
   - In constructor: load `powerFoodImage = new ImageIcon(getClass().getResource("./powerFood.png")).getImage();`.
2. Update `tileMap` at four corners with `'*'`:
   - Row 3: `"X*               *X"`
   - Row 19: `"X*               *X"`
3. In `loadMap()`:
   - Initialize `powerFoods = new HashSet<Block>();`.
   - If `tileMapChar == '*'`: create `Block powerFood = new Block(powerFoodImage, x + 8, y + 8, 16, 16); powerFoods.add(powerFood);`.
4. In `draw(Graphics g)`:
   - For each `powerFood` in `powerFoods`, draw `g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);`.
5. In `move()`:
   - Check `foods.isEmpty() && powerFoods.isEmpty()` before reloading map.
6. Write test `testPowerPelletsLoaded()` in `PacManTest.java` that asserts 4 power pellets are loaded and placed in corners.
7. Follow strict TDD:
   - Step 1: Write failing test in `PacManTest.java`
   - Step 2: Compile & run test to verify failure
   - Step 3: Implement changes in `PacMan.java`
   - Step 4: Run test to verify it passes
   - Step 5: Clean up `.class` files, then git add and commit: `git commit -m "feat: parse and render power pellets in map corners"`
8. Write execution report to `.superpowers/sdd/task-2-report.md`.
