package ru.ifmo.servertest.gui;

public class AddressPair {
    private final String clientAddress;
    private final String serverAddress;

    public AddressPair(String serverAddress, String clientAddress) {
        if (serverAddress.equals("")) {
            this.serverAddress = "localhost";
        } else {
            this.serverAddress = serverAddress;
        }
        if (clientAddress.equals("")) {
            this.clientAddress = "localhost";
        } else {
            this.clientAddress = serverAddress;
        }
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
