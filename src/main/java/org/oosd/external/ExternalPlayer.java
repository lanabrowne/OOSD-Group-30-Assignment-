package org.oosd.external;


import javafx.application.Platform;
import org.oosd.controller.GameController;
import org.oosd.controller.TwoPlayerController;
import org.oosd.model.Tetromino;
import org.oosd.model.Board;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalPlayer {
    private final ExternalClient client;
    private final TwoPlayerController controller;
    public OpMove pendingMove = null;

    //Set boolean to identify which screen the server
    //tries to send action to
    //Ex: if isLeft false == right screen, True == left screen
    private final boolean isLeft;

    private Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;

    private Thread listeningThread;

    private Runnable onConnectionLost;

    private Runnable onReconnected;
    private ScheduledExecutorService retryExec;

    public void setOnConnectionLost(Runnable handler) {
        this.onConnectionLost = handler;
    }


    //Initialize the external client and controller
    public ExternalPlayer(TwoPlayerController controller, boolean isLeft)
    {
        this.client = new ExternalClient();
        this.controller = controller;
        this.isLeft = isLeft;
        this.client.setPlayer(this);
    }
    public void updateBoardState(Board board, Tetromino currentPiece, Tetromino nextPiece) {
        this.board = board;
        this.currentPiece = currentPiece;
        this.nextPiece = nextPiece;
    }

    public void setCurrentPiece(org.oosd.model.Tetromino piece) {
        this.currentPiece = piece;
    }

    public void setNextPiece(org.oosd.model.Tetromino piece) {
        this.nextPiece = piece;
    }

    public void setOnReconnected(Runnable handler) { this.onReconnected = handler; }


    /**
     * This method is connecting to server
     * @return
     */
    public boolean connectToServer()
    {
        boolean ok = client.connect();
        if (!ok) {
            notifyConnectionLost("Failed to connect to server");
        }
        //Set host name and port number to connect to server
        return client.connect();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void updateGameState(Board board, Tetromino currentPiece, Tetromino nextPiece)
    {
        this.board = board;
        this.currentPiece = currentPiece;
        this.nextPiece = nextPiece;
    }

    private static int[][] toMatrixFromCoords(int[][] coords) {
        if (coords == null || coords.length == 0) {
            return new int[][]{{0}};
        }
        int maxRow = 0, maxCol = 0;
        for (int[] p : coords) {
            if (p == null || p.length < 2) continue;
            maxRow = Math.max(maxRow, p[0]);
            maxCol = Math.max(maxCol, p[1]);
        }
        int[][] m = new int[maxRow + 1][maxCol + 1];
        for (int[] p : coords) {
            if (p == null || p.length < 2) continue;
            m[p[0]][p[1]] = 1;
        }
        return m;
    }

    /**
     * Receive operation from server and apply
     */
    private boolean isSending = false;


    //send my action to server (This one will be used only network connection for p vs p)
    public void sendAction()
    {
        if (client == null || board == null || currentPiece == null) {
            System.err.println("Cannot send action: missing client, board, or current piece.");
            return;
        }

        try {
            // create one line Json file
            String jsonGame = buildJsonOneLine(board, currentPiece, nextPiece);

            System.out.println("? Sent manual JSON: " + jsonGame);

            // sned to server and collect response
            OpMove move = client.requestMove(jsonGame);

            if (move == null) {
                System.err.println("⚠️ No move received from server.");
                return;
            }

            System.out.println("Received OpMove: " + move);

            //Platform.runLater(() -> controller.applyExternalMove(move, isLeft));
            System.out.println("Received OpMove: " + move);
            Platform.runLater(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
                controller.applyExternalMove(move, isLeft);
            });

        }catch (Exception e) {

            System.err.println("Connection lost: " + e.getMessage());
            notifyConnectionLost(e.getMessage());
            if (onConnectionLost != null) onConnectionLost.run();
        }
    }

    private String arrayToJson(int[][] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append("[");
            for (int j = 0; j < array[i].length; j++) {
                sb.append(array[i][j]);
                if (j < array[i].length - 1) sb.append(",");
            }
            sb.append("]");
            if (i < array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildJsonOneLine(Board board, Tetromino current, Tetromino next) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"width\":").append(board.w).append(",");
        json.append("\"height\":").append(board.h).append(",");
        json.append("\"cells\":").append(arrayToJson(board.snapshot())).append(",");
        json.append("\"currentShape\":").append(arrayToJson(current.cells())).append(",");
        json.append("\"nextShape\":").append(arrayToJson(next.cells()));
        json.append("}");
        return json.toString();
    }

    public void notifyConnectionLost(String reason) {
        System.err.println("Communication error: " + reason);
        if (onConnectionLost != null) {
            Platform.runLater(() -> { if (onConnectionLost != null) onConnectionLost.run(); });
            startAutoReconnect();
        }
    }

    private synchronized void startAutoReconnect() {
        if (retryExec != null && !retryExec.isShutdown()) return;
        retryExec = Executors.newSingleThreadScheduledExecutor();
        retryExec.scheduleAtFixedRate(() -> {
            try {
                if (!client.isConnected()) {
                    boolean ok = client.connect();
                    if (ok) {
                        stopAutoReconnect();
                        Platform.runLater(() -> { if (onReconnected != null) onReconnected.run(); });
                    }
                } else {
                    stopAutoReconnect();
                }
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    private synchronized void stopAutoReconnect() {
        if (retryExec != null) {
            retryExec.shutdownNow();
            retryExec = null;
        }
    }

}
