package ru.ifmo.servertest.gui;

import ru.ifmo.java.servertest.protocol.TestingProtocol;

import javax.swing.*;
import java.awt.*;

public class ParametersPane {

    private final JTextField min = new JTextField(5);
    private final  JTextField max = new JTextField(5);
    private final JTextField step = new JTextField(5);
    private final JTextField m = new JTextField(5);
    private final JTextField n = new JTextField(5);
    private final JTextField delta = new JTextField(5);
    private final JTextField x = new JTextField(5);
    private final JComboBox<String> toChange;
    private final JComboBox<String> type;
    private final JPanel panel;
    private final JFrame parent;

    public ParametersPane(JFrame parent) {
        this(new TestParams(), parent);
    }

    public ParametersPane(TestParams defaultParams, JFrame parent) {
        this.parent = parent;
        String[] params = {"M", "N", "\u0394"};
        String[] types = {"Threads", "Pool", "Non-blocking"};
        toChange = new JComboBox<>(params);
        type = new JComboBox<>(types);
        min.setText(Integer.toString(defaultParams.getMin()));
        max.setText(Integer.toString(defaultParams.getMax()));
        step.setText(Integer.toString(defaultParams.getStep()));
        m.setText(Integer.toString(defaultParams.getM()));
        n.setText(Integer.toString(defaultParams.getN()));
        delta.setText(Integer.toString(defaultParams.getDelta()));
        x.setText(Integer.toString(defaultParams.getX()));
        TestParams.Param defaultToChange = defaultParams.getToChange();
        if (defaultToChange == TestParams.Param.M) {
            toChange.setSelectedIndex(0);
        } else if (defaultToChange == TestParams.Param.N) {
            toChange.setSelectedIndex(1);
        } else {
            toChange.setSelectedIndex(2);
        }
        TestingProtocol.ServerType defaultType = defaultParams.getType();
        if (defaultType == TestingProtocol.ServerType.BLOCKINGTHREAD) {
            toChange.setSelectedIndex(0);
        } else if (defaultType == TestingProtocol.ServerType.BLOCKINGPOOL) {
            toChange.setSelectedIndex(1);
        } else {
            toChange.setSelectedIndex(2);
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel firstLayer = new JPanel();
        firstLayer.add(toChange);
        firstLayer.add(Box.createHorizontalStrut(15));
        firstLayer.add(type);
        firstLayer.add(Box.createHorizontalStrut(15));
        firstLayer.add(new Label("X"));
        firstLayer.add(x);
        panel.add(firstLayer, Component.LEFT_ALIGNMENT);

        JPanel secondLayer = new JPanel();
        secondLayer.add(new Label("min"));
        secondLayer.add(min);
        secondLayer.add(Box.createHorizontalStrut(15));
        secondLayer.add(new Label("max"));
        secondLayer.add(max);
        secondLayer.add(Box.createHorizontalStrut(15));
        secondLayer.add(new Label("step"));
        secondLayer.add(step);
        panel.add(secondLayer, Component.LEFT_ALIGNMENT);

        JPanel thirdLayer = new JPanel();
        thirdLayer.add(new Label("M"));
        thirdLayer.add(m);
        thirdLayer.add(Box.createHorizontalStrut(15));
        thirdLayer.add(new Label("N"));
        thirdLayer.add(n);
        thirdLayer.add(Box.createHorizontalStrut(15));
        thirdLayer.add(new Label("\u0394"));
        thirdLayer.add(delta);
        panel.add(thirdLayer, Component.LEFT_ALIGNMENT);
    }

    public TestParams getParams() throws TestParams.ParameterException {
        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Please Set Testing parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedToChange = (String) toChange.getSelectedItem();
            String selectedType = (String) type.getSelectedItem();
            return new TestParams(
                    selectedToChange, selectedType,
                    min.getText(), max.getText(), step.getText(),
                    n.getText(), m.getText(), delta.getText(), x.getText()
            );
        }
        return null;
    }
}
