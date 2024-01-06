package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatedChromosomeTest {

    @Test
    void mateWith() {
        final EvaluatedChromosome chromosome1 = new EvaluatedChromosome(new ChromosomeSolution(new Position(100, 200), new Position(300, 400)), 1);
        final EvaluatedChromosome chromosome2 = new EvaluatedChromosome(new ChromosomeSolution(new Position(500, 600), new Position(700, 800)), 1);

        final ChromosomeSolution[] children = chromosome1.mateWith(chromosome2, 0.5f);

        // both children should have the same moves
        for ( ChromosomeSolution child : children ) {
            assertEquals(2, child.getMoves().length);
            assertEquals(2, child.getMoves().length);
            assertEquals(300, child.getMoves()[0].x());
            assertEquals(400, child.getMoves()[0].y());
            assertEquals(500, child.getMoves()[1].x());
            assertEquals(600, child.getMoves()[1].y());
        }
    }
}