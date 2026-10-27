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

    public static void main(String[] args) {
        testTunnelWrap();
        testPowerPelletsLoaded();
    }
}
