package org.oosd.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

/**
 * Handles socket communication with the TetrisServer.
 * Responsible for sending PureGame JSON and receiving OpMove JSON.
 */
public class ExternalClient {
    private ExternalPlayer player;
    private boolean connected = false;

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
            // 1) JSONを1行で送る（必ず println）
            out.println(jsonGame);
            out.flush();

            // 2) 1行の応答を受け取る
            String response = in.readLine();
            if (response == null) {
                System.err.println("⚠️ Server returned null response. Using default move.");
                return new OpMove(0, 0);
            }

            OpMove move = mapper.readValue(response, OpMove.class);
            System.out.println("📩 Received from server: " + response);
            return move;

        } catch (IOException e) {
            System.err.println("💥 Communication error: " + e.getMessage());
            return new OpMove(0, 0);
        }
    }

    /** For two-way live modes (optional) */
    public boolean connect() {
        try {
            Socket socket = new Socket("localhost", 3000);
            connected = true;
            System.out.println("✅ Connected to Tetris Server.");
            socket.close();
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}










