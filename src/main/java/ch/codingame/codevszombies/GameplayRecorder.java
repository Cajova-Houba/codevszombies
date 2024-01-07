package ch.codingame.codevszombies;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which allows to record vairous parts of gameplay.
 */
public class GameplayRecorder {

    private final List<Position> ashMovement;
    private final List<List<Position>> zombiesMovement;

    public GameplayRecorder() {
        this.ashMovement = new ArrayList<>();
        this.zombiesMovement = new ArrayList<>();
    }

    public void recordAshMovement(Position position) {
        ashMovement.add(position);
    }

    public List<Position> getAshMovement() {
        return ashMovement;
    }

    public List<List<Position>> getZombiesMovement() {
        return zombiesMovement;
    }

    public void recordGameState(GameState gameState) {
        recordAshMovement(gameState.getAsh());
        recordZombiesMovement(gameState.getZombies());
    }

    private void recordZombiesMovement(List<Position> zombies) {
        if (zombiesMovement.isEmpty()) {
            // no recorded moves yet
            for (Position zombie : zombies) {
                List<Position> zombieMovement = new ArrayList<>();
                zombiesMovement.add(zombieMovement);

                if (zombie != null) {
                    zombieMovement.add(zombie);
                }
            }
        } else {
            // add new moves to existing ones
            for (int zombieId = 0; zombieId < zombies.size(); zombieId++) {
                Position zombie = zombies.get(zombieId);
                if (zombie == null) {
                    continue;
                }
                zombiesMovement.get(zombieId).add(zombies.get(zombieId));
            }
        }
    }
}
