package ru.ifmo.servertest.gui;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

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

    public static class TestResult {
        private final List<Integer> values;
        private final List<StatResult> results;

        public TestResult(List<Integer> values, List<StatResult> results) {
            this.results = results;
            this.values = values;
        }

        public List<Integer> getValues() {
            return values;
        }

        public List<StatResult> getResults() {
            return results;
        }
    }

    public static class StatResult{
        private final double fullTime;
        private final double sortTime;
        private final double clientTime;

        public StatResult(double fullTime, double sortTime, double clientTime) {
            this.fullTime = fullTime;
            this.sortTime = sortTime;
            this.clientTime = clientTime;
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
                    "fullTime=" + fullTime +
                    ", sortTime=" + sortTime +
                    ", clientTime=" + clientTime +
                    '}';
        }
    }

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

    public StatResult runTest(int n, int m, int delta, int x, TestingProtocol.ServerType type) throws IOException {
        int port = startServer(inputServer, outputServer, type);
        double clientTime = getClientStat(inputClient, outputClient, port, n, m, delta, x);
        ServerStats serverStats = stopServer(inputServer, outputServer);
        System.out.println(serverStats.sortTime);
        System.out.println(serverStats.fullTime);
        System.out.println(clientTime);
        return new StatResult(serverStats.fullTime, serverStats.sortTime, clientTime);
    }

    public TestResult runTests(TestParams params, ProgressPane progressPane) {
        progressPane.setMax(params.getMax());
        progressPane.setMin(params.getMin());
        List<StatResult> results = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int value = params.getMin(); value < params.getMax(); value += params.getStep()) {
            if (progressPane.isCanceled()) {
                return null;
            }
            TestParams.Param toChange = params.getToChange();
            if (toChange == TestParams.Param.M) {
                try {
                    results.add(runTest(params.getN(), value, params.getDelta(), params.getX(), params.getType()));
                    values.add(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (toChange == TestParams.Param.N) {
                try {
                    results.add(runTest(value, params.getM(), params.getDelta(), params.getX(), params.getType()));
                    values.add(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    results.add(runTest(params.getN(), params.getM(), value, params.getX(), params.getType()));
                    values.add(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            progressPane.update(value);
        }
        return new TestResult(values, results);
    }

    public static void main(String[] args) throws IOException {
        TestRunner runner = new TestRunner("localhost", "localhost");
        runner.runTest(10, 10, 1000, 10, TestingProtocol.ServerType.BLOCKINGTHREAD);
    }
}
