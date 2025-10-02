package org.oosd.external;


import org.oosd.controller.GameController;
import org.oosd.controller.TwoPlayerController;

public class ExternalPlayer {
    private final ExternalClient client;
    private final TwoPlayerController controller;

    //Set boolean to identify which screen the server
    //tries to send action to
    //Ex: if isLeft false == right screen, True == left screen
    private final boolean isLeft;

    //Initialize the external client and controller
    public ExternalPlayer(TwoPlayerController controller, boolean isLeft)
    {
        this.client = new ExternalClient();
        this.controller = controller;
        this.isLeft = isLeft;
        this.client.setPlayer(this);
    }


    /**
     * This method is connecting to server
     * @return
     */
    public boolean connectToServer()
    {
        //Set host name and port number to connect to server
        return client.connect();
    }

    public boolean isConnected() {
        return client.isConnected();
    }
    public void processServerInput()
    {
        if(!client.isConnected())
        {
            return;
        }
        //read command by one line from server
        String command = client.readResponse();
        //if command is null, do not make any action
        if(command == null)
        {
            return;
        }

        //Reflect the received command from server into game board
        controller.processCommand(command, isLeft);
    }

    //send my action to server (This one will be used only network connection for p vs p)
    public void sendAction(String action)
    {
        if(client.isConnected())
        {
            client.sendCommand(action);
        }
    }



    //Shut down from server
    public void disconnect()
    {
        client.disconnect();
    }

}
