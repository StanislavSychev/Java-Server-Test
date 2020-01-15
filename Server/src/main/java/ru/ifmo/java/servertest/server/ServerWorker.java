package ru.ifmo.java.servertest.server;

import java.io.IOException;

public interface ServerWorker extends Runnable {

    public void stop() throws IOException;

    public double getAverageFullTime();

    public double getAverageSortTime();

    public int getPort();
}
