package ru.ifmo.java.servertest.clients;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

public class Client implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    public Client(InetAddress address, int port) throws IOException{
        socket = new Socket(address, port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public List<Integer> sort(List<Integer> list) throws IOException {
        System.out.println(list);
        TestingProtocol.IntegerListMessage request = TestingProtocol.IntegerListMessage.newBuilder().addAllItem(list).build();
        output.writeInt(request.getSerializedSize());
        //output.write(ByteBuffer.allocate(4).putInt(request.getSerializedSize()).array());
        request.writeTo(output);
        int size = input.readInt();
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        return TestingProtocol.IntegerListMessage.parseFrom(bytes).getItemList();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
