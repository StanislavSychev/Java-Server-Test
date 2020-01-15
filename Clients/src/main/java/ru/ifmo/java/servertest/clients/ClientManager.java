package ru.ifmo.java.servertest.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ClientManager {

    private final ExecutorService pool = Executors.newCachedThreadPool();

    public double runTest(InetAddress address, int port, int n, int m, int delta, int x) {
        List<ClientWorker> clientWorkers = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            clientWorkers.add(new ClientWorker(n, delta, x, address, port));
        }
        List<Future<?>> futures = clientWorkers.stream().map(pool::submit).collect(Collectors.toList());
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return clientWorkers.stream().mapToDouble(ClientWorker::getAverageRequestTime).average().orElse(0);
    }

    public static void main(String[] args) throws UnknownHostException {
        ClientManager manager = new ClientManager();
        System.out.println(manager.runTest(InetAddress.getLocalHost(), 8082, 10, 10, 1000, 10));
    }
}
