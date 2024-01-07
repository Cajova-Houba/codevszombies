package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.GameEngine;
import ch.codingame.codevszombies.GameState;
import ch.codingame.codevszombies.GameplayRecorder;
import ch.codingame.codevszombies.Position;
import ch.codingame.codevszombies.export.SvgExporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ContinuousGenericAlgorithmTest {

    @Test
    void selectPairsToMate_evenMatingPopulation() {
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 12);

        int[][] pairs = algorithm.selectPairsToMate(6);

        assertEquals(3, pairs.length);

        // check there are no duplicates
        Set<Integer> indexes = new HashSet<>();
        for (int[] pair : pairs) {
            for (int index : pair) {
                assertFalse(indexes.contains(index));
                indexes.add(index);
            }
        }
    }

    @Test
    void selectPairsToMate_oddMatingPopulation() {
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 12);

        int[][] pairs = algorithm.selectPairsToMate(5);

        assertEquals(2, pairs.length);

        // check there are no duplicates
        Set<Integer> indexes = new HashSet<>();
        for (int[] pair : pairs) {
            for (int index : pair) {
                assertFalse(indexes.contains(index));
                indexes.add(index);
            }
        }
    }

    @Test
    void selectNBest() {
        final int chromosomeCount = 10;
        final int nBest = 5;
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 12);
        final EvaluatedChromosome[] chromosomes = new EvaluatedChromosome[chromosomeCount];
        for (int i = 0; i < chromosomes.length; i++) {
            chromosomes[i] = new EvaluatedChromosome(new ChromosomeSolution(new Position(i, -i)), i);
        }

        List<EvaluatedChromosome> shuffled = new ArrayList<>(List.of(chromosomes));
        Collections.shuffle(shuffled);
        final EvaluatedChromosome[] result = algorithm.selectNBest(shuffled.toArray(new EvaluatedChromosome[]{}), nBest);

        assertEquals(nBest, result.length);
        assertEquals(9, result[0].score());
        assertEquals(8, result[1].score());
        assertEquals(7, result[2].score());
        assertEquals(6, result[3].score());
        assertEquals(5, result[4].score());
    }

    @Test
    void run_simple_1Gen() {
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 12);
        final GameState game = prepareSimpleGameState();
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(10, 1, 5, 0,0.5f, 0f);
        final EvaluatedChromosome[] result = algorithm.run(configuration, game);

        // print
        for (int i = 0; i < result.length; i++) {
            System.out.println("Chromosome " + i + ": ");
            for (int j = 0; j < result[i].chromosome().getMoves().length; j++) {
                System.out.println("Gene " + j + ": " + result[i].chromosome().getMoves()[j]);
            }
            System.out.println("Score: " + result[i].score());
        }
    }

    @Test
    void run_simple_2Gen() {
        final int chromoSize = 12;
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, chromoSize);
        final GameState game = prepareSimpleGameState();
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(20, 2, 10, 0, 0.5f, 0.2f);
        final EvaluatedChromosome[] result = algorithm.run(configuration, game);

        // print
        for (int i = 0; i < result.length; i++) {
            System.out.println("Chromosome " + i + ": ");
            for (int j = 0; j < result[i].chromosome().getMoves().length; j++) {
                System.out.println("Gene " + j + ": " + result[i].chromosome().getMoves()[j]);
            }
            System.out.println("Score: " + result[i].score());
        }
    }

    @Test
    void run_comboOpportunity_20gen() {
        final int chromoSize = 40;
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 40);
        final GameState game = prepareComboOpportunityGameState();
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(20, 20, 10, chromoSize - 10,0.5f, 0.2f);
        final ResultsAggregator aggregator = new ResultsAggregator();
        final EvaluatedChromosome[] result = algorithm.run(configuration, game, aggregator);

        printResults(result, aggregator);

        saveGameplayToSvg(game, aggregator, "comboOpportunity");

        assertNotEquals(0, aggregator.getBestScore());
    }

    @Test
    void run_unavoidableDeaths_20gen() {
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(GameEngine.MAX_X, GameEngine.MAX_Y, 40);
        final GameState game = prepareUnavoidableDeathsGameState();
        // todo: populationSize + matingCount only works when matingCount = populationSize/2, otherwise new population is not filled properly
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(60, 20, 30, 0,0.5f, 0.2f);
        final ResultsAggregator aggregator = new ResultsAggregator();
        final EvaluatedChromosome[] result = algorithm.run(configuration, game, aggregator);

        printResults(result, aggregator);

        saveGameplayToSvg(game, aggregator, "unavoidableDeaths");

        assertNotEquals(0, aggregator.getBestScore());
    }

    private void saveGameplayToSvg(GameState initialState, ResultsAggregator aggregator, String filename) {
        List<GameplayRecorder[]> generationGameplays = aggregator.getGenerationGameplays();
        int[] best = aggregator.getBest();
        GameplayRecorder gameplay = generationGameplays.get(best[0])[best[1]];

        String svg = SvgExporter.exportToSvg(initialState, gameplay, GameEngine.MAX_X, GameEngine.MAX_Y);

        try {
            Files.writeString(Paths.get(filename+".svg"), svg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResults(EvaluatedChromosome[] result, ResultsAggregator aggregator) {
        int maxScore = 0;
        for (int i = 0; i < result.length; i++) {
            System.out.println("Chromosome " + i + ": ");
            for (int j = 0; j < result[i].chromosome().getMoves().length; j++) {
                System.out.println("Gene " + j + ": " + result[i].chromosome().getMoves()[j]);
            }
            if (result[i].score() > maxScore) {
                maxScore = result[i].score();
            }
            System.out.println("Score: " + result[i].score());
        }
        System.out.println("Max score: " + aggregator.getBestScore());
        System.out.println("Best trend: " + Arrays.toString(aggregator.getBestTrend()));
        System.out.println("Best ids: " + Arrays.toString(aggregator.getBestIds()));
        System.out.println("Worst trend: " + Arrays.toString(aggregator.getWorstTrend()));
        System.out.println("Mean trend: " + Arrays.toString(aggregator.getMeanTrend()));
    }

    private GameState prepareUnavoidableDeathsGameState() {
        return new GameState(
                new ArrayList<>(List.of(
                        new Position(0,3033),
                        new Position(1500,6251),
                        new Position(3000,2502),
                        new Position(4500,6556),
                        new Position(6000,3905),
                        new Position(7500,5472),
                        new Position(10500,2192),
                        new Position(12000,6568),
                        new Position(13500,7448)
                )),
                new Position(9000,684),
                new ArrayList<>(List.of(
                        new Position(15999,4500),
                        new Position(8000,7999),
                        new Position(0,4500)
                ))
        );
    }

    private GameState prepareComboOpportunityGameState() {
        return new GameState(
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
    }

    private GameState prepareSimpleGameState() {
        // based on the test case 1 from codingame
        return new GameState(
                new ArrayList<>(List.of(new Position(8250,8999))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(8250,4500)))
        );
    }
}