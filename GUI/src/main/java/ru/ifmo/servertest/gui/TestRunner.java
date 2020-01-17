package ru.ifmo.servertest.gui;

import ru.ifmo.java.servertest.Constants;
import ru.ifmo.java.servertest.protocol.TestingProtocol;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TestRunner implements AutoCloseable {
    private final String clientAddress;
    private final String serverAddress;
    private final Socket socketServer;
    private final Socket socketClient;
    private final InputStream inputServer;
    private final InputStream inputClient;
    private final OutputStream outputServer;
    private final OutputStream outputClient;

    public static class TestingException extends Exception {

        private final String message;

        public TestingException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public static class StatResult{
        private final double fullTime;
        private final double sortTime;
        private final double clientTime;
        private final int value;

        public StatResult(int value, double fullTime, double sortTime, double clientTime) {
            this.value = value;
            this.fullTime = fullTime;
            this.sortTime = sortTime;
            this.clientTime = clientTime;
        }

        public int getValue() {
            return value;
        }

        public double getFullTime() {
            return fullTime;
        }

        public double getSortTime() {
            return sortTime;
        }

        public double getClientTime() {
            return clientTime;
        }

        @Override
        public String toString() {
            return "StatResult{" +
                    "value=" + value +
                    "fullTime=" + fullTime +
                    ", sortTime=" + sortTime +
                    ", clientTime=" + clientTime +
                    '}';
        }
    }

    public TestRunner(String clientAddress, String serverAddress) throws IOException {
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;
        socketServer = new Socket(InetAddress.getByName(serverAddress), Constants.SERVER_PORT);
        socketClient = new Socket(InetAddress.getByName(clientAddress), Constants.CLIENT_PORT);
        inputServer = socketServer.getInputStream();
        inputClient = socketClient.getInputStream();
        outputServer = socketServer.getOutputStream();
        outputClient = socketClient.getOutputStream();
    }

    public TestRunner(AddressPair addressPair) throws IOException {
        this(addressPair.getClientAddress(), addressPair.getServerAddress());
    }

    private int startServer(InputStream input, OutputStream output, TestingProtocol.ServerType type) throws IOException, TestingException {
        TestingProtocol.ServerStartRequest.newBuilder().setType(type).build().writeDelimitedTo(output);
        TestingProtocol.ServerStartResponse response = TestingProtocol.ServerStartResponse.parseDelimitedFrom(input);
        if (response == null) {
            throw new TestingException("server not responding");
        }
        return response.getPort();
    }

    private double getClientStat(InputStream input, OutputStream output, int port, int n, int m, int delta, int x) throws IOException, TestingException {
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
        TestingProtocol.ClientResponse response = TestingProtocol.ClientResponse.parseDelimitedFrom(input);
        if (response == null) {
            throw new TestingException("client not responding");
        }
        return response.getClientTime();
    }

    @Override
    public void close() throws IOException {
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

    private ServerStats stopServer(InputStream input, OutputStream output) throws IOException, TestingException {
        TestingProtocol.ServerStopRequest.newBuilder().build().writeDelimitedTo(output);
        TestingProtocol.ServerStopResponse response = TestingProtocol.ServerStopResponse.parseDelimitedFrom(input);
        if (response == null) {
            throw new TestingException("server not responding");
        }
        return new ServerStats(response.getFullTime(), response.getSortTime());
    }

    public StatResult runTest(int n, int m, int delta, int x, TestingProtocol.ServerType type, TestParams.Param toChange, int value) throws IOException, TestingException {
        int port = startServer(inputServer, outputServer, type);
        double clientTime;
        if (toChange == TestParams.Param.M) {
            clientTime = getClientStat(inputClient, outputClient, port, n, value, delta, x);
        } else if (toChange == TestParams.Param.N) {
            clientTime = getClientStat(inputClient, outputClient, port, value, m, delta, x);
        } else {
            clientTime = getClientStat(inputClient, outputClient, port, n, m, value, x);
        }
        ServerStats serverStats = stopServer(inputServer, outputServer);
        return new StatResult(value, serverStats.fullTime, serverStats.sortTime, clientTime);
    }

    public List<StatResult> runTests(TestParams params, ProgressMonitor progressMonitor) throws IOException, TestingException {
        List<StatResult> results = new ArrayList<>();
        for (int value = params.getMin(); value < params.getMax(); value += params.getStep()) {
            if (progressMonitor.isCanceled()) {
                return null;
            }

            results.add(runTest(params.getN(), params.getM(), params.getDelta(), params.getX(), params.getType(), params.getToChange(), value));

            progressMonitor.setNote("running on value " + value + "/" + params.getMax());
            progressMonitor.setProgress(value);
        }
        return results;
    }

    public static void main(String[] args) throws IOException, TestingException {
        TestRunner runner = new TestRunner("localhost", "localhost");
        runner.runTest(10, 10, 1000, 10, TestingProtocol.ServerType.BLOCKINGTHREAD, TestParams.Param.M, 10);
        runner.close();
    }
}
