package ru.ifmo.java.servertest.server.nonblocking;

import ru.ifmo.java.servertest.protocol.TestingProtocol;
import ru.ifmo.java.servertest.server.Sorter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;

public class ClientAttachment {

    private ByteBuffer requestSize = ByteBuffer.allocate(4);
    private ByteBuffer responseSize;
    private boolean hasSize = false;
    private boolean writeSize;
    private ByteBuffer request;
    private ByteBuffer response;
    private List<Integer> unsorted = null;
    private int x;
    private volatile long fullTime;
    private volatile long sortTime;
    private volatile long fullTic;

    public ClientAttachment() {

    }



    public synchronized boolean read(SocketChannel channel) throws IOException {
        if (hasSize) {
            int read = channel.read(request);
            if (read == -1) {
                return false;
            }
            if (request.position() == request.limit()) {
                hasSize = false;
                request.flip();
                byte[] bytes = new byte[request.remaining()];
                request.get(bytes);
                unsorted = TestingProtocol.IntegerListMessage.parseFrom(bytes).getItemList();
            }
        } else {
            int read = channel.read(requestSize);
            if (read == -1) {
                return false;
            }
            if (requestSize.position() == requestSize.limit()) {
                hasSize = true;
                requestSize.flip();
                int size = requestSize.getInt();
                request = ByteBuffer.allocate(size);
                fullTic = System.currentTimeMillis();
            }
        }
        return true;
    }

    public synchronized List<Integer> getRequest() {
        return unsorted;
    }

    public void clear() {
        unsorted = null;
        hasSize = false;
        requestSize = ByteBuffer.allocate(4);
    }

    public List<Integer> sort(List<Integer> unsorted) {
        long sortTic = System.currentTimeMillis();
        List<Integer> sorted = Sorter.sort(unsorted);
        sortTime += System.currentTimeMillis() - sortTic;
        return sorted;
    }

    public synchronized void sendResponse(List<Integer> sorted) {
        TestingProtocol.IntegerListMessage message = TestingProtocol.IntegerListMessage.newBuilder().addAllItem(sorted).build();
        writeSize = false;
        responseSize = ByteBuffer.allocate(4);
        int size = message.getSerializedSize();
        responseSize.putInt(size);
        responseSize.flip();
        response = ByteBuffer.allocate(size);
        response.put(message.toByteArray());
        response.flip();
    }

    public synchronized boolean write(SocketChannel chanel) throws IOException {
        if (writeSize) {
            chanel.write(response);
            if (response.position() == response.limit()) {
                fullTime += System.currentTimeMillis() - fullTic;
                x++;
                return true;
            }
        } else {
            chanel.write(responseSize);
            if (responseSize.position() == responseSize.limit()) {
                writeSize = true;
            }
        }
        return false;
    }

    public double getAverageFullTime() {
        return ((double) fullTime) / x;
    }

    public double getAverageSortTime() {
        return ((double) sortTime) / x;
    }

}
