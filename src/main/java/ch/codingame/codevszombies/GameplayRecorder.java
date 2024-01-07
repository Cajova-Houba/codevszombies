package ch.codingame.codevszombies;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which allows to record vairous parts of gameplay.
 */
public class GameplayRecorder {

    private final List<Position> ashMovement;

    public GameplayRecorder() {
        this.ashMovement = new ArrayList<>();
    }

    public void recordAshMovement(Position position) {
        ashMovement.add(position);
    }

    public List<Position> getAshMovement() {
        return ashMovement;
    }

    public void recordGameState(GameState gameState) {
        recordAshMovement(gameState.getAsh());
    }
}
