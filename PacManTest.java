public class PacManTest {
    public static void testTunnelWrap() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        // Place pacman at left tunnel opening (Row 9, col 0) moving left past left edge
        game.pacman.x = -game.pacman.width;
        game.pacman.y = 9 * 32;
        game.pacman.velocityX = -8;
        game.pacman.velocityY = 0;
        
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != game.getBoardWidth() - game.pacman.width) {
            throw new AssertionError("Pacman did not wrap from left to right. Expected: " + 
                (game.getBoardWidth() - game.pacman.width) + " but got: " + game.pacman.x);
        }

        // Place pacman at right tunnel opening moving right past right edge
        game.pacman.x = game.getBoardWidth();
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != 0) {
            throw new AssertionError("Pacman did not wrap from right to left. Expected: 0 but got: " + game.pacman.x);
        }
        System.out.println("testTunnelWrap passed");
    }

    public static void testPowerPelletsLoaded() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        java.util.HashSet<PacMan.Block> powerFoods = game.getPowerFoods();
        if (powerFoods == null || powerFoods.size() != 4) {
            throw new AssertionError("Expected 4 power pellets, found: " + (powerFoods == null ? 0 : powerFoods.size()));
        }
        System.out.println("testPowerPelletsLoaded passed");
    }

    public static void testPowerPelletEatsGhost() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

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

        // Now test eating a ghost while scared in an isolated position
        PacMan.Block ghost = game.ghosts.iterator().next();
        ghost.x = 200;
        ghost.y = 200;
        game.pacman.x = 200;
        game.pacman.y = 200;
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

    public static void testGhostIntersectionTurning() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

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

    public static void main(String[] args) {
        testTunnelWrap();
        testPowerPelletsLoaded();
        testPowerPelletEatsGhost();
        testGhostIntersectionTurning();
    }
}
