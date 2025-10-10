//advanced testing
package org.oosd.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class BoardEvaluatorTest {

    /**
     * PARAMETERIZED TEST
     * Demonstrates a parameterized test with a STUB BoardEvaluator
     */
    @ParameterizedTest(name = "Board {0}x{1} should have non-negative score")
    @CsvSource({
        "10,20",
        "5,5",
        "15,10"
    })
    void parameterizedTestBoardEvaluation(int width, int height) {
        // Stub: returns fixed score regardless of input
        BoardEvaluator stubEvaluator = new BoardEvaluator() {
            @Override
            public int evaluateBoard(int[][] board) {
                return 100; // stub: always positive
            }
        };

        int[][] board = new int[height][width];
        int score = stubEvaluator.evaluateBoard(board);

        System.out.println("Board " + width + "x" + height + " score: " + score);
        assertTrue(score >= 0, "Board score should be non-negative");
    }

    /**
     * STUB TEST
     * Demonstrates a stub explicitly
     */
    @Test
    void testWithStub() {
        BoardEvaluator stubEvaluator = new BoardEvaluator() {
            @Override
            public int evaluateBoard(int[][] board) {
                return 50;
            }
        };

        int[][] board = new int[5][5];
        assertEquals(50, stubEvaluator.evaluateBoard(board), "Stub should always return 50");
    }

    /**
     * FAKE TEST
     * Demonstrates a fake with minimal logic
     */
    @Test
    void testWithFake() {
        class FakeBoardEvaluator extends BoardEvaluator {
            @Override
            public int evaluateBoard(int[][] board) {
                // fake: returns board size as score
                return board.length * board[0].length;
            }
        }

        BoardEvaluator fakeEvaluator = new FakeBoardEvaluator();
        int[][] board = new int[4][3];

        assertEquals(12, fakeEvaluator.evaluateBoard(board), "Fake should return board area");
    }

    /**
     * MOCK TEST
     * Demonstrates Mockito mock
     */
    @Test
    void testWithMock() {
        BoardEvaluator mockEvaluator = Mockito.mock(BoardEvaluator.class);

        int[][] dummyBoard = new int[2][2];
        when(mockEvaluator.evaluateBoard(dummyBoard)).thenReturn(999);

        int result = mockEvaluator.evaluateBoard(dummyBoard);

        assertEquals(999, result, "Mock should return configured value");
        verify(mockEvaluator).evaluateBoard(dummyBoard);
    }

    /**
     * SPY TEST
     * Demonstrates Mockito spy
     */
    @Test
    void testWithSpy() {
        BoardEvaluator realEvaluator = new BoardEvaluator();
        BoardEvaluator spyEvaluator = Mockito.spy(realEvaluator);

        int[][] board = new int[2][2];
        doReturn(777).when(spyEvaluator).evaluateBoard(board);

        int result = spyEvaluator.evaluateBoard(board);

        assertEquals(777, result, "Spy should override real method return");
    }
}
