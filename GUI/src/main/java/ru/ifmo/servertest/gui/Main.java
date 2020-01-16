package ru.ifmo.servertest.gui;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        TestRunner testRunner;
        while (true) {
            try {
                AddressPair addressPair = AddressPane.getAddressPair();
                if (addressPair == null) {
                    System.exit(0);
                }
                testRunner = new TestRunner(addressPair);
                break;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        ResultsPane resultsPane = new ResultsPane(testRunner);
        resultsPane.setVisible(true);
        //TODO show results
    }
}
