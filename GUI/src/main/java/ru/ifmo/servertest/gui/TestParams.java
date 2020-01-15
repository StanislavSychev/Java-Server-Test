package ru.ifmo.servertest.gui;

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

    public TestParams(Param param, int min, int max, int step, int n, int m, int delta, int x) {
        this.toChange = param;
        this.min = min;
        this.max = max;
        this.step = step;
        this.n = n;
        this.m = m;
        this.delta = delta;
        this.x = x;
    }

    public TestParams(String paramS, String minS, String maxS, String stepS, String nS, String mS, String deltaS, String xS) throws ParameterException {
        if (paramS.equals("N")) {
            toChange = Param.N;
        } else if (paramS.equals("M")) {
            toChange = Param.M;
        } else {
            toChange = Param.DELTA;
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

}
