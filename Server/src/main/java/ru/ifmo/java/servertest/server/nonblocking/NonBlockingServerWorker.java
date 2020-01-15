package ru.ifmo.java.servertest.server.nonblocking;

import ru.ifmo.java.servertest.server.ServerWorker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServerWorker implements ServerWorker {

    private static class ChanelAttachmentPair {
        final SocketChannel channel;
        final ClientAttachment client;

        ChanelAttachmentPair(SocketChannel channel, ClientAttachment client) {
            this.channel = channel;
            this.client = client;
        }
    }

    private final ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress selfAddreas;
    private final Selector selectorRead;
    private final Selector selectorWrite;
    private final Queue<ChanelAttachmentPair> queueRead;
    private final Queue<ChanelAttachmentPair> queueWrite;
    private volatile boolean alive = true;
    private final ExecutorService sorter;
    private final Thread reader;
    private final Thread writer;
    private List<ClientAttachment> attachments = new ArrayList<>();

    public NonBlockingServerWorker() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        selfAddreas = new InetSocketAddress(8082);
        serverSocketChannel.bind(selfAddreas);
        selectorRead = Selector.open();
        selectorWrite = Selector.open();
        queueRead = new ConcurrentLinkedQueue<>();
        queueWrite = new ConcurrentLinkedQueue<>();
        sorter = Executors.newFixedThreadPool(4);
        reader =  new Thread(() -> {
            try {
                while (alive) {
                    while (!queueRead.isEmpty()) {
                        ChanelAttachmentPair pair = queueRead.poll();;
                        SocketChannel socketChannel = pair.channel;
                        ClientAttachment client = pair.client;
                        try {
                            socketChannel.register(selectorRead, SelectionKey.OP_READ, client);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                    }
                    int read = selectorRead.select();
                    if (read == 0) continue;
                    Iterator<SelectionKey> keys = selectorRead.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ClientAttachment client = (ClientAttachment) key.attachment();

                        if (!client.read(socketChannel)) {
                            key.cancel();
                            keys.remove();
                            continue;
                        }
                        final List<Integer> unsorted = client.getRequest();
                        if (unsorted != null) {
                            client.clear();
                            sorter.submit(() -> {
                                List<Integer> sorted = client.sort(unsorted);
                                client.sendResponse(sorted);
                                queueWrite.add(new ChanelAttachmentPair(socketChannel, client));
                                selectorWrite.wakeup();
                            });
                        }
                        keys.remove();
                    }
                }
            } catch (IOException e) {

            }
        });
        writer = new Thread(() -> {
            try {
                while (alive) {
                    while (!queueWrite.isEmpty()) {
                        ChanelAttachmentPair pair = queueWrite.poll();
                        SocketChannel socketChannel = pair.channel;
                        ClientAttachment client = pair.client;
                        try {
                            socketChannel.register(selectorWrite, SelectionKey.OP_WRITE, client);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                    }
                    if (selectorWrite.select() == 0) continue;
                    Iterator<SelectionKey> keys = selectorWrite.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ClientAttachment client = (ClientAttachment) key.attachment();
                        if (client.write(socketChannel)) {
                            key.cancel();
                        }
                        keys.remove();
                    }

                }
            } catch (IOException e) {

            }
        });
        reader.start();
        writer.start();
    }

    @Override
    public void run() {
        while (alive) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                ClientAttachment clientAttachment = new ClientAttachment();
                attachments.add(clientAttachment);
                queueRead.add(new ChanelAttachmentPair(socketChannel, clientAttachment));
                selectorRead.wakeup();
                System.out.println("client(");
            } catch (IOException e) {

            }
        }
        try {
            reader.join();
            writer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(getAverageFullTime());
        System.out.println(getAverageSortTime());
    }

    @Override
    public void stop() throws IOException {
        alive = false;
        serverSocketChannel.close();
        selectorRead.close();
        selectorWrite.close();
        sorter.shutdown();
    }

    @Override
    public double getAverageFullTime() {
        return  attachments.stream().mapToDouble(ClientAttachment::getAverageFullTime).average().orElse(0);
    }

    @Override
    public double getAverageSortTime() {
        return attachments.stream().mapToDouble(ClientAttachment::getAverageSortTime).average().orElse(0);
    }

    @Override
    public int getPort() {
        return selfAddreas.getPort();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NonBlockingServerWorker worker = new NonBlockingServerWorker();
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
