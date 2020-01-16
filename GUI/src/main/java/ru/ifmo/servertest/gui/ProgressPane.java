package ru.ifmo.servertest.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ProgressPane extends JFrame {
    private final JProgressBar bar;
    private volatile boolean canceled = false;

    public ProgressPane() {
        bar = new JProgressBar();
        add(bar);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;
            }
        });
        pack();
        setResizable(false);
        setVisible(true);
    }

    public void setMax(int max) {
        bar.setMaximum(max);
    }

    public void setMin(int min) {
        bar.setMinimum(min);
    }

    public void update(int val) {
        bar.setValue(val);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void delete() {
        setVisible(false);
        dispose();
    }

    public static void main(String[] args) {
        ProgressPane pane = new ProgressPane();
        System.out.println("window");
        int i = 0;
        while (true) {
            i ++;
            pane.update(i);
            if (pane.isCanceled()) {
                System.out.println("finish");
                pane.delete();
                break;
            }
        }
    }
}
