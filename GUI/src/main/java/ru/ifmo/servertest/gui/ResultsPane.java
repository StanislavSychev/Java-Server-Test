package ru.ifmo.servertest.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ResultsPane extends JFrame {

    private final ChartPanel chartPanel;
    private final JButton runTest;
    private final JButton save;
    private final JPanel panel;
    private final TestRunner testRunner;
    private final XYSeriesCollection data;
    private final ParametersPane parametersPane;
    private TestParams lastParams;
    private List<TestRunner.StatResult> lastResults;

    private class ResultsTask extends SwingWorker<List<TestRunner.StatResult>, Object> {

        final ProgressMonitor progressMonitor;
        final TestParams params;


        ResultsTask(ProgressMonitor progressMonitor, TestParams params) {
            this.progressMonitor = progressMonitor;
            this.params = params;
        }

        @Override
        protected List<TestRunner.StatResult> doInBackground() throws Exception {
            final List<TestRunner.StatResult> results;
            try {
                results = testRunner.runTests(params, progressMonitor);
            } catch (IOException | TestRunner.TestingException e) {
                JOptionPane.showMessageDialog(ResultsPane.this, e.getMessage(), "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
            progressMonitor.setNote("test finished");
            progressMonitor.close();
            lastParams = params;
            System.out.println(params);
            lastResults = results;
            return results;
        }

        @Override
        protected void done() {
            try {
                List<TestRunner.StatResult> results = get();
                XYSeries fullTime = new XYSeries("server time");
                XYSeries clientTime = new XYSeries("client time");
                XYSeries sortTime = new XYSeries("sort time");
                if (results == null) {
                    return;
                }
                results.forEach(i -> fullTime.add(i.getValue(), i.getFullTime()));
                results.forEach(i -> clientTime.add(i.getValue(), i.getClientTime()));
                results.forEach(i -> sortTime.add(i.getValue(), i.getSortTime()));
                data.removeAllSeries();
                data.addSeries(clientTime);
                data.addSeries(fullTime);
                data.addSeries(sortTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void getResults() {
        TestParams params;
        while (true) {
            try {
                params = parametersPane.getParams();
                if (params == null) {
                    return;
                }
                break;
            } catch (TestParams.ParameterException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Running test", "Starting", params.getMin(), params.getMax());
        final List<TestRunner.StatResult> results;
        ResultsTask task = new ResultsTask(progressMonitor, params);
        progressMonitor.setMillisToDecideToPopup(10);
        progressMonitor.setMillisToPopup(10);
        task.execute();

//        try {
//            results = testRunner.runTests(params, progressMonitor);
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(this, e.getMessage(), "Warning",
//                    JOptionPane.WARNING_MESSAGE);
//            return null;
//        }
//        progressMonitor.setNote("test finished");
//        progressMonitor.close();
//        lastParams = params;
//        System.out.println(params);
//        lastResults = results;
//        return results;
    }

    public ResultsPane(TestRunner testRunner) {
        parametersPane = new ParametersPane(this);
        this.testRunner = testRunner;
        panel = new JPanel();
        data = new XYSeriesCollection();
        JFreeChart jFreeChart = ChartFactory.createXYLineChart("test", "X", "Y", data);
        chartPanel = new ChartPanel(jFreeChart);
        panel.add(chartPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        runTest = new JButton("run test");
        runTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getResults();
//                List<TestRunner.StatResult> results = getResults();
//                XYSeries fullTime = new XYSeries("server time");
//                XYSeries clientTime = new XYSeries("client time");
//                XYSeries sortTime = new XYSeries("sort time");
//                if (results == null) {
//                    return;
//                }
//                results.forEach(i -> fullTime.add(i.getValue(), i.getFullTime()));
//                results.forEach(i -> clientTime.add(i.getValue(), i.getClientTime()));
//                results.forEach(i -> sortTime.add(i.getValue(), i.getSortTime()));
//                data.removeAllSeries();
//                data.addSeries(clientTime);
//                data.addSeries(fullTime);
//                data.addSeries(sortTime);
            }
        });
        save = new JButton("save");
        panel.add(runTest);
        panel.add(save);
        add(panel);
        pack();
        setResizable(false);
    }

}
