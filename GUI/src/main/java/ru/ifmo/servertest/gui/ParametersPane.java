package ru.ifmo.servertest.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ParametersPane {
    public static TestParams getParams() throws TestParams.ParameterException {
        JTextField min = new JTextField(5);
        JTextField max = new JTextField(5);
        JTextField step = new JTextField(5);
        JTextField m = new JTextField(5);
        JTextField n = new JTextField(5);
        JTextField delta = new JTextField(5);
        JTextField x = new JTextField(5);
        String[] params = {"N", "M", "Delta"};
        JComboBox<String> toChange = new JComboBox<>(params);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel firstLayer = new JPanel();
        firstLayer.add(toChange);
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
        thirdLayer.add(new Label("Delta"));
        thirdLayer.add(delta);
        panel.add(thirdLayer, Component.LEFT_ALIGNMENT);


        int result = JOptionPane.showConfirmDialog(null, panel,
                "Please Set Testing parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String choice = (String) toChange.getSelectedItem();
            return new TestParams(
                    choice, min.getText(), max.getText(), step.getText(),
                    n.getText(), m.getText(), delta.getText(), x.getText()
            );
        }
        return null;
    }
}
