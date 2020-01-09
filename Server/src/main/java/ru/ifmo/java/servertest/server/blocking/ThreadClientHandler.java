package ru.ifmo.java.servertest.server.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ThreadClientHandler extends ClientHandler {
    public ThreadClientHandler(Socket socket) {
        super(socket);
    }

    @Override
    boolean handleRequest(InputStream input, OutputStream output) throws IOException {
        List<Integer> unsorted = getRequest(input);
        if (unsorted == null) {
            return false;
        }
        List<Integer> sorted = sort(unsorted);
        sendResponse(sorted, output);
        return true;
    }
}
