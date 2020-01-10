package ru.ifmo.java.servertest.server.blocking;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ThreadClientHandler extends ClientHandler {
    public ThreadClientHandler(Socket socket) {
        super(socket);
    }

    @Override
    boolean handleRequest(DataInputStream input, DataOutputStream output) throws IOException {
        List<Integer> unsorted = getRequest(input);
        if (unsorted == null) {
            return false;
        }
        List<Integer> sorted = sort(unsorted);
        sendResponse(sorted, output);
        return true;
    }
}
