package ch.codingame.codevszombies.solution.star1gen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class PlayerTest {

    private InputStream originalSysIn;

    @BeforeEach
    void setUp() {
        originalSysIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSysIn);
    }

    @Test
    void testSimple() {

    }
}