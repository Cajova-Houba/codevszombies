package ch.codingame.codevszombies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void moveTo_stationary() {
        final Position position = new Position(10, 10);
        final Position newPosition = position.moveTo(position, 100);

        assertEquals(10, newPosition.x());
        assertEquals(10, newPosition.y());
    }

    @Test
    void moveTo() {
        final Position position = new Position(8250,8999);
        final Position target = new Position(8250,4500);

        final Position newPosition = position.moveTo(target, 400);

        assertEquals(8250, newPosition.x());
        assertEquals(8599, newPosition.y());
    }
}