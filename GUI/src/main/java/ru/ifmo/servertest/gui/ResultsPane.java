package ru.ifmo.servertest.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.ifmo.java.servertest.protocol.TestingProtocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
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
    private final JFreeChart jFreeChart;
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
            lastResults = results;
            return results;
        }

        private String getServerName() {
            if (params.getType() == TestingProtocol.ServerType.BLOCKINGTHREAD) {
                return "One Thread Blocking";
            } if (params.getType() == TestingProtocol.ServerType.BLOCKINGPOOL) {
                return "Shared Pool Blocking";
            } else {
                return "Non-Blocking";
            }
        }

        private String getParams() {
            if (params.getToChange() == TestParams.Param.M) {
                return "N=" + params.getN() +  " \u0394=" + params.getDelta() + " X=" + params.getX();
            } else if (params.getToChange() == TestParams.Param.N) {
                return "M=" + params.getM() +  " \u0394=" + params.getDelta() + " X=" + params.getX();
            } else {
                return "N=" + params.getN() + " M=" + params.getM()  + " X=" + params.getX();
            }
        }

        private String getChangedName() {
            if (params.getToChange() == TestParams.Param.M) {
                return "M";
            } else if (params.getToChange() == TestParams.Param.N) {
                return "N";
            } else {
                return "\u0394";
            }
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
                jFreeChart.getXYPlot().getDomainAxis().setLabel(getChangedName());

                jFreeChart.setTitle("Server Type : " + getServerName() + ", Parameters: " + getParams());
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
    }

    private String toFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lastParams);
        stringBuilder.append("\n");
        lastResults.forEach(i -> {
            stringBuilder.append(i);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    public ResultsPane(TestRunner testRunner) {
        parametersPane = new ParametersPane(this);
        this.testRunner = testRunner;
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        data = new XYSeriesCollection();
        jFreeChart = ChartFactory.createXYLineChart("No tests yet", "Value", "average time, ms", data);
        chartPanel = new ChartPanel(jFreeChart);
        panel.add(chartPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        runTest = new JButton("run test");
        runTest.addActionListener(e -> getResults());
        save = new JButton("save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileSave = new JFileChooser();
                int ret = fileSave.showSaveDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    try (FileWriter fileWriter = new FileWriter(fileSave.getSelectedFile()+".txt")) {
                        fileWriter.write(toFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(panel, "Couldn't save to file\n" + ex.getMessage(), "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        JPanel buttons= new JPanel();

        buttons.add(runTest);
        buttons.add(save);
        panel.add(buttons);
        add(panel);
        pack();
        setResizable(false);
    }

}
