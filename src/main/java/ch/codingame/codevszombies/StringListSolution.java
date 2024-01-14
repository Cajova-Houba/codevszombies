package ch.codingame.codevszombies;


import java.util.List;

public class StringListSolution implements ISolution {

    private final String[] moves;
    private int index = 0;

    public StringListSolution(String... moves) {
        this.moves = moves;
    }

    public StringListSolution(List<String> moves) {
        this(moves.toArray(new String[0]));
    }

    @Override
    public boolean hasNextMove() {
        return index < moves.length;
    }

    @Override
    public String getNextMove() {
        if (!hasNextMove()) {
            return "0 0";
        }
        return moves[index++];
    }
}
