package org.oosd.external;

import java.io.*;
import java.net.Socket;

/**
 * This class is for socket communication class with Server (Tetris Server jar file)
 */
public class ExternalClient {
    //Set socket
    private Socket socket;
    private PrintWriter writeOut;
    //Use Buffer to read file
    private BufferedReader readIn;
    //Set connected to false as default
    private boolean connected = false;

    /**
     * By using host name and port number, connect to server
     * @param host --> Host name to connect with
     * @param port --> Port number to connect to server
     * @return
     */
    public boolean connect(String host, int port)
    {
        try{
            socket = new Socket(host, port);
            writeOut = new PrintWriter(socket.getOutputStream(), true);
            readIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("Connected to Server: " + host + ": " + port);
            return true;
        } catch (IOException e)
        {
            System.err.println("Connection Failed: " + e.getMessage());
            connected = false;
            return false;
        }

    }

    public void sendCommand(String command)
    {
        if(connected && writeOut != null)
        {
            writeOut.println(command);
        }
    }

    public String readResponse()
    {
        try
        {
            if(connected && readIn != null)
            {
                return readIn.readLine();
            }
        } catch (IOException e)
        {
            System.err.println("Reading error from server: " + e.getMessage());

        }
        return null;
    }
    public void disconnect()
    {
        try
        {
            if(socket != null)
            {
                socket.close();
            }
            connected = false;
            System.out.println("Disconnected from server.");
        }catch(IOException e)
        {
            System.err.println("Closing error: " + e.getMessage());
        }
    }

    public boolean isConnected()
    {
        return connected;
    }
}









