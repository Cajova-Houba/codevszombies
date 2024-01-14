package ch.codingame.codevszombies;
public interface ISolution {

    String getNextMove();

    default void init() {};

    default boolean hasNextMove() {
        return true;
    };
}
