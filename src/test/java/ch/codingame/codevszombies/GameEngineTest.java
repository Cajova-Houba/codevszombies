package ch.codingame.codevszombies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTest {

    private GameState twoZombiesInitial;

    @BeforeEach
    void setUp() {
        twoZombiesInitial = new GameState(
                List.of(new Position(3100,7000), new Position(11500,7100)),
                new Position(5000, 0),
                new ArrayList<>(List.of(new Position(950,6000), new Position(8000,6100)))
        );
    }

    @Test
    void twoZombies_fail() {
        // this solution failed in codingame
        final ISolution solution = new StringListSolution(
                "4444 831", "3888 1662", "3332 2493", "2776 3324",
                        "2220 4155", "1664 4986", "1108 5817", "552 6648" ,
                        "0 7479", "0 8310", "0 8310", "0 8310", "0 8310" ,
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310"
        );
        final int expectedScore = 0;

        GameEngine engine = new GameEngine();
        int score = engine.playGame(twoZombiesInitial.clone(), solution);

        assertEquals(expectedScore, score);
    }
}