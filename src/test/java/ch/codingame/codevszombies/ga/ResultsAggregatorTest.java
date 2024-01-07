package ch.codingame.codevszombies.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultsAggregatorTest {

    private ResultsAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new ResultsAggregator();
    }

    @Test
    void evaluateGeneration() {
        final EvaluatedChromosome[] generation = createTestGeneration();

        EvaluatedGeneration evaluated = aggregator.evaluateGeneration(generation);

        assertEquals(101, evaluated.bestScore);
        assertEquals(4, evaluated.worstScore);
        assertEquals(63, evaluated.meanScore);
        assertEquals(52, evaluated.averageScore);
        assertEquals(3, evaluated.bestId);
    }

    private EvaluatedChromosome[] createTestGeneration() {
        return new EvaluatedChromosome[] {
                new EvaluatedChromosome(new ChromosomeSolution(), 4),
                new EvaluatedChromosome(new ChromosomeSolution(), 100),
                new EvaluatedChromosome(new ChromosomeSolution(), 50),
                new EvaluatedChromosome(new ChromosomeSolution(), 101),
        };
    }
}