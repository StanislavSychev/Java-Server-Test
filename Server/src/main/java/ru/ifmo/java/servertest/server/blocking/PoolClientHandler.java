package ru.ifmo.java.servertest.server.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    boolean handleRequest(InputStream input, OutputStream output) throws IOException {
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
