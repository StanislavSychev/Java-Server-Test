package ru.ifmo.servertest.gui;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

public class TestParams {

    public enum Param {
        N, M, DELTA
    }

    public static class ParameterException extends Exception {

        private final String message;

        public ParameterException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    private final Param toChange;
    private final int min;
    private final int max;
    private final int step;
    private final int n;
    private final int m;
    private final int delta;
    private final int x;
    private final TestingProtocol.ServerType type;

    public TestParams(Param param, TestingProtocol.ServerType type, int min, int max, int step, int n, int m, int delta, int x) {
        this.toChange = param;
        this.type = type;
        this.min = min;
        this.max = max;
        this.step = step;
        this.n = n;
        this.m = m;
        this.delta = delta;
        this.x = x;
    }

    public TestParams() {
        this(Param.M, TestingProtocol.ServerType.BLOCKINGTHREAD, 1, 10, 1, 100, 2, 10, 10);
    }

    public TestParams(String paramS, String typeS, String minS, String maxS, String stepS, String nS, String mS, String deltaS, String xS) throws ParameterException {
        if (paramS.equals("N")) {
            toChange = Param.N;
        } else if (paramS.equals("M")) {
            toChange = Param.M;
        } else {
            toChange = Param.DELTA;
        }
        if (typeS.equals("Threads")) {
            type = TestingProtocol.ServerType.BLOCKINGTHREAD;
        } else if (typeS.equals("Pool")) {
            type = TestingProtocol.ServerType.BLOCKINGPOOL;
        } else {
            type = TestingProtocol.ServerType.NONBLOCKING;
        }
        try {
            n = Integer.parseInt(nS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter N should be number");
        }
        try {
            m = Integer.parseInt(mS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter M should be number");
        }
        try {
            delta = Integer.parseInt(deltaS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter Delta should be number");
        }
        try {
            x = Integer.parseInt(xS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter X should be number");
        }
        try {
            min = Integer.parseInt(minS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter min should be number");
        }
        try {
            max = Integer.parseInt(maxS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter max should be number");
        }
        try {
            step = Integer.parseInt(stepS);
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter step should be number");
        }
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }

    public int getDelta() {
        return delta;
    }

    public int getX() {
        return x;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public Param getToChange() {
        return toChange;
    }

    public TestingProtocol.ServerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TestParams{" +
                "toChange=" + toChange +
                ", min=" + min +
                ", max=" + max +
                ", step=" + step +
                ", n=" + n +
                ", m=" + m +
                ", delta=" + delta +
                ", x=" + x +
                ", type=" + type +
                '}';
    }
}
