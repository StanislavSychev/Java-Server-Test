package ru.ifmo.java.servertest.clients;

import ru.ifmo.java.servertest.Constants;
import ru.ifmo.java.servertest.protocol.TestingProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp implements Runnable {

    private final ServerSocket serverSocket;
    private final ClientManager manager;
    private InputStream input;
    private OutputStream output;
    private volatile boolean alive = true;

    public ClientApp() throws IOException {
        serverSocket = new ServerSocket(Constants.CLIENT_PORT);
        manager = new ClientManager();
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
                    TestingProtocol.ClientRequest request = TestingProtocol.ClientRequest.parseDelimitedFrom(input);
                    InetAddress address;
                    if (request == null) {
                        break;
                    }
                    if (!request.getIsLocalClient() && request.getIp().equals("localhost")) {
                        address = socket.getInetAddress();
                    } else {
                        address = InetAddress.getByName(request.getIp());
                    }
                    double clientTime = manager.runTest(
                            address, request.getPort(),
                            request.getN(), request.getM(),
                            request.getDelta(), request.getX()
                    );
                    TestingProtocol.ClientResponse.newBuilder().setClientTime(clientTime).build().writeDelimitedTo(output);
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
        ClientApp clientApp = new ClientApp();
        Thread thread = new Thread(clientApp);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String comand = scanner.nextLine();
            if (comand.equals("exit")) {
                clientApp.stop();
                break;
            }
        }
        thread.join();
    }
}
