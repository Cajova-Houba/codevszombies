package ch.codingame.codevszombies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateTest {

    @ParameterizedTest
    @CsvSource({"1,1,10", "1,2,30", "1,3,60", "2,1,40", "2,2,120", "2,3,240", "6,2,1080"})
    void getScore(int aliveHumans, int kills, int expectedScore) {
        final GameState game = prapreGameState(aliveHumans);

        assertEquals(expectedScore, game.calculateScore(kills), "Incorrect score for " + kills + " kills and " + aliveHumans +" humans.");
    }

    @Test
    void moveZombies() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(8250,8999))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(8250,4500)))
        );

        final Position expectedZombiePosition = new Position(8250,8599);

        game.moveZombies(400);

        assertEquals(expectedZombiePosition.x(), game.getZombies().get(0).x());
        assertEquals(expectedZombiePosition.y(), game.getZombies().get(0).y());
    }

    @Test
    void eatHumans_eat() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(1,1))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(1,2)))
        );

        game.eatHumans(2);

        assertEquals(0, game.getHumans().size());
    }

    @Test
    void eatHumans_tooFar() {
        final GameState game = new GameState(
                new ArrayList<>(List.of(new Position(1,1))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(1,4)))
        );

        game.eatHumans(2);

        assertEquals(1, game.getHumans().size());
    }

    private GameState prapreGameState(int aliveHumans) {
        List<Position> humans = new ArrayList<>();
        for (int i = 0; i < aliveHumans; i++) {
            humans.add(new Position(0, 0));
        }
        return new GameState(Collections.emptyList(), new Position(0, 0), humans);
    }
}