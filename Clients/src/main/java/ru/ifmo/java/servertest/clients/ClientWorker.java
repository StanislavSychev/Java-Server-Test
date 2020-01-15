package ru.ifmo.java.servertest.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClientWorker implements Runnable {

    private final int n;
    private final int delta;
    private final int x;
    private final InetAddress address;
    private final int port;
    private long requestTime;

    public ClientWorker(int n, int delta, int x, InetAddress address, int port) {
        this.n = n;
        this.delta = delta;
        this.x = x;
        this.address = address;
        this.port = port;
    }

    public double getAverageRequestTime() {
        return ((double) requestTime) / x;
    }

    @Override
    public void run() {
        try (Client client = new Client(InetAddress.getLocalHost(), 8082)) {
            Random random = new Random();
            for (int i = 0; i < x; i++) {
                long tic = System.currentTimeMillis();
                List<Integer> list = client.sort(
                        random
                                .ints(n)
                                .boxed()
                                .collect(Collectors.toList())
                );
                System.out.println(i);
                requestTime += System.currentTimeMillis() - tic;
                Thread.sleep(delta);
            }
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }
    }
}
