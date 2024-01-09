package ch.codingame.codevszombies.solution.star1gen;

import java.util.*;

/**
 * Strategy: Generate multiple paths. Each of the paths starts at the player's initial position and
 * goes in a straight line towards the edge. Evaluate each path by simulating the game, then pick the
 * best one.
 */
class Player {

    // main two parameters to tweak
    private static final int MAX_TURNS = 40;
    private static final int INIT_POPULATION_SIZE = 32;

    // game constraints
    private static final int MAX_X = 16000;
    private static final int MAX_Y = 9000;
    private static final int ASH_SPEED = 1000;

    record EvaluatedChromosome(List<Position> chromosome, int score) {}

    record Position(int x, int y) {

        public int squareDistanceFrom(Position other) {
            int dx = this.x - other.x;
            int dy = this.y - other.y;
            return dx * dx + dy * dy;
        }

        public Position moveTo(Position target, int units) {
            if (target.equals(this)) {
                return this;
            }

            Position direction = new Position(target.x - x, target.y - y);
            double length = Math.sqrt(direction.x * direction.x + direction.y * direction.y);
            double dx = direction.x / length;
            double dy = direction.y / length;
            return new Position((int) (x + dx * units), (int) (y + dy * units));
        }

        public Position scale(double sx, double sy) {
            return new Position((int) (x * sx), (int) (y * sy));
        }

