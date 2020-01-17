package ru.ifmo.java.servertest.server.blocking;

import ru.ifmo.java.servertest.server.ServerWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingServerWorker implements ServerWorker {

    private final ServerSocket serverSocket;
    private volatile boolean alive = true;
    private List<Thread> threads = new ArrayList<>();
    private List<ClientHandler> handlers = new ArrayList<>();
    private final ExecutorService sorter;
    private final boolean usePool;

    public BlockingServerWorker(boolean usePool) throws IOException {
        serverSocket = new ServerSocket(0);
        this.usePool = usePool;
        if (usePool) {
            sorter = Executors.newFixedThreadPool(8);
        } else {
            sorter = null;
        }
    }

    @Override
    public void run() {
        while (alive) {
            try {
                Socket socket = serverSocket.accept();
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
    }

    @Override
    public void stop() throws IOException {
        serverSocket.close();
        alive = false;
        if (usePool) {
            sorter.shutdown();
            for (ClientHandler handler : handlers) {
                PoolClientHandler poolClientHandler = (PoolClientHandler) handler;
                poolClientHandler.stop();
            }
        }
    }

    @Override
    public double getAverageFullTime() {
        return handlers.stream().mapToDouble(ClientHandler::getAverageFullTime).average().orElse(0);
    }

    @Override
    public double getAverageSortTime() {
        return handlers.stream().mapToDouble(ClientHandler::getAverageSortTime).average().orElse(0);
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingServerWorker worker = new BlockingServerWorker(true);
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
