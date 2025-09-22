package org.oosd.external;


import org.oosd.controller.GameController;

public class ExternalPlayer {
    private final ExternalClient client;
    private final GameController controller;

    //Initialize the external client and controller
    public ExternalPlayer(GameController controller)
    {
        this.client = new ExternalClient();
        this.controller = controller;
    }


    public boolean connectToServer()
    {
        //Set host name and port number to connect to server
        return client.connect("localhost", 3000);
    }

    public void processServerInput()
    {
        if(!client.isConnected())
        {
            return;
        }
        String command = client.readResponse();
        if(command == null)
        {
            return;
        }

        //Reflect the received command from server into game board
        controller.processCommand(command);
    }

    public void sendAction(String action)
    {
        if(client.isConnected())
        {
            client.sendCommand(action);
        }
    }

    public void disconnect()
    {
        client.disconnect();
    }

}
