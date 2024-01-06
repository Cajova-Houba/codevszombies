package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.GameEngine;
import ch.codingame.codevszombies.GameState;
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
        ChromosomeSolution[] population = generateInitialPopulation(configuration.populationSize());

        EvaluatedChromosome[] evaluatedPopulation = new EvaluatedChromosome[configuration.populationSize()];
        for (int i = 0; i < configuration.generations(); i++) {

            // for each generation, evaluate population
            for (int j = 0; j < configuration.populationSize(); j++) {
                evaluatedPopulation[j] = new EvaluatedChromosome(population[j], evaluateChromosome(population[j], initialState.clone()));
            }

            if (aggregator != null) {
                aggregator.addGeneration(evaluatedPopulation);
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

        // select N best chromosomes for mating
        EvaluatedChromosome[] bestChromosomes = selectNBest(evaluatedPopulation, configuration.matingCount());

        // select paris to mate
        int[][] pairs = selectPairsToMate(configuration.matingCount());
        EvaluatedChromosome[][] matingPairs = createPairsToMate(bestChromosomes, pairs);

        // mating
        ChromosomeSolution[] children = mate(configuration, matingPairs);

        // todo: mutation

        // merge together parents and children into the new population
        ChromosomeSolution[] population = new ChromosomeSolution[evaluatedPopulation.length];
        for (int i = 0; i < bestChromosomes.length; i++) {
            population[i] = bestChromosomes[i].chromosome();
        }
        for (int i = 0; i < children.length; i++) {
            population[i + bestChromosomes.length] = children[i];
        }

        return population;
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


    private ChromosomeSolution[] generateInitialPopulation(int populationSize) {
        ChromosomeSolution[] population = new ChromosomeSolution[populationSize];
        for (int i = 0; i < populationSize; i++) {
            population[i] = generateRandomChromosome();
        }
        return population;
    }

    private int evaluateChromosome(ChromosomeSolution chromosome, GameState initialState) {
        return gameEngine.playGame(initialState, chromosome);
    }

    /**
     * Generate random chromosome. Each gene is a string of "x y" within boundaries of
     * 0 <= x < maxX and 0 <= y < maxY. Chromosome has chromosomeSize genes.
     * @return Random chromosome.
     */
    private ChromosomeSolution generateRandomChromosome() {
        Position[] genes = new Position[chromosomeSize];
        for (int i = 0; i < chromosomeSize; i++) {
            genes[i] = new Position((int)(Math.random() * maxX), (int)(Math.random() * maxY));
        }
        return new ChromosomeSolution(genes);
    }
}
