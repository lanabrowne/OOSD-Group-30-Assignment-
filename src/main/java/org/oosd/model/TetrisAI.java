package org.oosd.model;

import org.oosd.controller.GameController;
import org.oosd.model.Tetromino;

public class TetrisAI {
    private BoardEvaluator evaluator = new BoardEvaluator();

    /**
     * // Find the best move for the choosen Tetris piece on the board
     *
     *
     * @param piece the tetris piece to place
     * @ return Move object with col and rotation
     */

    public Move findBestMove(int[][] boardSnapshot, int width, int height, Tetromino piece) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int rotation = 0; rotation < 4; rotation++) {
            Tetromino rotatedPiece = piece.copy();
            for (int r = 0; r < rotation; r++) rotatedPiece = rotatedPiece.rotated(1);

            int minCol = 0;
            int maxCol = width - rotatedPiece.getBoundingWidth();

            // Testing AI is using full width of the board
            System.out.println("AI evaluating columns 0 to " + maxCol);
            for (int col = minCol; col <= maxCol; col++) {
                int dropRow = findDropRow(boardSnapshot, rotatedPiece, col, height);
                if (dropRow < 0) continue;

                int[][] simulatedBoard = simulateDrop(boardSnapshot, rotatedPiece, col, dropRow, width, height);
                int score = evaluator.evaluateBoard(simulatedBoard);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Move(col, rotation);
                }
            }
        }

        return bestMove;
    }


    // find the row where the piece will land
    private int findDropRow(int[][] board, Tetromino piece, int col, int boardHeight) {
        int row = 0;
        while (isValidPos(board, piece,row, col, boardHeight)) {
            row++;
        }
        return row - 1;
    }

    // Check if piece can be placed at row/col on given board snapshot
    private boolean isValidPos(int[][] board, Tetromino piece, int row, int col, int boardHeight) {
        int boardWidth = board[0].length;
        for (int[] cell : piece.cells()) {
            int r = row + cell[1];
            int c = col + cell[0];
            if (r < 0 || r >= boardHeight || c < 0 || c >= boardWidth) return false;
            if (board[r][c] != 0) return false;
        }
        return true;
    }


    private int[][] simulateDrop(int[][] board, Tetromino piece, int col, int row, int width, int height){
        int[][] simulated = new int[height][width];

        // Copy board
        for (int r = 0; r < board.length; r++) {
            System.arraycopy(board[r], 0, simulated[r], 0, board[0].length);
        }

        //Place piece
        for (int[] cell : piece.cells()) {
            int r = row + cell[1];
            int c = col + cell[0];
            if (r >= 0 && r < height && c >= 0 && c < width) {
                simulated[r][c] = piece.type.colorId;
            }
        }
        return clearFullLines(simulated, width, height);
    }

    // Clear full lines in simulated board
    private int[][] clearFullLines(int[][] boardArray, int width, int height) {
        int[][] newBoard = new int[height][width];
        int newRow = height - 1;

        for (int r = height - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < width; c++) {
                if (boardArray[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (!full) {
                System.arraycopy(boardArray[r], 0, newBoard[newRow], 0, width);
                newRow--;
            }
        }

        // Top rows are empty
        for (int r = newRow; r >= 0; r--) {
            for (int c = 0; c < width; c++) {
                newBoard[r][c] = 0;
            }
        }
        return newBoard;

    }
}





