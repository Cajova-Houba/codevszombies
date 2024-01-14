package ch.codingame.codevszombies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTest {

    private GameState twoZombiesInitial;
    private GameState rowsToDefendInitial;

    @BeforeEach
    void setUp() {
        twoZombiesInitial = new GameState(
                List.of(new Position(3100,7000), new Position(11500,7100)),
                new Position(5000, 0),
                new ArrayList<>(List.of(new Position(950,6000), new Position(8000,6100)))
        );

        List<Position> zombies = new ArrayList<>();
        // 2 rows of 8 zombies
        for (int i = 0; i < 8; i++) {
            if (i < 5) {
                zombies.add(new Position(5000 + i*2000,1000));
                zombies.add(new Position(5000 + i*2000,8000));
            } else {
                int j = i - 5;
                zombies.add(new Position(14000 + j*500,1000));
                zombies.add(new Position(14000 + j*500,8000));
            }
        }
        rowsToDefendInitial = new GameState(
                zombies,
                new Position(0, 4000),
                new ArrayList<>(List.of(new Position(0,1000), new Position(0,8000)))
        );
    }

    @Test
    void twoZombies_fail() {
        // this solution failed in codingame
        final ISolution solution = new StringListSolution(
                "4444 831", "3888 1662", "3332 2493", "2776 3324",
                        "2220 4155", "1664 4986", "1108 5817", "552 6648" ,
                        "0 7479", "0 8310", "0 8310", "0 8310", "0 8310" ,
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310", "0 8310", "0 8310", "0 8310",
                        "0 8310", "0 8310"
        );
        final int expectedScore = 0;

        GameEngine engine = new GameEngine();
        int score = engine.playGame(twoZombiesInitial.clone(), solution);

        assertEquals(expectedScore, score);
    }

    @Test
    void rowsToDefend_fail() throws FileNotFoundException {
        // this solution failed in codingame
        final ISolution solution = loadSolutionFromResource("rowsToDefend_1.txt");
        final int expectedScore = 0;

        GameEngine engine = new GameEngine();
        int score = engine.playGame(rowsToDefendInitial.clone(), solution);

        assertEquals(expectedScore, score);
    }

    private ISolution loadSolutionFromResource(String filename) throws FileNotFoundException {
        final String fullFilename = "solutions/failed/"+filename;
        URL resource = this.getClass().getClassLoader().getResource(fullFilename);

        if (resource == null) {
            throw new IllegalArgumentException("file "+fullFilename+" not found!");
        }

        File resourceFile = new File(resource.getFile());

        // read lines from file
        List<String> lines = new ArrayList<>();

        Scanner in = new Scanner(resourceFile);
        while (in.hasNextLine()) {
            lines.add(in.nextLine());
        }

        return new StringListSolution(lines);
    }
}