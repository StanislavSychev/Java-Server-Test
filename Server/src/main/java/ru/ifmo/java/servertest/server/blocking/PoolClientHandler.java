package ru.ifmo.java.servertest.server.blocking;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PoolClientHandler extends ClientHandler {

    private final ExecutorService sender;
    private final ExecutorService sorter;

    public PoolClientHandler(Socket socket, ExecutorService sorter) {
        super(socket);
        sender = Executors.newSingleThreadExecutor();
        this.sorter = sorter;

    }

    public void stop() {
        sender.shutdown();
    }

    @Override
    boolean handleRequest(DataInputStream input, DataOutputStream output) throws IOException {
        List<Integer> unsorted = getRequest(input);
        if (unsorted == null) {
            return false;
        }
        sorter.submit(() -> {
            List<Integer> sorted = sort(unsorted);
            sender.submit(() -> {
                try {
                    sendResponse(sorted, output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });
        return true;
    }
}
