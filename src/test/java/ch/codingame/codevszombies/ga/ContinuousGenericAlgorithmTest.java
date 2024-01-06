package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.GameState;
import ch.codingame.codevszombies.Position;
import org.junit.jupiter.api.Test;

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
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(10, 1, 5, 0.5f, 0.2f);
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
        final ContinuousGenericAlgorithm algorithm = new ContinuousGenericAlgorithm(16000, 9000, 12);
        final GameState game = prepareSimpleGameState();
        final AlgorithmConfiguration configuration = new AlgorithmConfiguration(20, 2, 10, 0.5f, 0.2f);
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

    private GameState prepareSimpleGameState() {
        // based on the test case 1 from codingame
        return new GameState(
                new ArrayList<>(List.of(new Position(8250,8999))),
                new Position(0, 0),
                new ArrayList<>(List.of(new Position(8250,4500)))
        );
    }
}