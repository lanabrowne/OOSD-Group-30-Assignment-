package org.oosd.external;

public class ExternalClientTest {
    public static void main(String[] args) {
        ExternalClient client = new ExternalClient();


        if (client.connect("localhost", 3000)) {
            System.out.println("Connected to server!");


            client.sendCommand("LEFT");
            System.out.println("Sent command: LEFT");


            String response = client.readResponse();
            System.out.println("Server says: " + response);


            client.disconnect();
        } else {
            System.out.println("Could not connect to server.");
        }
    }
}
