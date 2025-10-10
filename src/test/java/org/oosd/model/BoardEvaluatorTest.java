package org.oosd.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BoardEvaluatorTest {

    private BoardEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new BoardEvaluator();
    }

    static Stream<BoardTestCase> boardProvider() {
        return Stream.of(
                new BoardTestCase(new int[20][10], "Empty board should have lowest score"),
                new BoardTestCase(singleLineBoard(), "Single filled line should have higher score than empty board"),
                new BoardTestCase(filledBoard(), "Fully filled board should have higher score than single line board"),
                new BoardTestCase(boardWithHoles(), "Board with holes should have lower score than filled board")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("boardProvider")
    void testEvaluateBoard(BoardTestCase testCase) {
        int score = evaluator.evaluateBoard(testCase.board);
        testCase.score = score; // store score for later comparison
    }

    @org.junit.jupiter.api.Test
    void testRelativeScores() {
        int emptyScore = evaluator.evaluateBoard(new int[20][10]);
        int singleLineScore = evaluator.evaluateBoard(singleLineBoard());
        int filledScore = evaluator.evaluateBoard(filledBoard());
        int holesScore = evaluator.evaluateBoard(boardWithHoles());

        assertTrue(singleLineScore > emptyScore, "Single line should score higher than empty board");
        assertTrue(filledScore > singleLineScore, "Filled board should score higher than single line board");
        assertTrue(holesScore < filledScore, "Board with holes should score lower than filled board");
    }

    private static class BoardTestCase {
        int[][] board;
        String message;
        int score;

        BoardTestCase(int[][] board, String message) {
            this.board = board;
            this.message = message;
        }
    }

    private static int[][] singleLineBoard() {
        int[][] board = new int[20][10];
        for (int i = 0; i < 10; i++) board[19][i] = 1;
        return board;
    }

    private static int[][] filledBoard() {
        int[][] board = new int[20][10];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                board[y][x] = 1;
            }
        }
        return board;
    }

    private static int[][] boardWithHoles() {
        int[][] board = new int[20][10];
        board[18][0] = 1;
        board[19][0] = 1;
        return board;
    }
}
