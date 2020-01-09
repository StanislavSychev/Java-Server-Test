package ru.ifmo.java.servertest.server.blocking;

import ru.ifmo.java.servertest.protocol.TestingProtocol;
import ru.ifmo.java.servertest.server.Sorter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public abstract class ClientHandler implements Runnable {

    protected final Socket socket;
    protected int x;
    protected volatile long fullTime;
    protected volatile long sortTime;
    protected volatile long fullTic;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public double getAverageFullTime() {
        return ((double) fullTime) / x;
    }

    public double getAverageSortTime() {
        return ((double) sortTime) / x;
    }

    protected List<Integer> getRequest(InputStream input) throws IOException {
        TestingProtocol.IntegerListMessage request = TestingProtocol.IntegerListMessage.parseDelimitedFrom(input);
        fullTic = System.currentTimeMillis();
        if (request == null) {
            return null;
        }
        return request.getItemList();
    }

    protected void sendResponse(List<Integer> sorted, OutputStream output) throws IOException {
        TestingProtocol.IntegerListMessage
                .newBuilder()
                .addAllItem(sorted)
                .build()
                .writeDelimitedTo(output);
        fullTime += System.currentTimeMillis() - fullTic;
        x++;
    }

    protected List<Integer> sort(List<Integer> unsorted) {
        long sortTic = System.currentTimeMillis();
        List<Integer> sorted = Sorter.sort(unsorted);
        sortTime += System.currentTimeMillis() - sortTic;
        return sorted;
    }

    abstract boolean handleRequest(InputStream input, OutputStream output) throws IOException;

    @Override
    public void run() {
        try (
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream()
        ) {
            while (true) {
                if (!handleRequest(input, output)) {
                    break;
                }
            }
        } catch (IOException e) {

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
