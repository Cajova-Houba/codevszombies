package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.GameEngine;
import ch.codingame.codevszombies.GameState;
import ch.codingame.codevszombies.GameplayRecorder;
import ch.codingame.codevszombies.Position;

import java.util.*;

public class ContinuousGenericAlgorithm {

    private final int maxX;
    private final int maxY;

    private final int chromosomeSize;

    private GameEngine gameEngine;

    public ContinuousGenericAlgorithm(int maxX, int maxY, int chromosomeSize) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.chromosomeSize = chromosomeSize;
        this.gameEngine = new GameEngine();
    }

    public EvaluatedChromosome[] run(AlgorithmConfiguration configuration, GameState initialState, ResultsAggregator aggregator) {
        // generate initial population
        //ChromosomeSolution[] population = generateInitialPopulation(configuration.populationSize());
        ChromosomeSolution[] population = generateStarInitialPopulation(configuration.populationSize(), initialState.getAsh());

        EvaluatedChromosome[] evaluatedPopulation = new EvaluatedChromosome[configuration.populationSize()];
        for (int i = 0; i < configuration.generations(); i++) {

            // for each generation, evaluate population
            GameplayRecorder[] gameplays = new GameplayRecorder[configuration.populationSize()];
            for (int j = 0; j < configuration.populationSize(); j++) {
                evaluatedPopulation[j] = new EvaluatedChromosome(population[j], evaluateChromosome(population[j], initialState.clone()));
                gameplays[j] = gameEngine.getLastGameplayRecord();
            }

            if (aggregator != null) {
                aggregator.addGeneration(evaluatedPopulation, gameplays);
            }

            // based on the evaluation, generate new population
            population = generateNewPopulation(configuration, evaluatedPopulation);
        }
        return evaluatedPopulation;
    }

    public EvaluatedChromosome[] run(AlgorithmConfiguration configuration, GameState initialState) {
        return run(configuration, initialState, null);
    }

    /**
     * Select N best chromosome. Bigger score = better chromosome.
     * @param evaluatedPopulation Population to select N best from.
     * @param n Number of best chromosomes to select.
     * @return Array of N best chromosomes.
     */
    EvaluatedChromosome[] selectNBest(EvaluatedChromosome[] evaluatedPopulation, int n) {
        return Arrays.stream(evaluatedPopulation)
                .sorted((o1, o2) -> o2.score() - o1.score())
                .limit(n)
                .toArray(EvaluatedChromosome[]::new);
    }

    int[][] selectPairsToMate(int matingPopulationCount) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < matingPopulationCount; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        int[][] pairs = new int[matingPopulationCount / 2][2];

        for (int i = 0; i < matingPopulationCount / 2; i++) {
            pairs[i][0] = indexes.get(i * 2);
            pairs[i][1] = indexes.get(i * 2 + 1);
        }

        return pairs;
    }

    /**
     * Create pairs of chromosomes based on indexes.
     * @param bestChromosomes Chromosomes to create pairs from.
     * @param pairs Indexes of chromosomes to create pairs from. E.g. [[0, 1], [2, 3]] means that pairs
     *              will be created from chromosomes with indexes 0 and 1 and from chromosomes with indexes 2 and 3.
     * @return Pairs for mating.
     */
    EvaluatedChromosome[][] createPairsToMate(EvaluatedChromosome[] bestChromosomes, int[][] pairs) {
        EvaluatedChromosome[][] matingPairs = new EvaluatedChromosome[pairs.length][2];
        for (int i = 0; i < pairs.length; i++) {
            matingPairs[i][0] = bestChromosomes[pairs[i][0]];
            matingPairs[i][1] = bestChromosomes[pairs[i][1]];
        }

        return matingPairs;
    }

    ChromosomeSolution[] generateNewPopulation(AlgorithmConfiguration configuration, EvaluatedChromosome[] evaluatedPopulation) {

        List<ChromosomeSolution> population = new ArrayList<>();

        // select N best chromosomes for mating
        EvaluatedChromosome[] bestChromosomes = selectNBest(evaluatedPopulation, configuration.matingCount());

        // select paris to mate
        int[][] pairs = selectPairsToMate(configuration.matingCount());
        EvaluatedChromosome[][] matingPairs = createPairsToMate(bestChromosomes, pairs);

        // mate until the new population is full
        while(population.size() < configuration.populationSize()) {
            ChromosomeSolution[] children = mate(configuration, matingPairs);
            population.addAll(Arrays.asList(children));
        }

        ChromosomeSolution[] populationArray = population.toArray(new ChromosomeSolution[population.size()]);

        // mutate
        mutatePopulation(configuration, populationArray);

        return populationArray;
    }

    void mutatePopulation(AlgorithmConfiguration configuration, ChromosomeSolution[] population) {
        int mutationCount = (int) (configuration.mutationRate() * population.length * chromosomeSize * 2);

        // chromosomes in population
        int[] pi = new int[mutationCount];

        // genes in chromosome
        int[] ci = new int[mutationCount];

        // coordinates in gene
        int[] gi = new int[mutationCount];

        // randomly select coordinates in genes in chromosomes to mutate
        for(int i = 0; i < mutationCount; i++) {
            pi[i] = (int) (Math.random() * population.length);
            ci[i] = (int) (Math.random() * chromosomeSize);
            gi[i] = (int) (Math.random() * 2);
        }

        // mutate
        for(int i = 0; i < mutationCount; i++) {
            int newValue = gi[i] == 0 ? randomXInRange() : randomYInRange();
            population[pi[i]].mutate(ci[i], gi[i], newValue);
        }
    }

    /**
     * Mate given pairs of chromosomes.
     * @param configuration Algorithm configuration, {@link AlgorithmConfiguration#matingCoefficient()} is used.
     * @param matingPairs Array of pairs of chromosomes to mate.
     * @return Array of children chromosomes.
     */
    ChromosomeSolution[] mate(AlgorithmConfiguration configuration, EvaluatedChromosome[][] matingPairs) {
        ChromosomeSolution[] children = new ChromosomeSolution[matingPairs.length*2];

        for (int i = 0; i < matingPairs.length; i++) {
            ChromosomeSolution[] childrenPair = matingPairs[i][0].mateWith(matingPairs[i][1], configuration.matingCoefficient());
            children[i*2] = childrenPair[0];
            children[(i*2) + 1] = childrenPair[1];
        }

        return children;
    }

    /**
     * star as initial population:
     * - star has populationSize rays
     * - rays are evenly distributed around the star
     * - each ray is a straight path from ash to the border of the map
     */
    private ChromosomeSolution[] generateStarInitialPopulation(int populationSize, Position ashStart) {
        ChromosomeSolution[] population = new ChromosomeSolution[populationSize];
        for (int i = 0; i < populationSize; i++) {
            double angleRad = (i * 2 * Math.PI) / populationSize;
            population[i] = generateStarChromosome(chromosomeSize, ashStart, angleRad);
        }
        return population;
    }

    private ChromosomeSolution generateStarChromosome(int chromosomeSize, Position ashStart, double angleRad) {
        Position[] genes = new Position[chromosomeSize];

        // move in given direction until the border of the map is reached
        Position lastPos = ashStart;
        for (int i = 0; i < chromosomeSize; i++) {
            int x = (int) (lastPos.x() + (GameEngine.ASH_SPEED * Math.cos(angleRad)));
            int y = (int) (lastPos.y() + (GameEngine.ASH_SPEED * Math.sin(angleRad)));

            // check constraints
            if (x < 0) {
                x = 0;
            } else if (x >= maxX) {
                x = lastPos.x();
            }
            if (y < 0) {
                y = 0;
            } else if (y >= maxY) {
                y = lastPos.y();
            }

            genes[i] = new Position(x, y);
            lastPos = genes[i];
        }
        return new ChromosomeSolution(genes);
    }

    private ChromosomeSolution[] generateInitialPopulation(int populationSize) {
        ChromosomeSolution[] population = new ChromosomeSolution[populationSize];
        for (int i = 0; i < populationSize; i++) {
            population[i] = generateRandomChromosome();
        }
        return population;
    }

    private int evaluateChromosome(ChromosomeSolution chromosome, GameState initialState) {
        return gameEngine.playGame(initialState, chromosome, true);
    }

    private int randomXInRange() {
        return (int)(Math.random() * maxX);
    }

    private int randomYInRange() {
        return (int)(Math.random() * maxY);
    }

    /**
     * Generate random chromosome. Each gene is a string of "x y" within boundaries of
     * 0 <= x < maxX and 0 <= y < maxY. Chromosome has chromosomeSize genes.
     * @return Random chromosome.
     */
    private ChromosomeSolution generateRandomChromosome() {
        Position[] genes = new Position[chromosomeSize];
        for (int i = 0; i < chromosomeSize; i++) {
            genes[i] = new Position(randomXInRange(), randomYInRange());
        }
        return new ChromosomeSolution(genes);
    }
}
