package org.oosd.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Handles socket communication with the TetrisServer.
 * Responsible for sending PureGame JSON and receiving OpMove JSON.
 */
public class ExternalClient {
    private ExternalPlayer player;
    private boolean connected = false;

    private Runnable onConnectionError;

    public void setOnConnectionError(Runnable handler) {
        this.onConnectionError = handler;
    }

    public void setPlayer(ExternalPlayer player) {
        this.player = player;
    }

    /**
     * One-time request: send the current game state and get optimal move.
     * Each call opens and closes the socket.
     */
    public OpMove requestMove(String jsonGame) {
        ObjectMapper mapper = new ObjectMapper();
        try (
                Socket socket = new Socket("localhost", 3000);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8),
                        true // autoFlush on println
                );
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
                )
        ) {

            out.println(jsonGame);
            out.flush();


            String response = in.readLine();
            if (response == null) {
                System.err.println("Server returned null response. Using default move.");
                return new OpMove(0, 0);
            }

            OpMove move = mapper.readValue(response, OpMove.class);
            System.out.println("Received from server: " + response);
            return move;

        } catch (IOException e) {
            System.out.println("Communication error: " + e.getMessage());
            if (player != null) {
                player.notifyConnectionLost(e.getMessage());
            }
            return null;
        }
    }

    /** For two-way live modes (optional) */
    public boolean connect() {
        try {
            Socket socket = new Socket("localhost", 3000);
            connected = true;
            System.out.println("Connected to Tetris Server.");
            socket.close();
            return true;
        } catch (Exception ex) {
            notifyConnectionError(ex.getMessage());
            System.err.println("Connection failed: " + ex.getMessage());
            connected = false;
            return false;
        }
    }

    private void notifyConnectionError(String detailMessage) {

        if (onConnectionError != null) {
            Platform.runLater(onConnectionError);
            return;
        }


        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Server Connection Error");
            alert.setHeaderText("Server connection was cut");

            alert.setContentText("Please run the server again ");
            alert.showAndWait();
        });
    }

    public boolean isConnected() {
        return connected;
    }
}










