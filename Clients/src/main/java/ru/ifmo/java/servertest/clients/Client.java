package ru.ifmo.java.servertest.clients;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Client implements AutoCloseable {

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public Client(InetAddress address, int port) throws IOException{
        socket = new Socket(address, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    public List<Integer> sort(List<Integer> list) throws IOException {
        TestingProtocol.IntegerListMessage.newBuilder().addAllItem(list).build().writeDelimitedTo(output);
        return TestingProtocol.IntegerListMessage.parseDelimitedFrom(input).getItemList();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
