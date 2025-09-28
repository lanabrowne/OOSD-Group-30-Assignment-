package org.oosd.external;

import java.io.*;
import java.net.Socket;

/**
 * This class is for socket communication class with Server (Tetris Server jar file)
 * This class is like at front window of network communication (Send-receive operation with server)
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
     * @parameter--> Host name to connect with
     * @param  --> Port number to connect to server
     * @return
     */
    public boolean connect()
    {
        try{
            socket = new Socket("localhost", 3000);
            writeOut = new PrintWriter(socket.getOutputStream(), true);
            readIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Start receiving thread
            //Call thread command and create anonymouse thread and run the thread
            //And use the lamda to implement Runable interface
            Thread listener = new Thread(() -> {
                try{
                    String line;
                    //Read string massages line one by one
                    while((line = readIn.readLine()) != null)
                    {
                        //Store the collected method in the handle method
                        //ex) If LEFT, RIGHT msg received, send to controller
                        //or operation class and operate into UI
                        handle(line);
                    }
                }catch(IOException e)
                {
                    System.out.println("Connection lost: " + e.getMessage());
                }
            });


            listener.start();


            connected = true;
            System.out.println("Connected to Server: " + ": " );
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

    /**
     * This method is collecting actions and board data from server
     * and reflect to External Player mode
     * @param msg
     */
    public void handle(String msg)
    {
        System.out.println("Received msg: " + msg);
    }

    public boolean isConnected()
    {
        return connected;
    }
}









