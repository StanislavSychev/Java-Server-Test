package ru.ifmo.java.servertest.server;

import ru.ifmo.java.servertest.protocol.TestingProtocol;
import ru.ifmo.java.servertest.server.blocking.BlockingServerWorker;
import ru.ifmo.java.servertest.server.nonblocking.NonBlockingServerWorker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerApp implements Runnable {

    private final ServerSocket serverSocket;
    private InputStream input;
    private OutputStream output;
    private volatile boolean alive = true;

    public ServerApp() throws IOException {
        serverSocket = new ServerSocket(8080);
    }

    private ServerWorker makeServer() throws IOException {
        TestingProtocol.ServerStartRequest request = TestingProtocol.ServerStartRequest.parseDelimitedFrom(input);
        if (request == null) {
            return null;
        }
        TestingProtocol.ServerType type = request.getType();
        if (type == TestingProtocol.ServerType.BLOCKINGTHREAD) {
            return new BlockingServerWorker(false);
        } else if (type == TestingProtocol.ServerType.BLOCKINGPOOL) {
            return new BlockingServerWorker(true);
        } else {
            return new NonBlockingServerWorker();
        }
    }

    private void sendPort(int port) throws IOException {
        TestingProtocol.ServerStartResponse.newBuilder().setPort(port).build().writeDelimitedTo(output);
    }

    private void getStopRequest() throws IOException {
        TestingProtocol.ServerStopRequest.parseDelimitedFrom(input);
    }

    private void sendStats(double fullTime, double sortTime) throws IOException {
        TestingProtocol.ServerStopResponse.newBuilder().setFullTime(fullTime).setSortTime(sortTime).build().writeDelimitedTo(output);
    }

    @Override
    public void run() {
        while (alive) {
            try (
                    Socket socket = serverSocket.accept();
            ) {
                input = socket.getInputStream();
                output = socket.getOutputStream();
                while (alive) {
                    ServerWorker serverWorker = makeServer();
                    if (serverWorker == null) {
                        break;
                    }
                    Thread server = new Thread(serverWorker);
                    server.start();
                    try {
                        sendPort(serverWorker.getPort());
                        getStopRequest();
                        serverWorker.stop();
                    } catch (IOException ex) {

                    } finally {
                        try {
                            server.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendStats(serverWorker.getAverageFullTime(), serverWorker.getAverageSortTime());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        alive = false;
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerApp serverApp = new ServerApp();
        Thread thread = new Thread(serverApp);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String comand = scanner.nextLine();
            if (comand.equals("exit")) {
                serverApp.stop();
                break;
            }
        }
        thread.join();
    }
}
