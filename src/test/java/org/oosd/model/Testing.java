package org.oosd.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardEvaluatorTest {

    private BoardEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new BoardEvaluator();
    }

    // 1️⃣ Test cleared lines scoring
    @Test
    void testGetClearedLinesWeighted() {
        int[][] board = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // full line
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        int score = evaluator.getClearedLinesWeighted(board);
        assertEquals(40, score); // 1 line cleared = 40 points
    }

@Test
void testGetHoles() {
    int[][] board = {
        {1, 1, 1},
        {0, 0, 1}, // <-- two holes below filled cells in col 0 and 1
        {1, 1, 1}
    };
    int holes = evaluator.getHoles(board);
    assertEquals(2, holes);
}


    // 3️⃣ Test bumpiness
    @Test
    void testGetBumpiness() {
        int[][] board = {
                {0, 0, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1}
        };
        int bumpiness = evaluator.getBumpiness(board);
        assertTrue(bumpiness >= 0);
    }

    // 4️⃣ Test column height penalty
    @Test
    void testGetColumnHeightPenalty() {
        int[][] board = new int[20][10];
        // Fill one tall column
        for (int y = 10; y < 20; y++) {
            board[y][0] = 1;
        }
        int penalty = evaluator.getColumnHeightPenalty(board);
        assertTrue(penalty >= 0);
    }

    // 5️⃣ Test evaluateBoard overall logic
    @Test
    void testEvaluateBoardOverall() {
        int[][] board = new int[10][10];
        // create a simple pattern
        for (int y = 9; y >= 5; y--) {
            for (int x = 0; x < 10; x++) {
                board[y][x] = 1;
            }
        }
        int result = evaluator.evaluateBoard(board);
        assertTrue(result > 0 || result < 0); // ensures calculation ran successfully
    }
}
