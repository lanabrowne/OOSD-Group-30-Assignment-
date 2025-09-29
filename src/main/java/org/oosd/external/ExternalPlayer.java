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
        //controller.processCommand(cmd);
    }

    //send my action to server (This one will be used only network connection for p vs p)
    public void sendAction(String action)
    {
        if(client.isConnected())
        {
            client.sendCommand(action);
        }
    }

    /**
     * This method is executing commands which is received from server
     * into game controller.
     * @param cmd
     */
    public void processCommand(String cmd)
    {
        //if the inside of command is empty null, return and show the msg
        if(cmd == null)
        {
            System.out.println("Error of process. Cmd is null.");
            return;
        }

        //Just for checking sending command
        System.out.println("Command sending success. Sending command to controller." +
                "Command: " + cmd);
        controller.processCommand(cmd, isLeft);
    }

    //Shut down from server
    public void disconnect()
    {
        client.disconnect();
    }

}
