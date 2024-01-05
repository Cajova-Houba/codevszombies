package ch.codingame.codevszombies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test my engine with submitted solutions from coding game.
 */
class CodinGameSolutionTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
    }

    /**
     * Simulation of the first test case from coding game.
     */
    @Test
    void testCaseSimple() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(8250,8999))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(8250,4500)))
                );

        final ISolution solution = new StringListSolution("8250 8999", "8250 8599", "8250 8199", "8250 7799", "8250 7399", "8250 6999", "8250 6599", "8250 6199", "8250 5799");

        int score = engine.playGame(game, solution);

        assertEquals(10, score);
    }

    @Test
    void testCase2Zombies() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(3100,7000), new Position(11500,7100))),
                new Position(5000, 0),
                new ArrayList<>(List.of(new Position(950,6000), new Position(8000,6100)))
        );

        final ISolution solution = new StringListSolution(
                "8000 6100",
                "8000 6100",
                "8000 6100",
                "8000 6100",
                "8000 6100",
                "8000 6100",
                "950 6000",
                "1348 5963",
                "1746 5926",
                "2144 5889"
        );

        int score = engine.playGame(game, solution);

        assertEquals(50, score);
    }

    @Test
    void testCase2ZombiesRedux() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(1250,5500), new Position(15999,5500))),
                new Position(10999,0),
                new ArrayList<>(List.of(new Position(8000,5500), new Position(4000,5500)))
        );

        final ISolution solution = new StringListSolution(
                "8000 5500",
                "8000 5500",
                "8000 5500",
                "8000 5500",
                "8000 5500",
                "8000 5500",
                "8000 5500",
                "4000 5500",
                "4000 5500",
                "12723 4697",
                "12328 4764",
                "11933 4831",
                "11535 4874"
        );

        int score = engine.playGame(game, solution);

        assertEquals(20, score);
    }

    @Test
    void testCaseComboOpportunity() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(
                        new Position(8000,4500),
                        new Position(9000,4500),
                        new Position(10000,4500),
                        new Position(11000,4500),
                        new Position(12000,4500),
                        new Position(13000,4500),
                        new Position(14000,4500),
                        new Position(15000,3500),
                        new Position(14500,2500),
                        new Position(15900,500)
                )),
                new Position(500,4500),
                new ArrayList<>(List.of(
                        new Position(100,4000),
                        new Position(130,5000),
                        new Position(10,4500),
                        new Position(500,3500),
                        new Position(10,5500),
                        new Position(100,3000)
                        ))
        );

        final ISolution solution = new StringListSolution(
                "8000 4500",
                "7600 4500",
                "7200 4500",
                "6800 4500",
                "7400 4500",
                "8000 4500",
                "8600 4500",
                "10200 4500",
                "10800 4500",
                "11423 3882",
                "12262 2054"
        );

        int score = engine.playGame(game, solution);

        assertEquals(4320, score);
    }

    private static class StringListSolution implements ISolution {

        private final String[] moves;
        private int index = 0;

        public StringListSolution(String... moves) {
            this.moves = moves;
        }

        @Override
        public String getNextMove() {
            return moves[index++];
        }
    }
}