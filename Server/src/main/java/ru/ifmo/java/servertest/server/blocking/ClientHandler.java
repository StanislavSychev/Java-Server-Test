package ru.ifmo.java.servertest.server.blocking;

import ru.ifmo.java.servertest.protocol.TestingProtocol;
import ru.ifmo.java.servertest.server.Sorter;

import java.io.*;
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

    protected List<Integer> getRequest(DataInputStream input) throws IOException {
        int messageSize = input.readInt();
        fullTic = System.currentTimeMillis();
        byte[] bytes = new byte[messageSize];
        input.readFully(bytes);
        TestingProtocol.IntegerListMessage request = TestingProtocol.IntegerListMessage.parseFrom(bytes);
        if (request == null) {
            return null;
        }
        return request.getItemList();
    }

    protected void sendResponse(List<Integer> sorted, DataOutputStream output) throws IOException {
        TestingProtocol.IntegerListMessage response = TestingProtocol.IntegerListMessage
                .newBuilder()
                .addAllItem(sorted)
                .build();
        output.writeInt(response.getSerializedSize());
        response.writeTo(output);
        fullTime += System.currentTimeMillis() - fullTic;
        x++;
    }

    protected List<Integer> sort(List<Integer> unsorted) {
        long sortTic = System.currentTimeMillis();
        List<Integer> sorted = Sorter.sort(unsorted);
        sortTime += System.currentTimeMillis() - sortTic;
        return sorted;
    }

    abstract boolean handleRequest(DataInputStream input, DataOutputStream output) throws IOException;

    @Override
    public void run() {
        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
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
