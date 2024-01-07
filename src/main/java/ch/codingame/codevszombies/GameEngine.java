package ch.codingame.codevszombies;

/**
 * Rules:
 *
 * The game is played in a zone 16000 units wide by 9000 units high.
 * You control a man named Ash, wielding a gun that lets him kill any
 * zombie within a certain range around him.
 *
 * Ash works as follows:
 * - Ash can be told to move to any point within the game zone by outputting a coordinate X Y.
 *   The top-left point is 0 0.
 * - Each turn, Ash will move exactly 1000 units towards the target coordinate,
 *   or onto the target coordinates if he is less than 1000 units away.
 * - If at the end of a turn, a zombie is within 2000 units of Ash,
 *   he will shoot that zombie and destroy it. More details on combat further down.
 *
 *
 * Other humans will be present in the game zone, but will not move.
 * If zombies kill all of them, you lose the game and score 0 points for
 * the current test case.
 *
 * Zombies are placed around the game zone at the start of the game,
 * they must be destroyed to earn points.
 *
 * Zombies work as follows:
 * - Each turn, every zombie will target the closest human,
 *   including Ash, and step 400 units towards them. If the zombie is less
 *   than 400 units away, the human is killed and the zombie moves onto their coordinate.
 * - Two zombies may occupy the same coordinate.
 *
 *
 * The order in which actions happens in between two rounds is:
 * - Zombies move towards their targets.
 * - Ash moves towards his target.
 * - Any zombie within a 2000 unit range around Ash is destroyed.
 * - Zombies eat any human they share coordinates with.
 *
 *
 * Killing zombies earns you points. The number of points you get
 * per zombie is subject to a few factors.
 *
 * Scoring works as follows:
 * - A zombie is worth the number of humans still alive squared x10, not including Ash.
 * - If several zombies are destroyed during on the same round, the nth zombie
 *    killed's worth is multiplied by the (n+2)th number of
 *    the Fibonnacci sequence (1, 2, 3, 5, 8, and so on). As a consequence, you should
 *    kill the maximum amount of zombies during a same turn.
 *
 *
 * Note: You may activate gory mode in the settings panel () if you have the guts for it.
 */
public class GameEngine {

    public static final int MAX_X = 16000;
    public static final int MAX_Y = 9000;

    public static final int ASH_SPEED = 1000;

    public static final int ASH_RANGE = 2000;
    public static final int ZOMBIE_RANGE = 400;

    private GameplayRecorder gameplayRecorder;

    public int playGame(GameState initialGameState, ISolution solution, boolean recordGameplay) {
        // todo: negative score on error/all humans dead
        int turnCounter = 1;
        GameState gameState = initialGameState.clone();
        solution.init();
        if (recordGameplay) {
            gameplayRecorder = new GameplayRecorder();
        }
        if (recordGameplay) {
            gameplayRecorder.recordGameState(gameState);
        }
        while (!gameState.isGameOver()) {
            System.err.println("Turn " + turnCounter++);
            gameState.printState();
            playTurn(gameState, solution);

            if (recordGameplay) {
                gameplayRecorder.recordGameState(gameState);
            }
        }
        return initialGameState.getScore();
    }

    public GameplayRecorder getLastGameplayRecord() {
        return gameplayRecorder;
    }

    public int playGame(GameState initialGameState, ISolution solution) {
        return playGame(initialGameState, solution, false);
    }

    void playTurn(GameState gameState, ISolution solution) {
        // todo: pass game state to solution, so that it's able to calculate the next move
        // get player's input
        Position playersInput = parsePosition(solution.getNextMove());

        // move zombies
        gameState.moveZombies(ZOMBIE_RANGE);

        // move Ash
        gameState.moveAsh(playersInput, ASH_SPEED);

        // destroy zombies
        gameState.destroyZombies(ASH_RANGE);

        // zombies eat humans
        gameState.eatHumans(ZOMBIE_RANGE);
    }

    private Position parsePosition(String move) {
        String[] parts = move.split(" ");
        return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
