package ch.codingame.codevszombies;
import java.util.*;

public class GameState {

    public static final int[] FIB_SEQ = new int[] {0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};

    /**
     * Zombies ordered by id.
     */
    private final List<Position> zombies;

    private Position ash;

    /**
     * Humans ordered by id.
     */
    private final List<Position> humans;

    private int score;

    /**
     * Hash codes of zombies that have already played this turn and should not eat any humans.
     * Cleared every turn in {@link #moveZombies(int)}.
     */
    private Set<Integer> zombiesPlayed = new HashSet<>();

    public GameState(List<Position> zombies, Position ash, List<Position> humans) {
        this.zombies = zombies;
        this.ash = ash;
        this.humans = humans;
        this.score = 0;
    }

    /**
     * Clones the state as it is.
     */
    public GameState clone() {
        List<Position> newZombies = new ArrayList<>();
        for (Position zombie : zombies) {
            newZombies.add(zombie.clone());
        }

        List<Position> newHumans = new ArrayList<>();
        for (Position human : humans) {
            newHumans.add(human.clone());
        }

        return new GameState(newZombies, ash.clone(), newHumans);
    }

    public int getScore() {
        return score;
    }

    /**
     * Move zombies to the closes humans (including Ash).
     *
     * @param zombieSpeed How many units to move.
     */
    public void moveZombies(int zombieSpeed) {
        zombiesPlayed.clear();

        // iterate over zombies
        // move each zombie towards closest human
        for (int i = 0; i < zombies.size(); i++) {
            Position zombie = zombies.get(i);
            Position closestHuman = findClosestHuman(zombie);

            if (zombie.equals(closestHuman)) {
                System.err.println("Zombie "+i+" is already at the closest human: " + closestHuman);
                continue;
            }

            zombies.set(i, zombie.moveTo(closestHuman, zombieSpeed));
            zombie = zombies.get(i);

            // if zombie is < range from human, it will move to human's position BUT NOT EAT IT
            if (zombie.squareDistanceFrom(closestHuman) < (zombieSpeed*zombieSpeed)) {
                System.err.println("Zombie "+i+" is moving to the closest human: " + closestHuman);
                zombie = closestHuman.clone();
                zombies.set(i, zombie);
                zombiesPlayed.add(zombie.hashCode());
            }
        }
    }

    /**
     * Move ash in given direction by given number of units.
     * @param direction Direction to move in.
     * @param ashSpeed How many units to move.
     */
    public void moveAsh(Position direction, int ashSpeed) {
        ash = ash.moveTo(direction, ashSpeed);
        System.err.println("Ash moved to: " + ash);
    }

    /**
     * Destroy zombies in Ash's range.
     * Scoring works as follows:
     *  - A zombie is worth the number of humans still alive squared x10, not including Ash.
     *  - If several zombies are destroyed during on the same round, the nth zombie
     *     killed's worth is multiplied by the (n+2)th number of
     *     the Fibonnacci sequence (1, 2, 3, 5, 8, and so on). As a consequence, you should
     *     kill the maximum amount of zombies during a same turn.
     *
     * @param ashRange Ash's range.
     */
    public void destroyZombies(int ashRange) {
        int kills = 0;
        for (int i = zombies.size() - 1; i >= 0; i--) {
            Position zombie = zombies.get(i);
            if (zombie.squareDistanceFrom(ash) < (ashRange*ashRange)) {
                kills++;
                zombies.remove(i);
                System.err.println("Zombie killed: " + zombie);
            }
        }

        score += calculateScore(kills);
    }

    /**
     * Eat humans in zombie's range.
     * @param zombieRange Zombie's range.
     */
    public void eatHumans(int zombieRange) {
        for (int i = humans.size() - 1; i >= 0; i--) {
            Position human = humans.get(i);
            for (int j = 0; j < zombies.size(); j++) {
                Position zombie = zombies.get(j);
                if (zombiesPlayed.contains(zombie.hashCode())) {
                    continue;
                }
                if (zombie.squareDistanceFrom(human) < (zombieRange*zombieRange)) {
                    System.err.println("Human eaten: " + human);
                    humans.remove(i);

                    // check if there's any human left
                    if (humans.isEmpty()) {
                        System.err.println("All humans eaten!");
                        score = 0;
                    }

                    // zombie moves to human's coordinates
                    zombies.set(j, human.clone());
                    break;
                }
            }
        }
    }

    /**
     * Check if game is over.
     * @return True if there are no humans left.
     */
    public boolean isGameOver() {
        return humans.isEmpty() || zombies.isEmpty();
    }

    public void printState() {
        System.err.println("Ash: " + ash);
        System.err.println("Humans: " + humans);
        System.err.println("Zombies: " + zombies);
    }

    int calculateScore(int killsInTurn) {
        int fib = 0;
        for (int i = 0; i < killsInTurn; i++) {
            fib += FIB_SEQ[i+2];
        }
        return humans.size()*humans.size() * 10 * fib;
    }

    public List<Position> getZombies() {
        return zombies;
    }

    private Position findClosestHuman(Position zombie) {
        // try Ash first
        Position nearestHuman = ash;
        int nearestDistance = zombie.squareDistanceFrom(ash);

        for (Position human : humans) {
            int distance = zombie.squareDistanceFrom(human);
            if (distance < nearestDistance) {
                nearestHuman = human;
                nearestDistance = distance;
            }
        }

        return nearestHuman;
    }

    public List<Position> getHumans() {
        return humans;
    }

    public Position getAsh() {
        return ash;
    }
}
