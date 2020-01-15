package ru.ifmo.servertest.gui;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestRunner implements AutoCloseable {
    private final String clientAddress;
    private final String serverAddress;
    private final Socket socketServer;
    private final Socket socketClient;
    private final InputStream inputServer;
    private final InputStream inputClient;
    private final OutputStream outputServer;
    private final OutputStream outputClient;

    public TestRunner(String clientAddress, String serverAddress) throws IOException {
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;
        socketServer = new Socket(InetAddress.getByName(serverAddress), 8080);
        socketClient = new Socket(InetAddress.getByName(clientAddress), 8081);
        inputServer = socketServer.getInputStream();
        inputClient = socketClient.getInputStream();
        outputServer = socketServer.getOutputStream();
        outputClient = socketClient.getOutputStream();
    }

    public TestRunner(AddressPair addressPair) throws IOException {
        this(addressPair.getClientAddress(), addressPair.getServerAddress());
    }

    private int startServer(InputStream input, OutputStream output, TestingProtocol.ServerType type) throws IOException {
        TestingProtocol.ServerStartRequest.newBuilder().setType(type).build().writeDelimitedTo(output);
        return TestingProtocol.ServerStartResponse.parseDelimitedFrom(input).getPort();
    }

    private double getClientStat(InputStream input, OutputStream output, int port, int n, int m, int delta, int x) throws IOException {
        TestingProtocol.ClientRequest.newBuilder()
                .setIp(serverAddress)
                .setIsLocalClient(clientAddress.equals("localhost"))
                .setPort(port)
                .setN(n)
                .setM(m)
                .setDelta(delta)
                .setX(x)
                .build()
                .writeDelimitedTo(output);
        return TestingProtocol.ClientResponse.parseDelimitedFrom(input).getClientTime();
    }

    @Override
    public void close() throws Exception {
        socketServer.close();
        socketClient.close();
    }

    private static class ServerStats {
        private final double fullTime;
        private final double sortTime;

        ServerStats(double fullTime, double sortTime) {
            this.fullTime = fullTime;
            this.sortTime = sortTime;
        }
    }

    private ServerStats stopServer(InputStream input, OutputStream output) throws IOException {
        TestingProtocol.ServerStopRequest.newBuilder().build().writeDelimitedTo(output);
        TestingProtocol.ServerStopResponse response = TestingProtocol.ServerStopResponse.parseDelimitedFrom(input);
        return new ServerStats(response.getFullTime(), response.getSortTime());
    }

    public void runTest(int n, int m, int delta, int x, TestingProtocol.ServerType type) throws IOException {
        int port = startServer(inputServer, outputServer, type);
        double clientTime = getClientStat(inputClient, outputClient, port, n, m, delta, x);
        ServerStats serverStats = stopServer(inputServer, outputServer);
        System.out.println(serverStats.sortTime);
        System.out.println(serverStats.fullTime);
        System.out.println(clientTime);
    }

    public static void main(String[] args) throws IOException {
        TestRunner runner = new TestRunner("localhost", "localhost");
        runner.runTest(10, 10, 1000, 10, TestingProtocol.ServerType.BLOCKINGTHREAD);
    }
}
