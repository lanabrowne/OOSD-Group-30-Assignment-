/*package org.oosd.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

class AdvancedTesting {

    /**
     *  Demonstrates a PARAMETERIZED TEST (JUnit 5)
     * Runs multiple board sizes through the real BoardEvaluator.
     */
  /*   @ParameterizedTest(name = "Board {0}x{1} should return a valid score")
    @CsvSource({
            "4,4",
            "6,10",
            "10,20"
    })
    void testParameterizedBoardEvaluation(int width, int height) {
        BoardEvaluator evaluator = new BoardEvaluator();
        int[][] board = new int[height][width]; // all empty cells

        int score = evaluator.evaluateBoard(board);
        System.out.println("Board " + width + "x" + height + " => score: " + score);

        // The result should be numeric (not crashing or invalid)
        assertTrue(score <= 0, "Empty board should have non-positive score");
    }

    /**
     * Demonstrates a TEST DOUBLE- STUB
     * Creates a stubbed BoardEvaluator that always returns a fixed score.
     */
   /*  @Test
    void testWithStub() {
        // Stub class overriding evaluateBoard
        BoardEvaluator stubEvaluator = new BoardEvaluator() {
            @Override
            public int evaluateBoard(int[][] board) {
                return 100; // fixed score for testing
            }
        };

        int[][] board = new int[5][5];
        int score = stubEvaluator.evaluateBoard(board);

        System.out.println("Stub score: " + score);
        assertEquals(100, score, "Stub should always return 100");
    }

    /**
     *  Demonstrates use of a MOCK 
     * Mocks BoardEvaluator behavior and verifies method call.
     */
 /*    @Test
    void testWithMock() {
        // Create a Mockito mock of BoardEvaluator
        BoardEvaluator mockEvaluator = Mockito.mock(BoardEvaluator.class);

        int[][] dummyBoard = new int[2][2];
        when(mockEvaluator.evaluateBoard(dummyBoard)).thenReturn(999);

        int result = mockEvaluator.evaluateBoard(dummyBoard);

        // Validate mock behavior
        assertEquals(999, result, "Mock should return configured value");
        verify(mockEvaluator).evaluateBoard(dummyBoard);
    }
}
*/