package ru.ifmo.java.servertest.server.blocking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker implements Runnable {

    private final ServerSocket serverSocket;
    private volatile boolean alive = true;
    private List<Thread> threads = new ArrayList<>();
    private List<ClientHandler> handlers = new ArrayList<>();
    private final ExecutorService sorter;
    private final boolean usePool;

    public ServerWorker(boolean usePool) throws IOException {
        serverSocket = new ServerSocket(8081);
        this.usePool = usePool;
        if (usePool) {
            sorter = Executors.newFixedThreadPool(4);
        } else {
            sorter = null;
        }
    }

    @Override
    public void run() {
        while (alive) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("client(");
                ClientHandler handler;
                if (usePool) {
                    handler = new PoolClientHandler(socket, sorter);
                } else {
                    handler = new ThreadClientHandler(socket);
                }
                Thread thread = new Thread(handler);
                thread.start();
                threads.add(thread);
                handlers.add(handler);
            }catch (IOException e) {

            }
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        double averageFullTime = handlers.stream().mapToDouble(ClientHandler::getAverageFullTime).average().orElse(0);
        double averageSortTime = handlers.stream().mapToDouble(ClientHandler::getAverageSortTime).average().orElse(0);
        System.out.println(averageSortTime);
        System.out.println(averageFullTime);
    }

    public void stop() throws IOException {
        serverSocket.close();
        alive = false;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerWorker worker = new ServerWorker(true);
        Thread thread = new Thread(worker);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String comand = scanner.nextLine();
            if (comand.equals("exit")) {
                worker.stop();
                break;
            }
        }
        thread.join();
    }
}
