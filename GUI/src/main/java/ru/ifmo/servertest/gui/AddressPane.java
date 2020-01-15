package ru.ifmo.servertest.gui;

import javax.swing.*;

public class AddressPane {

    public static AddressPair getAddressPair() {
        JTextField serverIP = new JTextField(5);
        JTextField clientIP = new JTextField(5);

        JPanel jPanel = new JPanel();
        jPanel.add(new JLabel("Server:"));
        jPanel.add(serverIP);
        jPanel.add(Box.createHorizontalStrut(15)); // a spacer
        jPanel.add(new JLabel("Client:"));
        jPanel.add(clientIP);

        int result = JOptionPane.showConfirmDialog(null, jPanel,
                "Please Enter Server and Client IP", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new AddressPair(serverIP.getText(), clientIP.getText());
        }
        return null;
    }
}
