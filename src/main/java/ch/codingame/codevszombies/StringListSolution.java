package ch.codingame.codevszombies;


public class StringListSolution implements ISolution {

    private final String[] moves;
    private int index = 0;

    public StringListSolution(String... moves) {
        this.moves = moves;
    }

    @Override
    public String getNextMove() {
        return moves[index++];
    }
}
