package ch.codingame.codevszombies.solution.genAlg;

import java.util.*;

/**
 * Simple genetic algorithm to find a good solution to the Code vs Zombies game.
 *
 * Initial population is a star-shaped set of paths, all originating from the player's initial position.
 */
class Player {

    // main two parameters to tweak
    private static final int MAX_TURNS = 40;
    private static final int INIT_POPULATION_SIZE = 32;
    private static final int GENERATIONS = 5;
    private static final int N_BEST_TO_MATE = 8;

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
                if (turnCounter >= solution.size()) {
                    // no more turns, game over
                    score = 0;
                    break;
                }

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
            if (playerPosition.squareDistanceFrom(direction) <= (ashSpeed*ashSpeed)) {
                playerPosition = direction.clone();
                return;
            }
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
                debugPrintSolution(solution);
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

    private static void debugPrintSolution(List<Position> solution) {
        System.err.println("Solution:");
        for (Position move : solution) {
            System.err.println(move.x + " " + move.y);
        }
    }

    private static List<Position> findBestSolution(Position ash, List<Position> humans, List<Position> zombies) {

        // initial variables
        List<List<Position>> population;
        List<EvaluatedChromosome> evaluatedPopulation = new ArrayList<>(INIT_POPULATION_SIZE);
        int bestScore = -1;
        List<Position> bestSolution = null;

        // generic algorithm
        for (int generation = 0; generation < GENERATIONS; generation++) {
            if (generation == 0) {
                // first round = initial population
                population = generateStarPopulation(ash, INIT_POPULATION_SIZE, MAX_TURNS);
            } else {
                // we have population from previous round, use it to generate new one
                population = generateOffsprings(evaluatedPopulation, INIT_POPULATION_SIZE);
            }

            // test print population
            //debugPrintChromosomes(starPopulation);

            // evaluate each path by simulating the game
            for (int popId = 0; popId < population.size(); popId++) {
                List<Position> chromosome = population.get(popId);
                int score = evaluateChromosome(chromosome, ash, humans, zombies);
                evaluatedPopulation.add(new EvaluatedChromosome(chromosome, score));
                //if (score > 0) {
                //System.err.println("Evaluating chromosome " + popId+ " of generation "+ generation +". Score: " + score);
                //}

                // find the solution from the current population
                if (score > bestScore) {
                    bestScore = score;
                    bestSolution = chromosome;
                    System.err.println("Found new best. Chromosome "+popId+" of generation "+generation+": "+bestScore);
                }
            }
        }
        return bestSolution;
    }

    private static List<List<Position>> generateOffsprings(List<EvaluatedChromosome> evaluatedPopulation, int initPopulationSize) {

        List<List<Position>> newPopulation = new ArrayList<>(initPopulationSize);

        // select N best pair to mate
        List<EvaluatedChromosome> nBest = evaluatedPopulation.stream()
                .sorted((o1, o2) -> o2.score() - o1.score())
                .limit(N_BEST_TO_MATE)
                .toList();

        // elitism = keep the best one
        newPopulation.add(nBest.get(0).chromosome());

        // pair nBest parents until new population is generated
        while (newPopulation.size() < initPopulationSize) {
            List<EvaluatedChromosome> parents = selectParents(nBest);
            newPopulation.add(mate(parents));
        }

        // todo: mutation


        // return the new population
        return newPopulation;
    }

    private static List<Position> mate(List<EvaluatedChromosome> parents) {
        List<Position> child = new ArrayList<>(MAX_TURNS);

        List<Position> parent1 = parents.get(0).chromosome();
        List<Position> parent2 = parents.get(1).chromosome();

        // lets start with just an average of the two parents
        for (int i = 0; i < MAX_TURNS; i++) {
            Position p1 = parent1.get(i);
            Position p2 = parent2.get(i);
            child.add(new Position((p1.x+p2.x)/2, (p1.y+p2.y)/2));
        }

        return child;
    }

    /**
     * Select parents from the N best chromosomes. Selection algorithm:
     *
     * 1. Normalise the scores of the N best chromosomes to be in range [0,1] so that the sum of all scores is 1.
     * 2. Generate random number R in range [0,1].
     * 3. Iterate over the N best chromosomes and sum their scores. When the sum is greater than R, select the current
     *
     * @param nBest N best chromosomes, nBest.get(0) is the best one.
     * @return Two parents to mate.
     */
    private static List<EvaluatedChromosome> selectParents(List<EvaluatedChromosome> nBest) {
        // normalise scores
        // last index = the worst one
        double[] normalisedAccumulatedScores = new double[nBest.size()];
        int scoreSum = nBest.stream().mapToInt(EvaluatedChromosome::score).sum();
        for (int i = nBest.size()-1; i >= 0; i--) {
            normalisedAccumulatedScores[i] = (double) nBest.get(i).score() / scoreSum;
            if (i < nBest.size()-1) {
                normalisedAccumulatedScores[i] += normalisedAccumulatedScores[i+1];
            }
        }

        // select parents
        int p1Id = -1,
            p2Id = -1;
        double r1 = Math.random(),
               r2 = Math.random();
        for (int i = nBest.size()-1; i >= 0; i--) {

            // first parent
            if (normalisedAccumulatedScores[i] >= r1) {
                p1Id = i;
                break;
            }

            // second parent, because of the break in the previous condition
            // we know that p1Id != p2Id
            if (normalisedAccumulatedScores[i] >= r2) {
                p2Id = i;
                break;
            }
        }

        // if for some reason this algorithm fails, just pick the two best chromosomes
        if (p1Id == -1 || p2Id == -1) {
            p1Id = 0;
            p2Id = 1;
        }

        List<EvaluatedChromosome> parents = new ArrayList<>(2);
        parents.add(nBest.get(p1Id));
        parents.add(nBest.get(p2Id));

        return parents;
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
        // then turn back and move in the opposite direction
        Position lastPos = start;
        double[] direction = new double[] {Math.cos(angleRad), Math.sin(angleRad)};
        for (int i = 0; i < chromosomeSize; i++) {
            int x = (int) (lastPos.x() + (ASH_SPEED * direction[0]));
            int y = (int) (lastPos.y() + (ASH_SPEED * direction[1]));

            // check constraints
            if (x < 0 || x >= MAX_X || y < 0 || y >= MAX_Y) {
                // reverse
                direction[0] *= -1;
                direction[1] *= -1;
                x = (int) (lastPos.x() + (ASH_SPEED * direction[0]));
                y = (int) (lastPos.y() + (ASH_SPEED * direction[1]));

                // check again, if we still hit the border, just stop
                if (x < 0 || x >= MAX_X || y < 0 || y >= MAX_Y) {
                    x = lastPos.x();
                    y = lastPos.y();
                }
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
