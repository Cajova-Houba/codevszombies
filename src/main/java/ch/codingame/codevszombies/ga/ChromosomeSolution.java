package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.ISolution;
import ch.codingame.codevszombies.Position;

/**
 * One chromosome of generic algorithm. String of "x y" is one gene.
 */
public class ChromosomeSolution implements ISolution {

        private final Position[] moves;
        private int index = 0;

        public ChromosomeSolution(Position... moves) {
            this.moves = moves;
        }

    @Override
    public void init() {
        this.index = 0;
    }

    public Position[] getMoves() {
            return moves;
        }

        @Override
        public String getNextMove() {
            Position nextMove = moves[index++];
            return nextMove.x() + " " + nextMove.y();
        }

    public void mutate(int geneIndex, int coordinate, int newValue) {
        Position geneToMutate = moves[geneIndex];
        if (coordinate == 0) {
            moves[geneIndex] = new Position(newValue, geneToMutate.y());
        } else {
            moves[geneIndex] = new Position(geneToMutate.x(), newValue);
        }
    }
}