        public Position clone() {
            return new Position(x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static class GameEngine {

        public static final int[] FIB_SEQ = new int[] {0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};

        public static final int ASH_RANGE = 2000;
        public static final int ZOMBIE_RANGE = 400;

        private Position playerPosition;
        private final List<Position> zombiePositions;
        private final List<Position> humanPositions;

        int score;

        /**
         * Ids codes of zombies that have already played this turn and should not eat any humans.
         * Cleared every turn in {@link #moveZombies(int)}.
         */
        private final Set<Integer> zombiesPlayed = new HashSet<>();

        public GameEngine(Position initialPlayerPosition, List<Position> initialZombiePositions, List<Position> initialHumanPositions) {
            this.playerPosition = initialPlayerPosition.clone();
            this.zombiePositions = new ArrayList<>(initialZombiePositions.size());
            this.humanPositions = new ArrayList<>(initialHumanPositions.size());

            for (Position zombiePosition : initialZombiePositions) {
                this.zombiePositions.add(zombiePosition.clone());
            }
            for (Position humanPosition : initialHumanPositions) {
                this.humanPositions.add(humanPosition.clone());
            }

            score = 0;
        }

        /**
         * Check if game is over.
         * @return True if there are no humans left.
         */
        public boolean isGameOver() {
            return humanPositions.isEmpty() || !anyZombiesLeft();
        }

        boolean anyZombiesLeft() {
            return zombiePositions.stream().anyMatch(Objects::nonNull);
        }

        public int playGame(List<Position> solution) {
            int turnCounter = 0;

            while (!isGameOver()) {
                playTurn(solution.get(turnCounter));

                turnCounter++;
            }
            return score;
        }

        void playTurn(Position playersMove) {

            // move zombies
            moveZombies(ZOMBIE_RANGE);

            // move Ash
            moveAsh(playersMove, ASH_SPEED);

            // destroy zombies
            destroyZombies(ASH_RANGE);

            // zombies eat humans
            eatHumans(ZOMBIE_RANGE);
        }

        private void eatHumans(int zombieRange) {
            for (int i = humanPositions.size() - 1; i >= 0; i--) {
                Position human = humanPositions.get(i);
                for (int zombieId = 0; zombieId < zombiePositions.size(); zombieId++) {
                    Position zombie = zombiePositions.get(zombieId);
                    if (zombie == null || zombiesPlayed.contains(zombieId)) {
                        continue;
                    }
                    if (zombie.squareDistanceFrom(human) < (zombieRange*zombieRange)) {
                        humanPositions.remove(i);

                        // check if there's any human left
                        if (humanPositions.isEmpty()) {
                            score = 0;
                        }

                        // zombie moves to human's coordinates
                        zombiePositions.set(zombieId, human.clone());
                        break;
                    }
                }
            }
        }

        private void destroyZombies(int ashRange) {
            int kills = 0;
            for (int zombieId = zombiePositions.size() - 1; zombieId >= 0; zombieId--) {
                Position zombie = zombiePositions.get(zombieId);
                if (zombie == null) {
                    continue;
                }

                if (zombie.squareDistanceFrom(playerPosition) < (ashRange*ashRange)) {
                    kills++;
                    zombiePositions.set(zombieId, null);
                }
            }

            score += calculateScore(kills);
        }

        int calculateScore(int killsInTurn) {
            int fib = 0;
            for (int i = 0; i < killsInTurn; i++) {
                fib += FIB_SEQ[i+2];
            }
            return humanPositions.size()*humanPositions.size() * 10 * fib;
        }

        private void moveAsh(Position direction, int ashSpeed) {
            playerPosition = playerPosition.moveTo(direction, ashSpeed);
        }

        private void moveZombies(int zombieRange) {
            zombiesPlayed.clear();

            // iterate over zombies
            // move each zombie towards closest human
            for (int zombieId = 0; zombieId < zombiePositions.size(); zombieId++) {
                Position zombie = zombiePositions.get(zombieId);
                if (zombie == null) {
                    continue;
                }

                Position closestHuman = findClosestHuman(zombie);

                if (zombie.equals(closestHuman)) {
                    continue;
                }

                zombiePositions.set(zombieId, zombie.moveTo(closestHuman, zombieRange));
                zombie = zombiePositions.get(zombieId);

                // if zombie is < range from human, it will move to human's position BUT NOT EAT IT
                if (zombie.squareDistanceFrom(closestHuman) < (zombieRange*zombieRange)) {
                    zombie = closestHuman.clone();
                    zombiePositions.set(zombieId, zombie);
                    zombiesPlayed.add(zombieId);
                }
            }
        }

        private Position findClosestHuman(Position zombie) {
            // try Ash first
            Position nearestHuman = playerPosition;
            int nearestDistance = zombie.squareDistanceFrom(playerPosition);

            for (Position human : humanPositions) {
                int distance = zombie.squareDistanceFrom(human);
                if (distance < nearestDistance) {
                    nearestHuman = human;
                    nearestDistance = distance;
                }
            }

            return nearestHuman;
        }
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        final List<Position> humans = new ArrayList<>();
        final List<Position> zombies = new ArrayList<>();
        Position ash;

        boolean isFirstTurn = true;

        int turnCounter = 0;

        // solution is essentially a list of moves
        List<Position> solution = new ArrayList<>();


        // game loop
        while (true) {
            // 1st line is Ash
            ash = parsePosition(in.nextLine());

            // 2nd line is human count, lines with human position follow
            parseHumanPositions(in, humans);

            // rest is zombies
            parseZombiePositions(in, zombies);

            if (isFirstTurn) {
                // try to find the best solution
                solution = findBestSolution(ash, humans, zombies);
                isFirstTurn = false;
            }

            if (turnCounter < solution.size()) {
                Position nextMove = solution.get(turnCounter);
                System.out.println(nextMove.x + " " + nextMove.y);
            } else {
                System.out.println("0 0");
            }

            turnCounter++;
        }

    }

    private static List<Position> findBestSolution(Position ash, List<Position> humans, List<Position> zombies) {
        // generate initial star of paths representing possible solutions
        // first generation of genetic algorithm (if I ever decide to implement it)
        List<List<Position>> starPopulation = generateStarPopulation(ash, INIT_POPULATION_SIZE, MAX_TURNS);

        // test print population
        //debugPrintChromosomes(starPopulation);

        // evaluate each path by simulating the game
        List<EvaluatedChromosome> evaluatedPopulation = new ArrayList<>(INIT_POPULATION_SIZE);
        for (int popId = 0; popId < starPopulation.size(); popId++) {
            List<Position> chromosome = starPopulation.get(popId);
            int score = evaluateChromosome(chromosome, ash, humans, zombies);
            evaluatedPopulation.add(new EvaluatedChromosome(chromosome, score));
            System.err.println("Evaluating chromosome " + popId+ ". Score: " + score);
        }


        // find the solution with the highest score and return it
        int bestScore = -1;
        List<Position> bestSolution = null;
        for (EvaluatedChromosome chromosome : evaluatedPopulation) {
            if (chromosome.score() > bestScore) {
                bestScore = chromosome.score();
                bestSolution = chromosome.chromosome();
            }
        }

        System.err.println("Found best solution with score: "+bestScore);
        return bestSolution;
    }

    private static void debugPrintChromosomes(List<List<Position>> starPopulation) {
        for (int i = 0; i < starPopulation.size(); i++) {
            System.err.println("Chromosome " + i + ": ");
            System.err.println(starPopulation.get(i).get(0));
            System.err.println(starPopulation.get(i).get(starPopulation.get(i).size()-1));
            System.err.println();
        }
    }

    /**
     * Run the game simulation and evaluate given chromosome. The chromosome is just a list of position Ash will output
     * in each turn.
     *
     * @param chromosome List of positions Ash will output in each turn.
     * @param initialAsh Initial position of Ash.
     * @param initialHumans Initial positions of humans.
     * @param initialZombies Initial positions of zombies.
     * @return Score of the chromosome.
     */
    private static int evaluateChromosome(List<Position> chromosome, Position initialAsh, List<Position> initialHumans, List<Position> initialZombies) {
        GameEngine game = new GameEngine(initialAsh, initialZombies, initialHumans);

        return game.playGame(chromosome);
    }

    private static List<List<Position>> generateStarPopulation(Position ashStart, int populationSize, int chromosomeSize) {
        List<List<Position>> starPopulation = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {
            double angleRad = (i * 2 * Math.PI) / populationSize;
            starPopulation.add(generatePath(ashStart, angleRad, chromosomeSize));
        }

        return starPopulation;
    }

    private static List<Position> generatePath(Position start, double angleRad, int chromosomeSize) {
        List<Position> genes = new ArrayList<>(chromosomeSize);
        // move in given direction until the border of the map is reached
        Position lastPos = start;
        for (int i = 0; i < chromosomeSize; i++) {
            int x = (int) (lastPos.x() + (ASH_SPEED * Math.cos(angleRad)));
            int y = (int) (lastPos.y() + (ASH_SPEED * Math.sin(angleRad)));

            // check constraints
            if (x < 0) {
                x = 0;
            } else if (x >= MAX_X) {
                x = lastPos.x();
            }
            if (y < 0) {
                y = 0;
            } else if (y >= MAX_Y) {
                y = lastPos.y();
            }

            lastPos = new Position(x, y);
            genes.add(lastPos);
        }
        return genes;
    }

    private static void parseZombiePositions(Scanner in, List<Position> zombies) {
        zombies.clear();
        int zombieCount = in.nextInt();
        in.nextLine();
        for (int i = 0; i < zombieCount; i++) {
            zombies.add(parsePositionWithId(in.nextLine()));
        }
    }

    private static void parseHumanPositions(Scanner in, List<Position> humans) {
        humans.clear();
        int humanCount = in.nextInt();
        in.nextLine();
        for (int i = 0; i < humanCount; i++) {
            humans.add(parsePositionWithId(in.nextLine()));
            System.err.println("Human:" + humans.get(i));
        }
    }

    private static Position parsePositionWithId(String str) {
        String[] parts = str.split(" ");
        return new Position(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private static Position parsePosition(String str) {
        String[] parts = str.split(" ");
        return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
